package com.acme.poc.notes.service;

import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.persistence.elasticsearch.esrepo.ESNotesRepository;
import com.acme.poc.notes.persistence.elasticsearch.generics.INotesOperations;
import com.acme.poc.notes.persistence.elasticsearch.metadata.ResourceFileReaderService;
import com.acme.poc.notes.persistence.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.persistence.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.persistence.elasticsearch.queries.SearchArchivedByExternalGuid;
import com.acme.poc.notes.persistence.elasticsearch.queries.SearchByContent;
import com.acme.poc.notes.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.persistence.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.service.generics.AbstractESService;
import com.acme.poc.notes.util.ESUtil;
import com.acme.poc.notes.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.acme.poc.notes.util.ExceptionUtil.throwRestError;

/**
 * Service to perform elastic search operations
 */
@Slf4j
@Service
public class ESNotesService extends AbstractESService implements INotesService {
    

    public ESNotesService(@Qualifier("notesProcessorV3") INotesOperations iNotesOperations,
                          ESNotesRepository esNotesRepository, 
                          ElasticsearchOperations elasticsearchOperations,
                          ResourceFileReaderService resourceFileReaderService
    ) {
        super(iNotesOperations, esNotesRepository, elasticsearchOperations, resourceFileReaderService);
    }

    /**
     * Create new entry or a thread if entryGuidParent is provided
     *
     * @param notesData Data for creating a new note entry
     * @return NotesData that is created and stored in Elasticsearch
     */
    @Override
    public NotesData create(NotesData notesData) {
        log.debug("{}", LogUtil.method());
        notesData.setGuid(UUID.randomUUID());
        notesData.setEntryGuid(UUID.randomUUID());
        notesData.setThreadGuid(UUID.randomUUID());
        notesData.setCreated(ESUtil.getCurrentDate());

        if (notesData.getEntryGuidParent() != null) {  // It's a thread that needs to be created
            List<NotesData> existingEntry = iNotesOperations.fetchAndProcessEsResults(SearchByEntryGuid.builder()
                    .searchGuid(notesData.getEntryGuidParent().toString())
                    .includeVersions(false)
                    .includeArchived(true)
                    .build());
            if (existingEntry == null || existingEntry.isEmpty()) {
                throwRestError(NotesAPIError.ERROR_NEW_RESPONSE_NO_THREAD_GUID, notesData.getEntryGuidParent());
                return null;
            }
            NotesData existingEntryFirst = existingEntry.get(0);
            if (existingEntryFirst.getArchived() != null) {
                throwRestError(NotesAPIError.ERROR_ENTRY_ARCHIVED_CANNOT_ADD_THREAD, existingEntryFirst.getExternalGuid(), existingEntryFirst.getEntryGuid());
                return null;
            }
            notesData.setThreadGuid(existingEntryFirst.getThreadGuid());
            notesData.setExternalGuid(existingEntryFirst.getExternalGuid());
            log.debug("Creating a thread for externalGuid: {}, entryGuid: {}", notesData.getExternalGuid().toString(), notesData.getEntryGuid().toString());
        } else {
            log.debug("Creating a new entry for externalGuid: {}", notesData.getExternalGuid().toString());
        }
        NotesData newEntry = esNotesRepository.save(notesData);
        if (newEntry == null) {
            // TODO What should happen in case of failure?
        }
        log.debug("Successfully created a new entry entryGuid: {} ", newEntry.getEntryGuid());
        return newEntry;
    }

    /**
     * Search entry by guid
     *
     * @param guid
     * @return Entry from Elasticsearch for given guid
     */
    @Override
    public NotesData getByGuid(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        return esNotesRepository.findById(guid).orElse(null);
    }

