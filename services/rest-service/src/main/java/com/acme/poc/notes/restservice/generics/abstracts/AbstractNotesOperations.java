package com.acme.poc.notes.restservice.generics.abstracts;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.restservice.generics.models.INoteEntity;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.generics.interfaces.INotesOperations;
import com.acme.poc.notes.restservice.util.ESUtil;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


@Slf4j
@Service
public abstract class AbstractNotesOperations<E extends INoteEntity<E>> extends AbstractNotesProcessor<E> implements INotesOperations<E> {
    
    protected CrudRepository crudRepository;


    public AbstractNotesOperations(CrudRepository crudRepository) {
        this.crudRepository = crudRepository;
    }


    /**
     * Create new entry or a thread if entryGuidParent is provided
     *
     * @param entity Data for creating a new note entry
     * @return T that is created and stored in database
     */
    @Override
    public E create(E entity) {
        log.debug("{}", LogUtil.method());
        if (ObjectUtils.isEmpty(entity.getGuid())) {
            entity.setGuid(UUID.randomUUID());
        }
        if (ObjectUtils.isEmpty(entity.getEntryGuid())) {
            entity.setEntryGuid(UUID.randomUUID());
        }
        if (ObjectUtils.isEmpty(entity.getThreadGuid())) {
            entity.setThreadGuid(UUID.randomUUID());
        }
        Date created = ESUtil.getCurrentDate();
        if (ObjectUtils.isEmpty(entity.getCreated())) {
            entity.setCreated(created);
        }
        if (ObjectUtils.isEmpty(entity.getCreatedInitially())) {
            entity.setCreatedInitially(created);
        }
        if (entity.getEntryGuidParent() != null) {  // It's a thread that needs to be created
            List<E> existingEntry = getProcessed(QueryRequest.builder()
                    .searchField(Field.ENTRY)
                    .searchData(entity.getEntryGuidParent().toString())
                    .filters(Set.of(Filter.GET_ONLY_RECENT, Filter.INCLUDE_ARCHIVED))
                    .build());
            if (existingEntry == null || existingEntry.isEmpty()) {
                throwRestError(NotesAPIError.ERROR_NEW_RESPONSE_NO_THREAD_GUID, entity.getEntryGuidParent());
                return null;
            }
            E creatingThread = existingEntry.get(0);
            if (creatingThread.getArchived() != null) {
                throwRestError(NotesAPIError.ERROR_ENTRY_ARCHIVED_CANNOT_ADD_THREAD, creatingThread.getExternalGuid(), creatingThread.getEntryGuid());
                return null;
            }
            entity.setThreadGuid(creatingThread.getThreadGuid());
            entity.setExternalGuid(creatingThread.getExternalGuid());
            log.debug("Creating a thread for externalGuid: {}, entryGuid: {}", entity.getExternalGuid().toString(), entity.getEntryGuid().toString());
        } else {
            log.debug("Creating a new entry for externalGuid: {}", entity.getExternalGuid());
        }
        E newEntry = save(entity);
        log.debug("Successfully created a new entry entryGuid: {} ", newEntry.getEntryGuid());
        return newEntry;
    }

    /**
     * Search entry by guid
     *
     * @param guid
     * @return Entry from database for given guid
     */
    @Override
    public E get(UUID guid) {
        log.debug("{} request: {}, field: {}, uuid: {}", LogUtil.method(), "get", "guid", guid);
        E entry = (E) crudRepository.findById(guid).orElse(null);
        if(entry != null && (entry.getOperationStatus().equals(OperationStatus.MARK_FOR_SOFT_DELETE) || entry.getOperationStatus().equals(OperationStatus.SOFT_DELETED))) {
            throwRestError(NotesAPIError.ERROR_SOFT_DELETED);
        }
        return entry;
    }

    @Override
    public List<E> get(IQueryRequest query) {
        log.debug("{} request: {}, field: {}, data: {}", LogUtil.method(), "get by query", query.getSearchField().getFieldName(), query.getSearchData());
        return getProcessed(query);
    }

