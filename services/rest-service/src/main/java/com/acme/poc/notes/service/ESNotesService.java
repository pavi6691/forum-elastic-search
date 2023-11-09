package com.acme.poc.notes.service;
import com.acme.poc.notes.elasticsearch.esrepo.ESNotesRepository;
import com.acme.poc.notes.elasticsearch.generics.INotesOperations;
import com.acme.poc.notes.elasticsearch.metadata.ResourceFileReaderService;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.*;
import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.service.generics.AbstractESService;
import com.acme.poc.notes.util.ESUtil;
import com.acme.poc.notes.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Service to perform elastic search operations
 */
@Slf4j
@Service
public class ESNotesService extends AbstractESService implements INotesService {
    

    public ESNotesService(@Qualifier("notesProcessorV3") INotesOperations iNotesOperations,
                          ESNotesRepository esNotesRepository, 
                          ElasticsearchOperations elasticsearchOperations,
                          ResourceFileReaderService resourceFileReaderService) {
        super(iNotesOperations,esNotesRepository,elasticsearchOperations,resourceFileReaderService);
    }

    /**
     * Create new entry or a thread if threadGuidParent is provided
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
        if (notesData.getThreadGuidParent() != null) {  // It's a thread that needs to created
            List<NotesData> existingEntry = iNotesOperations.fetchAndProcessEsResults(SearchByThreadGuid.builder()
                    .searchGuid(notesData.getThreadGuidParent().toString())
                    .includeVersions(false).includeArchived(true).build());
            if (existingEntry == null) {
                throw new RestStatusException(HttpStatus.SC_NOT_FOUND, String.format("Cannot create new response. No entry found for threadGuid=%s", 
                        notesData.getThreadGuidParent()));
            }
            NotesData existingEntryFirst = existingEntry.get(0);
            if (existingEntryFirst.getArchived() != null) {
                throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, 
                        String.format("Entry is archived cannot add thread. ExternalGuid=%s, EntryGuid=%s", 
                                existingEntryFirst.getExternalGuid(), existingEntryFirst.getEntryGuid()));
            }
            notesData.setExternalGuid(existingEntryFirst.getExternalGuid());
            log.debug("creating a thread for external guid={} and entry guid = {}", notesData.getExternalGuid().toString(), 
                    notesData.getEntryGuid().toString());
        } else {
            log.debug("creating a new entry for external guid = {}", notesData.getExternalGuid().toString());
        }
        NotesData newEntry = esNotesRepository.save(notesData);
        if(newEntry != null)
            log.debug("Successfully created a new entry entryGuid = {} ", newEntry.getEntryGuid());
        return newEntry;
    }

    /**
     * Search entry by key GUID
     * @param guid
     * @return Entry from elasticsearch for given guid (key)
     */
    @Override
    public NotesData getByGuid(UUID guid) {
        log.debug("{} guid: {}", LogUtil.method(), guid.toString());
        return esNotesRepository.findById(guid).orElse(null);
    }

    /**
     * Update entry by guid (key). if guid is not provided 
     * fetches recent entry for given entryGuid and updates it and create a new entry with updated content.
     * if entry is recently updated while this update is being made then throw an error asking for reload an entry and update again
     * @param updatedEntry
     * @return updated entry
     */
    @Override
    public NotesData updateByGuid(NotesData updatedEntry) {
        log.debug("{} externalGuid: {}, entryGuid: {}", LogUtil.method(), updatedEntry.getExternalGuid(), updatedEntry.getEntryGuid());
        NotesData  existingEntry;
        if (updatedEntry.getGuid() != null) {
            existingEntry = getByGuid(updatedEntry.getGuid());
            if(existingEntry == null) {
                throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"guid provided is not exists, cannot update");
            }
        } else {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"guid is not provided,provide");
        }
        return update(existingEntry,updatedEntry);
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
        NotesData  existingEntry;
        if (updatedEntry.getEntryGuid() != null) {
            List<NotesData> searchResult = iNotesOperations.fetchAndProcessEsResults(SearchByEntryGuid.builder()
                    .searchGuid(updatedEntry.getEntryGuid().toString())
                    .includeVersions(false).includeArchived(true).build());
            if(searchResult == null || searchResult.isEmpty()) {
                throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"entryGuid provided is not exists, cannot update");
            }
            existingEntry = searchResult.get(0);
        } else {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"entryGuid is not provided,provide");
        }
        return update(existingEntry,updatedEntry);
    }
    
    private NotesData update(NotesData existingEntry, NotesData updatedEntry) {
        log.debug("{}", LogUtil.method());
        if(updatedEntry.getCreated() == null) {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST, "createdDate of entry being updated should be provided");
        } else if(!updatedEntry.getCreated().equals(existingEntry.getCreated())) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Entry recently updated. please reload entry again and update");
        }
        if(existingEntry.getArchived() != null) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Entry is archived cannot be updated");
        }
        String currentContent = existingEntry.getContent();
        ESUtil.clearHistoryAndThreads(existingEntry);
        existingEntry.setGuid(UUID.randomUUID());
        existingEntry.setCreated(ESUtil.getCurrentDate());
        existingEntry.setContent(updatedEntry.getContent());
        NotesData updated = esNotesRepository.save(existingEntry);
        if(updated != null)
            log.debug("Successfully updated! externalGuid = {}, entryGuid ={}, changed content from ={} to ={}",
                    updatedEntry.getExternalGuid(), updatedEntry.getEntryGuid(), currentContent , updatedEntry.getContent());
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
        List<NotesData> processed = iNotesOperations.process(query,searchHitList.stream().iterator());
        Set<NotesData> flatten = new HashSet<>();
        try {
            ESUtil.flatten(processed,flatten);
            archive(flatten);
        } catch (Exception e) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR,"Error while archiving entries. error = " + e.getMessage());
        }
        AbstractQuery getArchived = ((AbstractQuery)query);
        getArchived.setIncludeArchived(true);
        return iNotesOperations.process(getArchived,getAllEntries(getArchived).stream().iterator());
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
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"Cannot archive. not entry found for given guid");
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
            entriesToArchive.forEach(entryToArchive -> {
                esNotesRepository.save(NotesData.builder()
                        .archived(dateTime)
                        .externalGuid(entryToArchive.getExternalGuid())
                        .entryGuid(entryToArchive.getEntryGuid())
                        .guid(entryToArchive.getGuid())
                        .threadGuid(entryToArchive.getThreadGuid())
                        .threadGuidParent(entryToArchive.getThreadGuidParent())
                        .content(entryToArchive.getContent())
                        .created(entryToArchive.getCreated()).build());
            });   
        } catch (Exception e) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR,"Error while archiving entries. error = " + e.getMessage()); 
        } finally {
            log.debug("Successfully archived! Nr of entries archived = {}", entriesToArchive.size());
        }
    }

}
