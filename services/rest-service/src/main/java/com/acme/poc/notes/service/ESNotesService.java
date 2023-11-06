package com.acme.poc.notes.service;

import com.acme.poc.notes.elasticsearch.esrepo.ESNotesRepository;
import com.acme.poc.notes.elasticsearch.generics.INotesOperations;
import com.acme.poc.notes.elasticsearch.metadata.ResourceFileReaderService;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.elasticsearch.queries.SearchByThreadGuid;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.util.ESUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * Service to perform elastic search operations
 */
@Service
public class ESNotesService implements INotesService {
    @Qualifier("notesProcessorV3")
    @Autowired
    private INotesOperations iNotesOperations;
    @Autowired
    private ResourceFileReaderService resourceFileReaderService;
    private ESNotesRepository esNotesRepository;
    private ElasticsearchOperations elasticsearchOperations;

    static Logger log = LogManager.getLogger(ESNotesService.class);
    
    public ESNotesService(ESNotesRepository esNotesRepository, ElasticsearchOperations elasticsearchOperations) {
        this.esNotesRepository = esNotesRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * creates new entry, or a thread if threadGuidParent is provided
     * @param notesData
     * @return Entry that's created and stored in elastic search
     */
    @Override
    public NotesData saveNew(NotesData notesData) {
        notesData.setGuid(UUID.randomUUID());
        notesData.setEntryGuid(UUID.randomUUID());
        notesData.setThreadGuid(UUID.randomUUID());
        notesData.setCreated(ESUtil.getCurrentDate());
        if(notesData.getThreadGuidParent() != null) {
            // It's a thread that needs to created
            List<NotesData> existingEntry = iNotesOperations.search(SearchByThreadGuid.builder()
                    .searchGuid(notesData.getThreadGuidParent().toString())
                    .getUpdateHistory(false).getArchived(true).build());
            if(existingEntry == null && !existingEntry.isEmpty()) {
                throw new RestStatusException(HttpStatus.SC_NOT_FOUND,String.format("Cannot create new thread. No entry found for threadGuid=%s",
                        notesData.getThreadGuidParent()));
            } else if(existingEntry.get(0).getArchived() != null) {
                throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        String.format("Entry is archived cannot add thread. ExternalGuid=%s, EntryGuid=%s",
                                existingEntry.get(0).getExternalGuid(), existingEntry.get(0).getEntryGuid()));
            } else {
                notesData.setExternalGuid(existingEntry.get(0).getExternalGuid());
            }
        }
        return esNotesRepository.save(notesData);
    }

