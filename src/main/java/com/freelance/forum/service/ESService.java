package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.configuration.*;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.elasticsearch.repository.ESRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ESService {

    @Autowired
    ResourceFileReaderService resourceFileReaderService;

    @Autowired
    EsConfig esConfig;

    @Autowired
    ESRepository esRepository;

    @Value("${index.name}")
    private String indexName;

    @Value("${max.number.of.history.and.threads}")
    private int max_number_of_history_and_threads;

    public NotesData saveNew(NotesData notesData) {
        if(notesData.getGuid() == null) {
            notesData.setGuid(UUID.randomUUID());
        }
        notesData.setEntryGuid(UUID.randomUUID());
        notesData.setThreadGuid(UUID.randomUUID());
        notesData.setCreated(getCurrentDate());
        if(notesData.getThreadGuidParent() != null) {
            // It's a thread that needs to created
            NotesData existingEntry = 
                    search(String.format(Queries.QUERY_ALL_ENTRIES, ESIndexNotesFields.THREAD.getEsFieldName(), notesData.getThreadGuidParent(),0),
                            false,false,false,SortOrder.ASC);
            if(existingEntry == null) {
                throw new RestStatusException(HttpStatus.SC_NOT_FOUND,String.format("Cannot create new thread. No entry found for threadGuid=%s",
                        notesData.getThreadGuidParent()));
            } else if(existingEntry.getArchived() != null) {
                throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, 
                        String.format("Entry is archived cannot add thread. ExternalGuid=%s, EntryGuid=%s",existingEntry.getExternalGuid(),
                                existingEntry.getEntryGuid()));
            } else {
                notesData.setExternalGuid(existingEntry.getExternalGuid());
            }
        }
        return esRepository.save(notesData);
    }

    public NotesData searchByGuid(UUID guid) {
        return esRepository.findById(guid).orElse(null);
    }

    /**
     * To Update 
     *   - provide GUID that's being updated
     *   - provide entryGUID in payload. - same entryGUID is used in new version of updated entry
     *   - So to get history, we can query by entryGUID
     * @param notesData
     * @return
     */
    public NotesData update(NotesData notesData, ESIndexNotesFields esIndexNotesFields) {
        NotesData notesDataToUpdate = null;
        if(esIndexNotesFields == ESIndexNotesFields.ENTRY ){
            if(notesData.getEntryGuid() == null) {
                throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"entryGuid is not provided");
            } else {
                notesDataToUpdate = search(String.format(Queries.QUERY_ALL_ENTRIES, esIndexNotesFields.getEsFieldName(), notesData.getEntryGuid(),0),
                        false,false,false,SortOrder.ASC);
            }
        } else if(esIndexNotesFields == ESIndexNotesFields.GUID && notesData.getGuid() == null) {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"guid is not provided");
        } else {
            notesDataToUpdate = searchByGuid(notesData.getGuid());
        }
        if(notesDataToUpdate == null) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND, String.format("Cannot update. %s not found.",esIndexNotesFields.getEsFieldName()));
        } else if(notesDataToUpdate.getArchived() != null) {
            throw new RestStatusException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Entry is archived cannot be updated");
        }
        notesDataToUpdate.setGuid(UUID.randomUUID());
        notesDataToUpdate.setCreated(getCurrentDate());
        notesDataToUpdate.setEntryGuid(notesData.getEntryGuid());
        notesDataToUpdate.setContent(notesData.getContent());
        return esRepository.save(notesDataToUpdate);
    }

    public List<NotesData> searchEntries(String searchString, ESIndexNotesFields esIndexNotesFields, boolean getUpdateHistory, 
                                                 boolean getArchivedResponse, SortOrder sortOrder) {
        String query = null;
        if(esIndexNotesFields == ESIndexNotesFields.EXTERNAL) {
            query = String.format(Queries.QUERY_ROOT_EXTERNAL_ENTRIES, searchString);
        } else if(esIndexNotesFields == ESIndexNotesFields.CONTENT) {
            query = String.format(Queries.QUERY_CONTENT_ROOT_EXTERNAL_ENTRIES, searchString);
        }
        List<NotesData> results = new ArrayList<>();
        Set<String> doNotSearchFurtherForHistory = new HashSet<>();
        if(query != null) {
            Iterator<SearchHit> searchResponseIterator = getSearchResponse(query, sortOrder);
            if (searchResponseIterator != null) {
                while (searchResponseIterator.hasNext()) {
                    NotesData entry = NotesData.fromJson(searchResponseIterator.next().getSourceAsString());
                    if (entry != null && (getArchivedResponse || entry.getArchived() == null)/*do not search archived threads*/) {
                        if(!doNotSearchFurtherForHistory.contains(entry.getEntryGuid().toString())) {
                            if (esIndexNotesFields == ESIndexNotesFields.EXTERNAL) {
                                results.add(searchThreadsAndHistories(entry, new HashSet<>(), getUpdateHistory, getArchivedResponse));
                            } else if (esIndexNotesFields == ESIndexNotesFields.CONTENT) {
                                results.add(searchThreadsAndHistoriesForContent(searchString, entry, new HashSet<>(), getUpdateHistory, getArchivedResponse));
                            }
                            doNotSearchFurtherForHistory.add(entry.getEntryGuid().toString());
                        } 
                    }
                }
            }
        }
        return results;
    }


    public NotesData searchEntries(String guid, ESIndexNotesFields esIndexNotesFields, boolean getUpdateHistory, boolean getArchivedResponse,
                                   boolean searchThreads, SortOrder sortOrder) {
       NotesData rootEntry = search(String.format(Queries.QUERY_ALL_ENTRIES, esIndexNotesFields.getEsFieldName(),guid,0),
               getUpdateHistory,getArchivedResponse,searchThreads,sortOrder);
        if(rootEntry == null || (!getArchivedResponse && rootEntry.getArchived() != null)) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND, String.format("No entries found for field. %s = %s",
                    esIndexNotesFields.getEsFieldName(),guid));
        }
        return rootEntry;
    }

    /**
     * Query by GUID. Supported GUIDs to query are
     *  1. externalGuid
     *  2. entryGuid
     * @param query
     * @param getUpdateHistory
     * @param getArchivedResponse
     * @param searchThreads
     * @param sortOrder
     * @return
     */
    private NotesData search(String query, boolean getUpdateHistory, boolean getArchivedResponse, boolean searchThreads, SortOrder sortOrder) {
        Iterator<SearchHit>  searchResponseIterator = getSearchResponse(query, sortOrder);
        NotesData rootEntry = null;
        if(searchResponseIterator != null) {
            while (searchResponseIterator.hasNext()) {
                rootEntry = NotesData.fromJson(searchResponseIterator.next().getSourceAsString());
                if (rootEntry.getThreadGuidParent() == null) {
                    break; // this is an external root entry, when updated 
                }
            }
            if(searchThreads && (rootEntry != null && (getArchivedResponse || rootEntry.getArchived() == null))/*do not search archived threads*/) {
                searchThreadsAndHistories(rootEntry, new HashSet<>(), getUpdateHistory, getArchivedResponse);
            }
        }
        return rootEntry;
    }

    public List<NotesData> archive(String guid, ESIndexNotesFields esIndexNotesFields) {
        NotesData result = search(String.format(Queries.QUERY_ALL_ENTRIES, esIndexNotesFields.getEsFieldName(), guid,0),
                true,false,true,getSortOrder(esIndexNotesFields));
        Set<NotesData> entriesToArchive = new HashSet<>();
        flatten(result,entriesToArchive);
        if(result == null || entriesToArchive.isEmpty()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,String.format("Cannot archive. not entries found. %s = %s",
                    esIndexNotesFields.getEsFieldName(),guid));
        }
        List<NotesData> response = new ArrayList<>();
        entriesToArchive.forEach(entryToArchive -> {
            entryToArchive.setArchived(getCurrentDate());
            clearHistoryAndThreads(entryToArchive);// clean up as threads and history will be also stored in es
            esRepository.save(entryToArchive);
            response.add(entryToArchive);
        });
        return response;
    }

    public NotesData searchArchive(String guid, ESIndexNotesFields esIndexNotesFields) {
        NotesData result =  search(String.format(Queries.QUERY_ALL_ENTRIES, esIndexNotesFields.getEsFieldName(), guid,0),
                true, true, true, getSortOrder(esIndexNotesFields));
        if(result != null) {
            while (result.getArchived() == null && !result.getThreads().isEmpty()) {
                result = result.getThreads().get(0);
            }
        }
        if(result.getArchived() != null) {
            return result;
        } else {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND, String.format("No archived entries found. %s = %s",
                    esIndexNotesFields.getEsFieldName(),guid));   
        }
    }
    
    public List<NotesData> delete(String guid, ESIndexNotesFields esIndexNotesFields, String deleteEntries) {
        NotesData result =  search(String.format(Queries.QUERY_ALL_ENTRIES, esIndexNotesFields.getEsFieldName(), guid,0),
                true, true, true, getSortOrder(esIndexNotesFields));
        List<NotesData> results = new ArrayList<>();
        if(result != null) {
            Set<NotesData> entriesToDelete = new HashSet<>();
            flatten(result, entriesToDelete);
            entriesToDelete.forEach(e -> {
                if (StringUtils.equalsAnyIgnoreCase(Constants.DELETE_ALL, deleteEntries) ||
                        (StringUtils.equalsAnyIgnoreCase(Constants.DELETE_ONLY_ARCHIVED, deleteEntries) && e.getArchived() != null)) {
                    esRepository.deleteById(e.getGuid());
                    clearHistoryAndThreads(e);
                    results.add(e);
                }
            });
        }
        if(result == null || results.isEmpty()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,String.format("No entries found to delete. %s = %s",
                    esIndexNotesFields.getEsFieldName(),guid));
        }
        return results;
    }

    public String createIndex(String indexName) {
        try {
            IndexMetadataConfiguration indexMetadataConfiguration =
                    resourceFileReaderService.getDocsPropertyFile(Constants.APPLICATION_YAML,this.getClass());
//            Template template = resourceFileReaderService.getTemplateFile(Constants.NOTE_V1_INDEX_TEMPLATE,this.getClass());
            String mapping  = resourceFileReaderService.getMappingFromFile(Constants.NOTE_V1_INDEX_MAPPINGS,this.getClass());
//            PolicyInfo policyInfo  = resourceFileReaderService.getPolicyFile(Constants.NOTE_V1_INDEX_POLICY,this.getClass());
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.source(mapping, XContentType.JSON);
            CreateIndexResponse createIndexResponse = esConfig.elasticsearchClient().indices().create(request, RequestOptions.DEFAULT);
            return createIndexResponse.index();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void flatten(NotesData root, Set<NotesData> entries) {
        entries.add(root);
        root.getThreads().forEach(e -> flatten(e,entries));
        root.getHistory().forEach(e -> flatten(e,entries));
    }
 
    private Iterator<SearchHit> getSearchResponse(String query,SortOrder sortOrder) {
        SearchRequest searchRequest = getQueryRequest(query,sortOrder);
        try {
            //Currently request options are default, we can configure based on requirements
            SearchResponse searchResponse = esConfig.elasticsearchClient().search(searchRequest, RequestOptions.DEFAULT);
            Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
            return iterator;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SearchRequest getQueryRequest(String query, SortOrder sortOrder) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.wrapperQuery(query));
        searchSourceBuilder.sort(ESIndexNotesFields.CREATED.getEsFieldName(),sortOrder);
        searchSourceBuilder.size(max_number_of_history_and_threads);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
    
    private NotesData searchThreadsAndHistories(NotesData threadRoot, Set<String> entryThreadUuid, boolean getUpdateHistory,
                                       boolean getArchivedResponse) {
        checkAndAddHistory(threadRoot,getUpdateHistory);
        Iterator<SearchHit> searchResponseIterator = getSearchResponse(String.format(Queries.QUERY_ALL_ENTRIES, ESIndexNotesFields.PARENT_THREAD.getEsFieldName(),
                threadRoot.getThreadGuid(),0), SortOrder.DESC);
        while(searchResponseIterator.hasNext()) {
            NotesData thread = NotesData.fromJson(searchResponseIterator.next().getSourceAsString());
            // below if to make sure to avoid history entries here as search Entry id will have history entries as well
            if(!entryThreadUuid.contains(thread.getEntryGuid().toString())) { 
                if(!getArchivedResponse && thread.getArchived() != null) {
                    break; // do not search archived thread
                }
                threadRoot.addThreads(thread);
                entryThreadUuid.add(thread.getEntryGuid().toString());
                searchThreadsAndHistories(thread,entryThreadUuid,getUpdateHistory, getArchivedResponse);
            }
        }
        return threadRoot;
    }

    private NotesData searchThreadsAndHistoriesForContent(String contentSearch, NotesData threadRoot, Set<String> entryThreadUuid, 
                                                          boolean getUpdateHistory, boolean getArchivedResponse) {
        checkAndAddHistoryForContent(contentSearch,threadRoot,getUpdateHistory);
        String query = String.format(Queries.QUERY_CONTENT_ENTRIES, ESIndexNotesFields.PARENT_THREAD.getEsFieldName(), 
                threadRoot.getThreadGuid(),contentSearch);
        Iterator<SearchHit> searchResponseIterator = getSearchResponse(query, SortOrder.DESC);
        while(searchResponseIterator.hasNext()) {
            NotesData thread = NotesData.fromJson(searchResponseIterator.next().getSourceAsString());
            // below if to make sure to avoid history entries here as search Entry id will have history entries as well
            if(!entryThreadUuid.contains(thread.getEntryGuid().toString())) {
                if(!getArchivedResponse && thread.getArchived() != null) {
                    break; // do not search archived thread
                }
                threadRoot.addThreads(thread);
                entryThreadUuid.add(thread.getEntryGuid().toString());
                searchThreadsAndHistoriesForContent(contentSearch,thread,entryThreadUuid,getUpdateHistory, getArchivedResponse);
            }
        }
        return threadRoot;
    }

    private void checkAndAddHistory(NotesData entry, boolean getUpdateHistory) {
        if(getUpdateHistory && entry != null) {
            Iterator<SearchHit> historyIterator = getSearchResponse(String.format(Queries.QUERY_ALL_ENTRIES,
                    ESIndexNotesFields.ENTRY.getEsFieldName(),entry.getEntryGuid(),0), SortOrder.DESC);
            if(historyIterator.hasNext()) {
                historyIterator.next();
            }
            while(historyIterator.hasNext()) {
                NotesData history = NotesData.fromJson(historyIterator.next().getSourceAsString());
                entry.addHistory(history);
            }
        }
    }

    private void checkAndAddHistoryForContent(String contentSearch,NotesData entry, boolean getUpdateHistory) {
        if(getUpdateHistory && entry != null) {
            Iterator<SearchHit> historyIterator = getSearchResponse(String.format(Queries.QUERY_CONTENT_ENTRIES,
                    ESIndexNotesFields.ENTRY.getEsFieldName(),entry.getEntryGuid(),contentSearch), SortOrder.DESC);
            if(historyIterator.hasNext()) {
                historyIterator.next();
            }
            while(historyIterator.hasNext()) {
                NotesData history = NotesData.fromJson(historyIterator.next().getSourceAsString());
                entry.addHistory(history);
            }
        }
    }
    
    private Date getCurrentDate() {
        return new Date();
    }

    private void clearHistoryAndThreads(NotesData entry) {
        if(entry.getThreads() != null) {
            entry.getThreads().clear();
        }
        if(entry.getHistory() != null) {
            entry.getHistory().clear();
        }
    }

    private SortOrder getSortOrder(ESIndexNotesFields esIndexNotesFields){
        if(ESIndexNotesFields.EXTERNAL == esIndexNotesFields) {
            return SortOrder.DESC; // DESC to get latest updated entry with threadParentGuid null and that entry will be root
        } else {
            return SortOrder.ASC;
        }
    }
}