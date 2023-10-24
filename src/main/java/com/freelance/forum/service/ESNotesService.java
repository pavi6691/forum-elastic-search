package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.configuration.Constants;
import com.freelance.forum.elasticsearch.configuration.EsConfig;
import com.freelance.forum.elasticsearch.configuration.IndexMetadataConfiguration;
import com.freelance.forum.elasticsearch.configuration.ResourceFileReaderService;
import com.freelance.forum.elasticsearch.esrepo.ESNotesRepository;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.SearchRequest;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.RequestType;
import com.freelance.forum.service.generics.ISearchNotesService;
import com.freelance.forum.util.ESUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ESNotesService implements INotesService {

    @Autowired
    ResourceFileReaderService resourceFileReaderService;
    
    @Autowired
    EsConfig esConfig;

    @Autowired
    ESNotesRepository esNotesRepository;

    @Autowired
    @Qualifier("searchNotesServiceV2")
    ISearchNotesService iSearchNotesService;
    

    @Override
    public NotesData saveNew(NotesData notesData) {
        notesData.setGuid(UUID.randomUUID());
        notesData.setEntryGuid(UUID.randomUUID());
        notesData.setThreadGuid(UUID.randomUUID());
        notesData.setCreated(ESUtil.getCurrentDate());
        if(notesData.getThreadGuidParent() != null) {
            // It's a thread that needs to created
            List<NotesData> existingEntry = iSearchNotesService.search(new SearchRequest.Builder()
                    .setSearch(notesData.getThreadGuidParent().toString())
                    .setSearchField(ESIndexNotesFields.THREAD).setSearchHistory(false)
                    .setRequestType(RequestType.ENTRIES)
                    .setSearchArchived(false).setSortOrder(SortOrder.DESC).build());
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
    public NotesData update(NotesData notesData, SearchRequest searchRequest) {
        List<NotesData>  notesDataToUpdate = null;
        if(searchRequest.getSearchField() == ESIndexNotesFields.ENTRY ){
            if(notesData.getEntryGuid() == null) {
                throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"entryGuid is not provided");
            } else {
                notesDataToUpdate = iSearchNotesService.search(searchRequest);
            }
        } else if(searchRequest.getSearchField() == ESIndexNotesFields.GUID && notesData.getGuid() == null) {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"guid is not provided");
        } else {
            NotesData notesData1 = searchByGuid(notesData.getGuid());
            if(notesData1 != null)
                notesDataToUpdate = List.of(notesData1);
        }
        if(notesDataToUpdate == null || notesDataToUpdate.isEmpty()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND, String.format("Cannot update. %s not found.",
                    searchRequest.getSearchField().getEsFieldName()));
        } else if(notesDataToUpdate.get(0).getArchived() != null) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Entry is archived cannot be updated");
        }
        notesDataToUpdate.get(0).setGuid(UUID.randomUUID());
        notesDataToUpdate.get(0).setCreated(ESUtil.getCurrentDate());
        notesDataToUpdate.get(0).setEntryGuid(notesData.getEntryGuid());
        notesDataToUpdate.get(0).setContent(notesData.getContent());
        return esNotesRepository.save(notesDataToUpdate.get(0));
    }
    @Override
    public List<NotesData> archive(SearchRequest searchRequest) {
        List<NotesData> result = iSearchNotesService.search(searchRequest);
        Set<NotesData> entriesToArchive = new HashSet<>();
        if(result != null && !result.isEmpty())
            ESUtil.flatten(result.get(0),entriesToArchive);
        if(result == null || entriesToArchive.isEmpty()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,String.format("Cannot archive. not entries found. %s = %s",
                    searchRequest.getSearchField().getEsFieldName(),searchRequest.getSearch()));
        }
        List<NotesData> response = new ArrayList<>();
        entriesToArchive.forEach(entryToArchive -> {
            entryToArchive.setArchived(ESUtil.getCurrentDate());
            ESUtil.clearHistoryAndThreads(entryToArchive);// clean up as threads and history will be also stored in es
            esNotesRepository.save(entryToArchive);
            response.add(entryToArchive);
        });
        return iSearchNotesService.search(new SearchRequest.Builder().setSearch(searchRequest.getSearch())
                .setSearchField(ESIndexNotesFields.THREAD).setSearchHistory(false)
                .setSearchArchived(false).setSortOrder(ESUtil.getSortOrder(searchRequest.getSearchField())).build());
    }
    @Override
    public List<NotesData> delete(SearchRequest searchRequest, String deleteEntries) {
        List<NotesData> result = iSearchNotesService.search(searchRequest);
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
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,String.format("No entries found to delete. %s = %s",
                    searchRequest.getSearchField().getEsFieldName(),searchRequest.getSearch()));
        }
        return results;
    }

    @Override
    public String createIndex(String indexName) {
        try {
            IndexMetadataConfiguration indexMetadataConfiguration =
                    resourceFileReaderService.getDocsPropertyFile(Constants.APPLICATION_YAML,this.getClass());
//            Template template = resourceFileReaderService.getTemplateFile(Constants.NOTE_V1_INDEX_TEMPLATE,this.getClass());
            String mapping  = resourceFileReaderService.getMappingFromFile(Constants.NOTE_V1_INDEX_MAPPINGS,this.getClass());
//            PolicyInfo policyInfo  = resourceFileReaderService.getPolicyFile(Constants.NOTE_V1_INDEX_POLICY,this.getClass());
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.source(mapping, XContentType.JSON);
            // TODO create index using repo
            CreateIndexResponse createIndexResponse = esConfig.elasticsearchClient().indices().create(request, RequestOptions.DEFAULT);
            return createIndexResponse.index();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<NotesData> search(SearchRequest searchRequest) {
        return iSearchNotesService.search(searchRequest);
    }
}