    /**
     * Deletes entries by either externalGuid/entryGuid for {@link OperationStatus#DELETE}{@link OperationStatus#ACTIVE}
     * For other OperationStatus just mark for delete entries by given query. 
     * entry is just marked for delete and backend job should pick it up perform operation
     * @param query search entries for given externalGuid/entryGuid
     * @return deleted entries
     */
    @Override
    public List<E> delete(IQueryRequest query, OperationStatus operationStatus) {
        log.debug("{} request: {}, deleteType: {}, field: {}, uuid: {}", LogUtil.method(), "delete by query",
                operationStatus, query.getSearchField().getFieldName(), query.getSearchData());
        query.getFilters().add(Filter.INCLUDE_SOFT_DELETED);
        List<E> searchHitList = search(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<E> cloned = new ArrayList<>(); // cloned for processing again for TREE view
        searchHitList.stream().forEach(s -> cloned.add(s.clone()));
        List<E> processed = process(query, searchHitList.stream().iterator());
        try {
            if (operationStatus == OperationStatus.DELETE || operationStatus == OperationStatus.ACTIVE) {
                crudRepository.deleteAll(processed);
            } else {
                processed.stream().forEach(entity -> {
                    entity.setOperationStatus(operationStatus);
                    crudRepository.save(entity);
                });
            }
        } catch (Exception e) {
            log.error("Error while performing delete operation. operation: {} field: {} and UUID: {}", operationStatus,
                    query.getSearchField().getFieldName(),query.getSearchData());
            throwRestError(NotesAPIError.ERROR_SERVER, e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage());
        }
        log.debug("Successfully performed delete operations. operation: {} entries: {}", operationStatus, processed.size());
        query.setResultFormat(ResultFormat.TREE);
        return process(query, cloned.stream().iterator());
    }

    /**
     * Deletes entries by guid for {@link OperationStatus#DELETE}{@link OperationStatus#ACTIVE}
     * For other OperationStatus just mark for delete entries by given query. 
     * entry is just marked for delete and backend job should pick it up perform operation
     * @param keyGuid get entry for given key guid
     * @return deleted entries
     */
    @Override
    public E delete(UUID keyGuid, OperationStatus operationStatus) {
        log.debug("{} request: {}, deleteType: {}, field: {}, uuid: {}", LogUtil.method(), "delete by guid", operationStatus, "guid", keyGuid);
        E entity = (E) crudRepository.findById(keyGuid).orElse(null);
        if (entity != null) {
            if (operationStatus == OperationStatus.DELETE ||
                    operationStatus == OperationStatus.ACTIVE) {
                crudRepository.deleteById(keyGuid);
                if (!crudRepository.findById(keyGuid).isPresent()) {
                    log.debug("Successfully performed delete operations. operation: {} guid: {}", operationStatus, keyGuid);
                    return entity;
                }
            } else {
                entity.setOperationStatus(operationStatus);
                return (E) crudRepository.save(entity);
            }
        }
        log.error("Cannot delete. No entry found. operation: {} given guid: {}", operationStatus, keyGuid);
        throwRestError(NotesAPIError.ERROR_NOT_FOUND);
        return null;
    }

    /**
     * Update entry by guid/entryGuid. if guid is not provided
     * fetches recent entry for given entryGuid and updates it and create a new entry with updated content.
     * if entry is recently updated while this update is being made then throw an error asking for reload an entry and update again
     *
     * @param payloadEntity
     * @return updated entry
     */
    
    @Override
    public E update(E payloadEntity) {
        log.debug("{}, field: {}, uuid: {}, content: {}", LogUtil.method(),
                payloadEntity.getGuid() != null ? "guid" : "entryGuid",
                payloadEntity.getGuid() != null ? payloadEntity.getGuid() : payloadEntity.getEntryGuid(), payloadEntity.getContent());
        E entry = null;
       if (payloadEntity.getGuid() != null) {
           entry = get(payloadEntity.getGuid());
            if (entry == null) {
                throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, payloadEntity.getGuid());
            }
        }
        List<E> searchResult = getProcessed(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(entry.getEntryGuid().toString())
                .filters(Set.of(Filter.GET_ONLY_RECENT, Filter.INCLUDE_ARCHIVED))
                .build());
        if (searchResult == null || searchResult.isEmpty()) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_ENTRY_GUID, payloadEntity.getEntryGuid());
        }
        return validateAndUpdate(searchResult.get(0), payloadEntity);
    }

    @Override
    public E restore(UUID keyGuid) {
        log.debug("{}, field: {}, uuid: {}", LogUtil.method(), "guid", keyGuid);
        E entity = (E) crudRepository.findById(keyGuid).orElse(null);
        if(entity != null ) {
            if (!(entity.getOperationStatus().equals(OperationStatus.MARK_FOR_SOFT_DELETE) ||
                    entity.getOperationStatus().equals(OperationStatus.SOFT_DELETED))) {
                throwRestError(NotesAPIError.ERROR_SOFT_DELETED_ENTRIES_NOT_FOUND);
            }
        }
        try {
            entity.setOperationStatus(OperationStatus.UPSERT);
            crudRepository.save(entity);
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(), LogUtil.method(), e), e);
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION,LogUtil.method(), e.getMessage());
        }
        log.debug("Restore Successful, guid: {}", keyGuid);
        return entity;
    }
    @Override
    public List<E> restore(IQueryRequest query) {
        log.debug("{}, field: {}, uuid: {}", LogUtil.method(), query.getSearchField().getFieldName(), query.getSearchData());
        query.getFilters().add(Filter.ONLY_SOFT_DELETED);
        List<E> searchHitList = search(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<E> processed = process(query, searchHitList.stream().iterator());
        if(processed.isEmpty()) {
            throwRestError(NotesAPIError.ERROR_SOFT_DELETED_ENTRIES_NOT_FOUND);
        }
        try {
            processed.forEach(entryToArchive -> {
                entryToArchive.setOperationStatus(OperationStatus.UPSERT);
                crudRepository.save(entryToArchive);
            });
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(), LogUtil.method(), e), e);
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION,LogUtil.method(), e.getMessage());
        }
        log.debug("Restore Successful, Number of entries restored: {}", processed.size());
        query.setResultFormat(ResultFormat.TREE);
        query.getFilters().remove(Filter.ONLY_SOFT_DELETED);
        return process(query, search(query).stream().iterator());
    }

    /**
     * Archive by updating existing entry. updates archived field on database with current date and time.
     *
     * @param query - archive is done querying by either externalGuid / entryGuid
     * @return archived entries
     */
    @Override
    public List<E> archive(IQueryRequest query) {
        log.debug("{} request: {}, field: {}, uuid: {}", LogUtil.method(), "archive", query.getSearchField().getFieldName(), query.getSearchData());
        List<E> searchHitList = search(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<E> processed = process(query, searchHitList.stream().iterator());
        archive(processed);
        query.getFilters().remove(Filter.EXCLUDE_ARCHIVED);
        query.getFilters().add(Filter.INCLUDE_ARCHIVED);
        query.setResultFormat(ResultFormat.TREE);
        return process(query, search(query).stream().iterator());
    }

    /**
     * Archive by updating existing entry. updates archived field on database with current date and time.
     *
     * @param guid - archive is done querying by guid
     * @return archived entries
     */
    @Override
    public List<E> archive(UUID guid) {
        log.debug("{} request: {}, field: {}, uuid: {}", LogUtil.method(), "archive", "guid", guid);
        Optional<E> result = crudRepository.findById(guid);
        if (!result.isPresent()) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, guid);
        }
        if((result.get().getOperationStatus().equals(OperationStatus.MARK_FOR_SOFT_DELETE) || result.get().getOperationStatus().equals(OperationStatus.SOFT_DELETED))) {
            throwRestError(NotesAPIError.ERROR_SOFT_DELETED);
        }
        archive(List.of(result.get()));
        return List.of((E) crudRepository.findById(guid).orElse(null));
    }

    /**
     * Get all entries from index/table/collections/any data store
     * @param dataStore
     * @return
     */
    @Override
    public List<E> get(String dataStore) {
        log.debug("{} request: {}, data store name: {}", LogUtil.method(), "get from data store", dataStore);
        return getProcessed(
                QueryRequest.builder()
                        .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                        .build());
    }

    /**
     * Get crud repo for any other operations
     */
    @Override
    public CrudRepository getCrudRepository() {
        return crudRepository;
    }

    private void archive(List<E> entriesToArchive) {
        Date dateTime = ESUtil.getCurrentDate();
        try {
            entriesToArchive.forEach(entryToArchive -> {
                entryToArchive.setArchived(dateTime);
                crudRepository.save(entryToArchive);
            });
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(), LogUtil.method(), e), e);
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION,LogUtil.method(), e.getMessage());
        }
        log.debug("Number of entries archived: {}", entriesToArchive.size());
    }

    private E validateAndUpdate(E recentEntry, E payloadEntity) {
        log.debug("{}", LogUtil.method());
        if (!payloadEntity.getCreated().equals(recentEntry.getCreated())) {
            throwRestError(NotesAPIError.ERROR_ENTRY_HAS_BEEN_MODIFIED, recentEntry.getCreated());  // TODO Make sure we format all timestamps in {@link NotesConstants.TIMESTAMP_ISO8601} format (not here, but in throwRestError method)
        }
        if (recentEntry.getArchived() != null) {
            throwRestError(NotesAPIError.ERROR_ENTRY_ARCHIVED_NO_UPDATE);
        }
        if((recentEntry.getOperationStatus().equals(OperationStatus.MARK_FOR_SOFT_DELETE) || recentEntry.getOperationStatus().equals(OperationStatus.SOFT_DELETED))) {
            throwRestError(NotesAPIError.ERROR_SOFT_DELETED);
        }
        ESUtil.clearHistoryAndThreads(recentEntry);
        E updating = recentEntry.clone();
        updating.setContent(payloadEntity.getContent());
        updating.setGuid(UUID.randomUUID());
        updating.setCreated(ESUtil.getCurrentDate());
        E newEntryUpdated = save(updating);
        log.debug("Updated externalGuid: {}, entryGuid: {}, changed content from: {} to: {}", recentEntry.getExternalGuid(),
                recentEntry.getEntryGuid(), recentEntry.getContent(), payloadEntity.getContent());
        return newEntryUpdated;
    }
    
    @Transactional
    private E save(E existingEntity) {
        E newEntryUpdated = null;
        try {
            existingEntity.setOperationStatus(OperationStatus.UPSERT);
            newEntryUpdated = (E) crudRepository.save(existingEntity);
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(),LogUtil.method(),e.getMessage()),e);
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION,LogUtil.method(),e.getMessage());
        }
        if (newEntryUpdated == null) {
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(),LogUtil.method(),"DataBase returned null value"));
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION,LogUtil.method(),"DataBase returned null value");
        }
        return newEntryUpdated;
    }
}
