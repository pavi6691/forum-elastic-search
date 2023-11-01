package com.freelance.forum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.forum.elasticsearch.metadata.ResourceFileReaderService;
import com.freelance.forum.elasticsearch.esrepo.ESNotesRepository;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.generics.enums.Entries;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import com.freelance.forum.elasticsearch.generics.ISearchNotes;
import com.freelance.forum.elasticsearch.queries.SearchByEntryGuid;
import com.freelance.forum.elasticsearch.queries.SearchByThreadGuid;
import com.freelance.forum.elasticsearch.queries.generics.enums.EsNotesFields;
import com.freelance.forum.exceptions.FieldValidationException;
import com.freelance.forum.util.ESUtil;
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
    @Qualifier("searchNotesV3")
    @Autowired
    private ISearchNotes iSearchNotes;
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
            List<NotesData> existingEntry = iSearchNotes.search(new SearchByThreadGuid()
                    .setSearchBy(notesData.getThreadGuidParent().toString())
                    .setGetUpdateHistory(false).setGetArchived(true));
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
     * Update entry by guid (key). if guid is not provided, then looks for entryGUID in payload. 
     * fetches recent entry for given entryGuid and updates it and create a new entry with updated content
     * @param notesData
     * @return updated entry
     */
    @Override
    public NotesData update(NotesData notesData) {
        NotesData  notesDataToUpdate = null;
        if(notesData.getGuid() != null) {
            notesDataToUpdate = searchByGuid(notesData.getGuid());
            if(notesDataToUpdate == null) {
                throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"guid provided is not exists, cannot update");
            }
        } else if (notesData.getEntryGuid() != null) {
            List<NotesData> searchResult = iSearchNotes.search(new SearchByEntryGuid()
                    .setSearchBy(notesData.getEntryGuid().toString())
                    .setGetUpdateHistory(false).setGetArchived(true));
            if(searchResult == null || searchResult.isEmpty()) {
                throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"entryGuid provided is not exists, cannot update");
            }
            notesDataToUpdate = searchResult.get(0);
        } else {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"GUID is not provided,provide guid/entryGuid");
        }
        if(notesDataToUpdate.getArchived() != null) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Entry is archived cannot be updated");
        }
        ESUtil.clearHistoryAndThreads(notesDataToUpdate);
        notesDataToUpdate.setGuid(UUID.randomUUID());
        notesDataToUpdate.setCreated(ESUtil.getCurrentDate());
        notesDataToUpdate.setEntryGuid(notesData.getEntryGuid());
        notesDataToUpdate.setContent(notesData.getContent());
        return esNotesRepository.save(notesDataToUpdate);
    }

    /**
     * Archive by updating existing entry. updates archived field on elastic search with current data and time.
     * @param query - archive can be done by either externalGuid / entryGuid
     * @return archived entries
     */
    @Override
    public List<NotesData> archive(IQuery query) {
        List<NotesData> result = iSearchNotes.search(query);
        Set<NotesData> entriesToArchive = new HashSet<>();
        if(result != null && !result.isEmpty())
            ESUtil.flatten(result.get(0),entriesToArchive);
        if(result == null || entriesToArchive.isEmpty()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"Cannot archive. not entries found");
        }
        List<NotesData> response = new ArrayList<>();
        entriesToArchive.forEach(entryToArchive -> {
            entryToArchive.setArchived(ESUtil.getCurrentDate());
            ESUtil.clearHistoryAndThreads(entryToArchive);// clean up as threads and history will be also stored in es
            esNotesRepository.save(entryToArchive);
            response.add(entryToArchive);
        });
        return iSearchNotes.search(query);
    }

    /**
     * Deletes entries by either externalGuid/entryGuid
     * @param query - search entries for given externalGuid/entryGuid
     * @param entries - {@link Entries#ALL} to delete all entries, {@link Entries#ARCHIVED} to delete only archived entries
     * @return deleted entries
     */
    @Override
    public List<NotesData> delete(IQuery query, Entries entries) {
        List<NotesData> esResults = iSearchNotes.search(query);
        if(esResults != null && !esResults.isEmpty()) {
            Set<NotesData> entriesToDelete = new HashSet<>();
            esResults.stream().forEach(e -> ESUtil.flatten(e, entriesToDelete));
            try {
                entriesToDelete.forEach(e -> {
                    if (Entries.ALL == entries || ((Entries.ARCHIVED == entries) && e.getArchived() != null)) {
                        esNotesRepository.deleteById(e.getGuid());
                    }
                });
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
        return iSearchNotes.search(query);
    }
}
