package com.acme.poc.notes.restservice.generics.abstracts.disctinct;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Match;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.generics.abstracts.AbstractNotesProcessor;
import com.acme.poc.notes.restservice.generics.interfaces.INotesCrudOperations;
import com.acme.poc.notes.restservice.util.ESUtil;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


@Slf4j
@Service
public abstract class AbstractNotesCrudOperations<E extends INoteEntity<E>> extends AbstractNotesProcessor<E> implements INotesCrudOperations<E> {

    @Value("${default.number.of.entries.to.return}")
    private int default_number_of_entries;
    
    protected CrudRepository crudRepository;


    public AbstractNotesCrudOperations(CrudRepository crudRepository) {
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
        if (ObjectUtils.isEmpty(entity.getCreated())) {
            entity.setCreated(ESUtil.getCurrentDate());
        }
        
        if (entity.getEntryGuidParent() != null) {  // It's a thread that needs to be created
            List<E> existingEntry = getProcessed(QueryRequest.builder()
                    .searchField(Match.ENTRY)
                    .searchData(entity.getEntryGuidParent().toString())
                    .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                    .build());
            if (existingEntry == null || existingEntry.isEmpty()) {
                throwRestError(NotesAPIError.ERROR_NEW_RESPONSE_NO_THREAD_GUID, entity.getEntryGuidParent());
                return null;
            }
            E existingEntryFirst = existingEntry.get(0);
            if (existingEntryFirst.getArchived() != null) {
                throwRestError(NotesAPIError.ERROR_ENTRY_ARCHIVED_CANNOT_ADD_THREAD, existingEntryFirst.getExternalGuid(), existingEntryFirst.getEntryGuid());
                return null;
            }
            entity.setThreadGuid(existingEntryFirst.getThreadGuid());
            entity.setExternalGuid(existingEntryFirst.getExternalGuid());
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
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        return (E) crudRepository.findById(guid).orElse(null);
    }

    @Override
    public List<E> getByQuery(IQueryRequest query) {
        log.debug("{}", LogUtil.method());
        return getProcessed(query);
    }

    /**
     * Deletes entries by either externalGuid/entryGuid
     *
     * @param query search entries for given externalGuid/entryGuid
     * @return deleted entries
     */
    @Override
    public List<E> delete(IQueryRequest query) {
        log.debug("{} request: {}", LogUtil.method(), query.getClass().getSimpleName());
        List<E> searchHitList = getAll(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<E> processed = process(query, searchHitList.stream().iterator());
        try {
            crudRepository.deleteAll(processed);
        } catch (Exception e) {
            log.error("Error while deleting entries for request: {} -- " + query.getClass().getSimpleName(), e.getMessage());
            throwRestError(NotesAPIError.ERROR_SERVER);
        }
        log.debug("Successfully deleted all {} entries", processed.size());
        return processed;
    }

    @Override
    public E delete(UUID keyGuid) {
        log.debug("{} keyGuid: {}", LogUtil.method(), keyGuid);
        E entity = (E) crudRepository.findById(keyGuid).orElse(null);
        if (entity != null) {
            crudRepository.deleteById(keyGuid);
            if (!crudRepository.findById(keyGuid).isPresent()) {
                log.debug("Successfully deleted an entry with guid: {}", entity.getGuid());
                return entity;
            }
        }
        log.error("Cannot delete. No entry found for given guid: {}", keyGuid);
        throwRestError(NotesAPIError.ERROR_NOT_FOUND);
        return null;
    }

    @Override
    public List<E> getAll(IQueryRequest query) {
        log.debug("{}", LogUtil.method());
        long startTime = System.currentTimeMillis();
        boolean timeout = false;
        List<E> searchHitList = new ArrayList<>();
        List<E> searchHits = getUnprocessed(query);
        searchHitList.addAll(searchHits);
        while (searchHits != null && !searchHits.isEmpty() && !timeout && searchHits.size() == default_number_of_entries) {
            searchHitList.addAll(searchHits);
            List<Object> sortValues = List.of(searchHits.get(searchHits.size() - 1).getCreated()); // TODO make sort value created dynamic
            query.searchAfter(sortValues.size() > 0 ? sortValues.get(0) : null);
            searchHits = getUnprocessed(query);
            timeout = (System.currentTimeMillis() - startTime) >= NotesConstants.TIMEOUT_DELETE;
        }
        if (timeout) {
            long timeTaken = (System.currentTimeMillis() - startTime);
            log.error("Delete entries operation timed out, time taken: {} ms", timeTaken);
            throwRestError(NotesAPIError.ERROR_TIMEOUT_DELETE, timeTaken);
        }
        if (searchHitList == null || searchHitList.isEmpty()) {
            log.error("No entries found for request: {}", query.getClass().getSimpleName());
            throwRestError(NotesAPIError.ERROR_NOT_FOUND);
        }
        log.debug("Number of entries found: {}", searchHitList.size());
        return searchHitList;
    }

    /**
     * Update entry by guid. if guid is not provided
     * fetches recent entry for given entryGuid and updates it and create a new entry with updated content.
     * if entry is recently updated while this update is being made then throw an error asking for reload an entry and update again
     *
     * @param entity
     * @return updated entry
     */
    
    @Override
    public E update(E entity) {
        log.debug("{} guid: {}, externalGuid: {}, entryGuid: {}", LogUtil.method(), entity.getGuid(),
                entity.getExternalGuid(), entity.getEntryGuid());
        if (entity.getGuid() == null && entity.getEntryGuid() == null) {
            throwRestError(NotesAPIError.ERROR_MISSING_PROPERTIES_FOR_UPDATE);
            return null;
        } else if (entity.getGuid() != null) {
            E existingEntry = get(entity.getGuid());
            if (existingEntry == null) {
                throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, entity.getGuid());
            }
            return update(existingEntry, entity);
        } else {
            List<E> searchResult = getProcessed(QueryRequest.builder()
                    .searchField(Match.ENTRY)
                    .searchData(entity.getEntryGuid().toString())
                    .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                    .build());
            if (searchResult == null || searchResult.isEmpty()) {
                throwRestError(NotesAPIError.ERROR_NOT_EXISTS_ENTRY_GUID, entity.getEntryGuid());
            }
            E existingEntry = searchResult.get(0);
            return update(existingEntry, entity);
        }
    }

    private E update(E existingEntity, E payloadEntry) {
        log.debug("{}", LogUtil.method());
        if (payloadEntry.getCreated() == null) {
            throwRestError(NotesAPIError.ERROR_MISSING_CREATED);
        }
        if (!payloadEntry.getCreated().equals(existingEntity.getCreated())) {
            throwRestError(NotesAPIError.ERROR_ENTRY_HAS_BEEN_MODIFIED, existingEntity.getCreated());  // TODO Make sure we format all timestamps in {@link NotesConstants.TIMESTAMP_ISO8601} format (not here, but in throwRestError method)
        }
        if (existingEntity.getArchived() != null) {
            throwRestError(NotesAPIError.ERROR_ENTRY_ARCHIVED_NO_UPDATE);
        }

        ESUtil.clearHistoryAndThreads(existingEntity);
        E updating = existingEntity.getInstance(existingEntity);
        updating.setContent(payloadEntry.getContent());
        updating.setGuid(UUID.randomUUID());
        updating.setCreated(ESUtil.getCurrentDate());
        E newEntryUpdated = save(updating);
        log.debug("Updated externalGuid: {}, entryGuid: {}, changed content from: {} to: {}", payloadEntry.getExternalGuid(), 
                payloadEntry.getEntryGuid(), existingEntity.getContent(), payloadEntry.getContent());
        return newEntryUpdated;
    }
    
    private E save(E existingEntity) {
        E newEntryUpdated = null;
        try {
            newEntryUpdated = (E) crudRepository.save(existingEntity);
            if (newEntryUpdated == null) {
                log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(),LogUtil.method(),"DataBase returned null value"));
                throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION,LogUtil.method(),"DataBase returned null value");
            }
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_DB_OPERATION.errorMessage(),LogUtil.method(),e.getMessage()),e);
            throwRestError(NotesAPIError.ERROR_ON_DB_OPERATION,LogUtil.method(),e.getMessage());
        }
        return newEntryUpdated;
    }
}