    /**
     * Search entry by key GUID
     * @param guid
     * @return Entry from elasticsearch for given guid (key)
     */
    @Override
    public NotesData searchByGuid(UUID guid) {
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
        NotesData  existingEntry;
        if (updatedEntry.getGuid() != null) {
            existingEntry = searchByGuid(updatedEntry.getGuid());
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
        NotesData  existingEntry;
        if (updatedEntry.getEntryGuid() != null) {
            List<NotesData> searchResult = iNotesOperations.search(SearchByEntryGuid.builder()
                    .searchGuid(updatedEntry.getEntryGuid().toString())
                    .getUpdateHistory(false).getArchived(true).build());
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
        if(updatedEntry.getCreated() == null) {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST, "createdDate of entry being updated should be provided");
        } else if(!updatedEntry.getCreated().equals(existingEntry.getCreated())) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Entry recently updated. please reload entry again and update");
        }
        if(existingEntry.getArchived() != null) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Entry is archived cannot be updated");
        }
        ESUtil.clearHistoryAndThreads(existingEntry);
        existingEntry.setGuid(UUID.randomUUID());
        existingEntry.setCreated(ESUtil.getCurrentDate());
        existingEntry.setContent(updatedEntry.getContent());
        return esNotesRepository.save(existingEntry);
    }

    /**
     * Archive by updating existing entry. updates archived field on elastic search with current date and time.
     * @param query - archive is done querying by either externalGuid / entryGuid
     * @return archived entries
     */
    @Override
    public List<NotesData> archive(IQuery query) {
        List<NotesData> result = iNotesOperations.search(query);
        Set<NotesData> entriesToArchive = new HashSet<>();
        if(result != null && !result.isEmpty())
            ESUtil.flatten(result.get(0),entriesToArchive);
        if(result == null || entriesToArchive.isEmpty()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"Cannot archive. not entries found");
        }
        update(entriesToArchive);
        return iNotesOperations.search(query);
    }

    /**
     * Archive by updating existing entry. updates archived field on elastic search with current date and time.
     * @param guid - archive is done querying by guid
     * @return archived entries
     */
    @Override
    public List<NotesData> archive(UUID guid) {
        Optional<NotesData> result = esNotesRepository.findById(guid);
        if (!result.isPresent()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"Cannot archive. not entry found for given guid");
        }
        update(Set.of(result.get()));
        return List.of(esNotesRepository.findById(guid).orElse(null));
    }


    // TODO when there are more than default number of records to archive? 1000 is max size
    private void update(Set<NotesData> entriesToArchive) {
        Date dateTime = ESUtil.getCurrentDate();
        entriesToArchive.forEach(entryToArchive -> {
            entryToArchive.setArchived(dateTime);
            ESUtil.clearHistoryAndThreads(entryToArchive);// clean up as threads and history will be also stored in es
            esNotesRepository.save(entryToArchive);
        });
    }

    /**
     * Deletes entries by either externalGuid/entryGuid
     * @param query - search entries for given externalGuid/entryGuid
     * @return deleted entries
     */
    // TODO when there are more than default number of records to delete? 1000 is max size
    @Override
    public List<NotesData> delete(IQuery query) {
        List<NotesData> esResults = iNotesOperations.search(query);
        if(esResults != null && !esResults.isEmpty()) {
            Set<NotesData> entriesToDelete = new HashSet<>();
            esResults.stream().forEach(e -> ESUtil.flatten(e, entriesToDelete));
            try {
                esNotesRepository.deleteAll(entriesToDelete);
            } catch (Exception e) {
                throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR,"Error while deleting entries. error=" + e.getMessage());
            }
        }
        if(esResults == null) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"No entries found to delete");
        }
        return esResults;
    }

    @Override
    public NotesData delete(String keyGuid) {
        UUID guid = UUID.fromString(keyGuid);
        NotesData notesData = esNotesRepository.findById(guid).orElse(null);
        if(notesData != null) {
            esNotesRepository.deleteById(guid);
            if (!esNotesRepository.findById(guid).isPresent()) {
                return notesData;
            }
        } 
        throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"cannot delete. No entry found for given GUID");
    }

    /**
     * Create new index if not exists
     * @param indexName - index name to create
     * @return indexName when successfully created, else message that says index already exists
     */
    @Override
    public String createIndex(String indexName) {
        try {
// TODO
//            IndexMetadataConfiguration indexMetadataConfiguration =
//            resourceFileReaderService.getDocsPropertyFile(Constants.APPLICATION_YAML,this.getClass());
//            Template template = resourceFileReaderService.getTemplateFile(Constants.NOTE_V1_INDEX_TEMPLATE,this.getClass());
//            String mapping  = resourceFileReaderService.getMappingFromFile(Constants.NOTE_V1_INDEX_MAPPINGS,this.getClass());
//            PolicyInfo policyInfo  = resourceFileReaderService.getPolicyFile(Constants.NOTE_V1_INDEX_POLICY,this.getClass());
            IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
            if(!indexOperations.exists()) {
                indexOperations.create();
                return new ObjectMapper().writeValueAsString(indexOperations.getInformation());
            } else {
                System.err.println(String.format("Index already exists, indexName=%s", indexName));
                return String.format("Index already exists, indexName=%s", indexName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<NotesData> search(IQuery query) {
        return iNotesOperations.search(query);
    }
}
