package com.freelance.forum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.forum.elasticsearch.configuration.Constants;
import com.freelance.forum.elasticsearch.configuration.EsConfig;
import com.freelance.forum.elasticsearch.configuration.IndexMetadataConfiguration;
import com.freelance.forum.elasticsearch.configuration.ResourceFileReaderService;
import com.freelance.forum.elasticsearch.esrepo.ESNotesRepository;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.IQuery;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.elasticsearch.generics.ISearchNotes;
import com.freelance.forum.util.ESUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

@Service
public class ESNotesService implements INotesService {
    @Qualifier("searchNotesV2")
    @Autowired
    private ISearchNotes iSearchNotes;
    @Autowired
    private ResourceFileReaderService resourceFileReaderService;
    private ESNotesRepository esNotesRepository;
    private ElasticsearchOperations elasticsearchOperations;
    private EsConfig esConfig;
    public ESNotesService(ESNotesRepository esNotesRepository, ElasticsearchOperations elasticsearchOperations, EsConfig esConfig) {
        this.esNotesRepository = esNotesRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.esConfig = esConfig;
    }
    
    @Override
    public NotesData saveNew(NotesData notesData) {
        notesData.setGuid(UUID.randomUUID());
        notesData.setEntryGuid(UUID.randomUUID());
        notesData.setThreadGuid(UUID.randomUUID());
        notesData.setCreated(ESUtil.getCurrentDate());
        if(notesData.getThreadGuidParent() != null) {
            // It's a thread that needs to created
            List<NotesData> existingEntry = iSearchNotes.search(new Queries.SearchByEntryGuid()
                    .setEntryGuid(notesData.getThreadGuidParent().toString())
                    .setSearchField(ESIndexNotesFields.THREAD)
                    .setGetUpdateHistory(false).setGetArchived(false));
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
    @Override
    public NotesData searchByGuid(UUID guid) {
        return esNotesRepository.findById(guid).orElse(null);
    }

    /**
     * To Update 
     *   - provide GUID that's being updated
     *   - provide entryGUID in payload. - same entryGUID is used in new version of updated entry
     *   - So to get history, we can query by entryGUID
     * @param notesData
     * @return
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
            List<NotesData> searchResult = iSearchNotes.search(new Queries.SearchByEntryGuid()
                    .setEntryGuid(notesData.getEntryGuid().toString())
                    .setSearchField(ESIndexNotesFields.ENTRY)
                    .setGetUpdateHistory(false).setGetArchived(false));
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
    @Override
    public List<NotesData> delete(IQuery query, String deleteEntries) {
        List<NotesData> result = iSearchNotes.search(query);
        List<NotesData> results = new ArrayList<>();
        if(result != null && !result.isEmpty()) {
            Set<NotesData> entriesToDelete = new HashSet<>();
            ESUtil.flatten(result.get(0), entriesToDelete);
            entriesToDelete.forEach(e -> {
                if (StringUtils.equalsAnyIgnoreCase(Constants.DELETE_ALL, deleteEntries) ||
                        (StringUtils.equalsAnyIgnoreCase(Constants.DELETE_ONLY_ARCHIVED, deleteEntries) && e.getArchived() != null)) {
                    esNotesRepository.deleteById(e.getGuid());
                    ESUtil.clearHistoryAndThreads(e);
                    results.add(e);
                }
            });
        }
        if(result == null || results.isEmpty()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"No entries found to delete");
        }
        return results;
    }

    @Override
    public String createIndex(String indexName) {
        try {
//            IndexMetadataConfiguration indexMetadataConfiguration =
//                    resourceFileReaderService.getDocsPropertyFile(Constants.APPLICATION_YAML,this.getClass());
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
