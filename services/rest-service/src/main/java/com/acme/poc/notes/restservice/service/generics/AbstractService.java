package com.acme.poc.notes.restservice.service.generics;
import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.persistence.elasticsearch.generics.INotesProcessor;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.enums.ResultFormat;
import com.acme.poc.notes.restservice.util.ESUtil;
import com.acme.poc.notes.restservice.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;

@Slf4j
@Service
public abstract class AbstractService<E extends INoteEntity> implements ICommonOperations<E> {
    @Value("${default.number.of.entries.to.return}")
    private int default_number_of_entries;
    protected INotesProcessor iNotesProcessor;
    protected CrudRepository crudRepository;
    
    public AbstractService(@Qualifier("NotesProcessor") INotesProcessor iNotesProcessor, CrudRepository crudRepository) {
        this.iNotesProcessor = iNotesProcessor;
        this.crudRepository = crudRepository;
    }

    /**
     * Search entry by guid
     *
     * @param guid
     * @return Entry from Elasticsearch for given guid
     */
    @Override
    public E getByGuid(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        return (E) crudRepository.findById(guid).orElse(null);
    }

    /**
     * Create new entry or a thread if entryGuidParent is provided
     *
     * @param entity Data for creating a new note entry
     * @return T that is created and stored in Elasticsearch
     */
    @Override
    public E create(E entity) {
        log.debug("{}", LogUtil.method());
        entity.setGuid(UUID.randomUUID());
        entity.setEntryGuid(UUID.randomUUID());
        entity.setThreadGuid(UUID.randomUUID());
        entity.setCreated(ESUtil.getCurrentDate());

        if (entity.getEntryGuidParent() != null) {  // It's a thread that needs to be created
            List<E> existingEntry = (List<E>) iNotesProcessor.fetchAndProcessEsResults(SearchByEntryGuid.builder()
                    .searchGuid(entity.getEntryGuidParent().toString())
                    .includeVersions(false)
                    .includeArchived(true)
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
        E newEntry = null;
        try {
            newEntry = (E) crudRepository.save(entity);
            if (newEntry == null) {
                log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(),LogUtil.method(),"ES returned null value"));
                throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH,LogUtil.method(),"ES returned null value");
            }
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(),LogUtil.method(),e.getMessage()),e);
            throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH,LogUtil.method(),e.getMessage());
        }
        log.debug("Successfully created a new entry entryGuid: {} ", newEntry.getEntryGuid());
        return newEntry;
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
    public E updateByGuid(E entity) {
        log.debug("{} externalGuid: {}, entryGuid: {}", LogUtil.method(), entity.getExternalGuid(), entity.getEntryGuid());
        if (entity.getGuid() == null) {
            throwRestError(NotesAPIError.ERROR_MISSING_GUID);
        }

        E existingEntry = getByGuid(entity.getGuid());
        if (existingEntry == null) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, entity.getGuid());
        }

        return update(existingEntry, entity);
    }

    /**
     * Update entry by entryGuid. if guid is not provided 
     * fetches recent entry for given entryGuid and updates it and create a new entry with updated content.
     * if entry is recently updated while this update is being made then throw an error asking for reload an entry and update again
     *
     * @param entity
     * @return updated entry
     */
    @Override
    public E updateByEntryGuid(E entity) {
        log.debug("{} externalGuid: {}, entryGuid: {}", LogUtil.method(), entity.getExternalGuid(), entity.getEntryGuid());
        if (entity.getEntryGuid() == null) {
            throwRestError(NotesAPIError.ERROR_MISSING_ENTRY_GUID);
        }

        List<E> searchResult = (List<E>) iNotesProcessor.fetchAndProcessEsResults(SearchByEntryGuid.builder()
                .searchGuid(entity.getEntryGuid().toString())
                .includeVersions(false)
                .includeArchived(true)
                .build());
        if (searchResult == null || searchResult.isEmpty()) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_ENTRY_GUID, entity.getEntryGuid());
        }
        E existingEntry = searchResult.get(0);
        return update(existingEntry, entity);
    }

    private E update(E existingEntity, E updatedEntity) {
        log.debug("{}", LogUtil.method());
        if (updatedEntity.getCreated() == null) {
            throwRestError(NotesAPIError.ERROR_MISSING_CREATED);
        }
        if (!updatedEntity.getCreated().equals(existingEntity.getCreated())) {
            throwRestError(NotesAPIError.ERROR_ENTRY_HAS_BEEN_MODIFIED, existingEntity.getCreated());    // TODO Make sure we format all timestamps in {@link NotesConstants.TIMESTAMP_ISO8601} format (not here, but in throwRestError method)
        }
        if (existingEntity.getArchived() != null) {
            throwRestError(NotesAPIError.ERROR_ENTRY_ARCHIVED_NO_UPDATE);
        }

        ESUtil.clearHistoryAndThreads(existingEntity); // TODO it needs to be corrected for getByGuid
        existingEntity.setGuid(UUID.randomUUID());
        existingEntity.setCreated(ESUtil.getCurrentDate());
        existingEntity.setContent(updatedEntity.getContent());
        E updated =null;
        try {
            updated = (E) crudRepository.save(existingEntity);
            if (updated == null) {
                log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(),LogUtil.method(),"ES returned null value"));
                throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH,LogUtil.method(),"ES returned null value");
            }
        } catch (Exception e) {
            log.error(String.format(NotesAPIError.ERROR_ON_ELASTICSEARCH.errorMessage(),LogUtil.method(),e.getMessage()),e);
            throwRestError(NotesAPIError.ERROR_ON_ELASTICSEARCH,LogUtil.method(),e.getMessage());
        }
        log.debug("Updated externalGuid: {}, entryGuid: {}, changed content from: {} to: {}", updatedEntity.getExternalGuid(), updatedEntity.getEntryGuid(), existingEntity.getContent(), updatedEntity.getContent());
        return updated;
    }

    /**
     * Deletes entries by either externalGuid/entryGuid
     *
     * @param query search entries for given externalGuid/entryGuid
     * @return deleted entries
     */
    @Override
    public List<E> delete(IQuery query) {
        log.debug("{} request: {}", LogUtil.method(), query.getClass().getSimpleName());
        List<SearchHit<E>> searchHitList = getAllEntries(query);
        query.setResultFormat(ResultFormat.FLATTEN);
        List<E> processed = (List<E>) iNotesProcessor.process(query, searchHitList.stream().iterator());
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

    public List<E> search(IQuery query) {
        log.debug("{}", LogUtil.method());
        return iNotesProcessor.fetchAndProcessEsResults(query);
    }

    protected List<SearchHit<E>> getAllEntries(IQuery query) {
        log.debug("{}", LogUtil.method());
        long startTime = System.currentTimeMillis();
        boolean timeout = false;
        List<SearchHit<E>> searchHitList = new ArrayList<>();
        SearchHits<E> searchHits = (SearchHits<E>) iNotesProcessor.getEsResults(query);
        searchHitList.addAll(searchHits.getSearchHits());
        while (searchHits != null && !searchHits.isEmpty() && !timeout && searchHits.getSearchHits().size() == default_number_of_entries) {
            searchHitList.addAll(searchHits.getSearchHits());
            List<Object> sortValues = searchHits.getSearchHits().get(searchHits.getSearchHits().size() - 1).getSortValues();
            query.searchAfter(sortValues.size() > 0 ? sortValues.get(0) : null);
            searchHits = (SearchHits<E>) iNotesProcessor.getEsResults(query);
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
        log.debug("Number of entries found: {}",searchHitList.size());
        return searchHitList;
    }
}