    /**
     * Update entry by guid. if guid is not provided
     * fetches recent entry for given entryGuid and updates it and create a new entry with updated content.
     * if entry is recently updated while this update is being made then throw an error asking for reload an entry and update again
     * @param updatedEntry
     * @return updated entry
     */
    @Override
    public NotesData updateByGuid(NotesData updatedEntry) {
        log.debug("{} externalGuid: {}, entryGuid: {}", LogUtil.method(), updatedEntry.getExternalGuid(), updatedEntry.getEntryGuid());
        if (updatedEntry.getGuid() == null) {
            throwRestError(NotesAPIError.ERROR_MISSING_GUID);
        }

        NotesData existingEntry = getByGuid(updatedEntry.getGuid());
        if (existingEntry == null) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, updatedEntry.getGuid());
        }

        return update(existingEntry, updatedEntry);
    }

    /**
     * Update entry by entryGuid. if guid is not provided 
     * fetches recent entry for given entryGuid and updates it and create a new entry with updated content.
     * if entry is recently updated while this update is being made then throw an error asking for reload an entry and update again
     * @param updatedEntry
     * @return updated entry
     */
    @Override
    public NotesData updateByEntryGuid(NotesData updatedEntry) {
        log.debug("{} externalGuid: {}, entryGuid: {}", LogUtil.method(), updatedEntry.getExternalGuid(), updatedEntry.getEntryGuid());
        if (updatedEntry.getEntryGuid() == null) {
            throwRestError(NotesAPIError.ERROR_MISSING_ENTRY_GUID);
        }

        List<NotesData> searchResult = iNotesOperations.fetchAndProcessEsResults(SearchByEntryGuid.builder()
                .searchGuid(updatedEntry.getEntryGuid().toString())
                .includeVersions(false)
                .includeArchived(true)
                .build());
        if (searchResult == null || searchResult.isEmpty()) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_ENTRY_GUID, updatedEntry.getEntryGuid());
        }
        NotesData existingEntry = searchResult.get(0);
        return update(existingEntry, updatedEntry);
    }
    
    private NotesData update(NotesData existingEntry, NotesData updatedEntry) {
        log.debug("{}", LogUtil.method());
        if (updatedEntry.getCreated() == null) {
            throwRestError(NotesAPIError.ERROR_MISSING_CREATED);
        }
        if (!updatedEntry.getCreated().equals(existingEntry.getCreated())) {
            throwRestError(NotesAPIError.ERROR_ENTRY_HAS_BEEN_MODIFIED, existingEntry.getCreated());    // TODO Make sure we format all timestamps in {@link NotesConstants.TIMESTAMP_ISO8601} format (not here, but in throwRestError method)
        }
        if (existingEntry.getArchived() != null) {
            throwRestError(NotesAPIError.ERROR_ENTRY_ARCHIVED_NO_UPDATE);
        }

        ESUtil.clearHistoryAndThreads(existingEntry);
        existingEntry.setGuid(UUID.randomUUID());
        existingEntry.setCreated(ESUtil.getCurrentDate());
        existingEntry.setContent(updatedEntry.getContent());
        NotesData updated = esNotesRepository.save(existingEntry);
        if (updated == null) {
            // TODO What should happen in case of failure?
            return null;
        }

        log.debug("Updated externalGuid: {}, entryGuid: {}, changed content from: {} to: {}", updatedEntry.getExternalGuid(), updatedEntry.getEntryGuid(), existingEntry.getContent(), updatedEntry.getContent());
        return updated;
    }

    /**
     * Archive by updating existing entry. updates archived field on elastic search with current date and time.
     * @param query - archive is done querying by either externalGuid / entryGuid
     * @return archived entries
     */
    @Override
    public List<NotesData> archive(IQuery query) {
        log.debug("{} request: {}", LogUtil.method(), query.getClass().getSimpleName());
        List<SearchHit<NotesData>> searchHitList = getAllEntries(query);
        List<NotesData> processed = iNotesOperations.process(query, searchHitList.stream().iterator());
        Set<NotesData> flatten = new HashSet<>();
        try {
            ESUtil.flatten(processed,flatten);
            archive(flatten);
        } catch (Exception e) {
            throwRestError(NotesAPIError.ERROR_ARCHIVING, e.getMessage());
        }
        AbstractQuery getArchived = (AbstractQuery)query;
        getArchived.setIncludeArchived(true);
        return iNotesOperations.process(getArchived, getAllEntries(getArchived).stream().iterator());
    }

    /**
     * Archive by updating existing entry. updates archived field on elastic search with current date and time.
     * @param guid - archive is done querying by guid
     * @return archived entries
     */
    @Override
    public List<NotesData> archive(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        Optional<NotesData> result = esNotesRepository.findById(guid);
        if (!result.isPresent()) {
            throwRestError(NotesAPIError.ERROR_NOT_EXISTS_GUID, guid);
        }
        archive(Set.of(result.get()));
        return List.of(esNotesRepository.findById(guid).orElse(null));
    }

    @Override
    public List<NotesData> searchByEntryGuid(SearchByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<NotesData> searchByContent(SearchByContent iQuery) {
        log.debug("{} content: {}", LogUtil.method(), iQuery.getContentToSearch());
        return search(iQuery);
    }

    @Override
    public List<NotesData> searchArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery) {
        log.debug("{} externalGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<NotesData> searchArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return search(iQuery);
    }

    @Override
    public List<NotesData> deleteArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery) {
        log.debug("{} externalGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return delete(iQuery);
    }

    @Override
    public List<NotesData> deleteArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery) {
        log.debug("{} entryGuid: {}", LogUtil.method(), iQuery.getSearchGuid());
        return delete(iQuery);
    }

    private void archive(Set<NotesData> entriesToArchive) {
        Date dateTime = ESUtil.getCurrentDate();
        try {
            entriesToArchive.forEach(entryToArchive -> esNotesRepository.save(NotesData.builder()
                    .archived(dateTime)
                    .externalGuid(entryToArchive.getExternalGuid())
                    .entryGuid(entryToArchive.getEntryGuid())
                    .guid(entryToArchive.getGuid())
                    .threadGuid(entryToArchive.getThreadGuid())
                    .entryGuidParent(entryToArchive.getEntryGuidParent())
                    .type(entryToArchive.getType())
                    .content(entryToArchive.getContent())
                    .created(entryToArchive.getCreated())
                    .build())
            );
        } catch (Exception e) {
            throwRestError(NotesAPIError.ERROR_ARCHIVING, e.getMessage());
        }
        log.debug("Number of entries archived: {}", entriesToArchive.size());
    }

}
