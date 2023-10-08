package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.configuration.*;
import com.freelance.forum.elasticsearch.pojo.ElasticDataModel;
import com.freelance.forum.elasticsearch.queries.ESIndexFields;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.elasticsearch.repository.ESRepository;
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
import org.springframework.util.StringUtils;

import java.io.IOException;
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

    @Value("${max.number.of.results}")
    private int max_number_of_results;

    public ElasticDataModel saveNew(ElasticDataModel elasticDataModel) {
        if(StringUtils.isEmpty(elasticDataModel.getGuid())) {
            elasticDataModel.setGuid(UUID.randomUUID().toString());
        }
        elasticDataModel.setEntryGuid(UUID.randomUUID().toString());
        elasticDataModel.setThreadGuid(UUID.randomUUID().toString());
        elasticDataModel.setCreated(getCurrentDate());
        if(StringUtils.isEmpty(elasticDataModel.getThreadGuidParent())) {
            elasticDataModel.setThreadGuidParent(null);
        } else {
            ElasticDataModel existingEntry = 
                    search(String.format(Queries.QUERY_BY_GUID, ESIndexFields.THREAD.getEsFieldName(), elasticDataModel.getThreadGuidParent()),
                            false,false,false,SortOrder.ASC);
            if(existingEntry != null) {
                elasticDataModel.setExternalGuid(existingEntry.getExternalGuid());
            } else {
                throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"Entry with this thread guid is not present");
            }
        }
        return esRepository.save(elasticDataModel);
    }

    public ElasticDataModel searchByGuid(String guid) {
        return esRepository.findById(guid).orElse(null);
    }

    /**
     * To Update 
     *   - provide GUID that's being updated
     *   - provide entryGUID in payload. - same entryGUID is used in new version of updated entry
     *   - So to get history, we can query by entryGUID
     * @param elasticDataModel
     * @return
     */
    public ElasticDataModel update(ElasticDataModel elasticDataModel, ESIndexFields esIndexFields) {
        List<ElasticDataModel> results = new ArrayList<>();
        ElasticDataModel elasticDataModelToUpdate = null;
        if(esIndexFields == ESIndexFields.ENTRY ){
            if(StringUtils.isEmpty(elasticDataModel.getEntryGuid())) {
                throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"entryGuid is not provided");
            } else {
                elasticDataModelToUpdate = search(String.format(Queries.QUERY_BY_GUID, esIndexFields.getEsFieldName(), elasticDataModel.getEntryGuid()),
                        false,false,false,SortOrder.ASC);
            }
        } else if(esIndexFields == ESIndexFields.GUID && StringUtils.isEmpty(elasticDataModel.getGuid())) {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"GUID is not provided");
        } else {
            elasticDataModelToUpdate = searchByGuid(elasticDataModel.getGuid());
        }
        if(elasticDataModelToUpdate== null) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND, "GUID provided is not exists");
        }
        elasticDataModelToUpdate.setGuid(UUID.randomUUID().toString());
        elasticDataModelToUpdate.setCreated(getCurrentDate());
        elasticDataModelToUpdate.setEntryGuid(elasticDataModel.getEntryGuid());
        elasticDataModelToUpdate.setContent(elasticDataModel.getContent());
        return esRepository.save(elasticDataModelToUpdate);
    }

    /**
     * Query by GUID. Supported GUIDs to query are
     *  1. externalGuid
     *  2. entryGuid
     * @param query
     * @param searchUpdateHistory
     * @param getArchivedResponse
     * @param searchResponses
     * @param sortOrder
     * @return
     */
    public ElasticDataModel search(String query,boolean searchUpdateHistory,boolean getArchivedResponse,boolean searchResponses,SortOrder sortOrder) {
        Iterator<SearchHit> searchResponseIterator = null;
        searchResponseIterator = getSearchResponse(query, sortOrder);
        ElasticDataModel rootEntry = null;
        if(searchResponseIterator != null) {
            while (searchResponseIterator.hasNext()) {
                rootEntry = ElasticDataModel.fromJson(searchResponseIterator.next().getSourceAsString());
                if (rootEntry.getThreadGuidParent() == null) {
                    break;
                }
            }
            if(searchResponses && (rootEntry != null && (getArchivedResponse || rootEntry.getArchived() == null))/*do not search archived response*/) {
                searchAllEntries(rootEntry, new HashSet<>(), searchUpdateHistory, getArchivedResponse);
            }
        }
        if(rootEntry == null) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND, "GUID provided is not exists");
        }
        return rootEntry;
    }

    public ElasticDataModel archive(String guid, ESIndexFields esIndexFields) {
        ElasticDataModel result = search(String.format(Queries.QUERY_BY_GUID, esIndexFields.getEsFieldName(), guid),
                true,true,true,SortOrder.DESC);
        if(result == null) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"No entries with given GUID");
        }
        Set<ElasticDataModel> entriesToArchive = new HashSet<>();
        flatten(result,entriesToArchive);
        entriesToArchive.forEach(entryToArchive -> {
            if(entryToArchive.getGuid() != null) {
                entryToArchive.setArchived(getCurrentDate());
                entryToArchive.getHistory().clear(); // clean up as answers and history will be also stored in es
                entryToArchive.getAnswers().clear();
                esRepository.save(entryToArchive);
            }
        });
        if(entriesToArchive.isEmpty()) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND, "GUID provided is not exists");
        }
        return result;
    }

    public ElasticDataModel searchArchive(String guid, ESIndexFields esIndexFields) {
        ElasticDataModel result = search(String.format(Queries.QUERY_ARCHIVED_ENTRIES, esIndexFields.getEsFieldName(), guid),
                true,true,true,SortOrder.DESC);
        if(result== null) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND, "GUID provided is not exists");
        }
        return result;
    }
    
    public ElasticDataModel delete(String guid, ESIndexFields esIndexFields, boolean deleteOnlyArchived) {
        ElasticDataModel result;
        if(deleteOnlyArchived) {
            result = search(String.format(Queries.QUERY_ARCHIVED_ENTRIES, esIndexFields.getEsFieldName(), guid),
                    true, true, true, SortOrder.DESC);
        } else {
            result =  search(String.format(Queries.QUERY_BY_GUID, esIndexFields.getEsFieldName(), guid),
                    true, true, true, SortOrder.DESC);   
        }
        if(result == null) {
            throw new RestStatusException(HttpStatus.SC_NOT_FOUND,"No entries with given GUID");
        }
        Set<ElasticDataModel> entriesToDelete = new HashSet<>();
        flatten(result,entriesToDelete);
        entriesToDelete.forEach(e -> {
            esRepository.deleteById(e.getGuid());
        });
        return result;
    }
    
    public void flatten(ElasticDataModel root, Set<ElasticDataModel> entries) {
        entries.add(root);
        root.getAnswers().forEach(e -> flatten(e,entries));
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
        searchSourceBuilder.sort("created",sortOrder);
        searchSourceBuilder.size(max_number_of_results);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
    
    private ElasticDataModel searchAllEntries(ElasticDataModel responseRoot,Set<String> entryUuid, boolean searchUpdateHistory, 
                                              boolean getArchivedResponse) {
        checkAndAddHistory(responseRoot,searchUpdateHistory);
        Iterator<SearchHit> searchResponseIterator = getSearchResponse(String.format(Queries.QUERY_BY_GUID, ESIndexFields.PARENT_THREAD.getEsFieldName(),
                responseRoot.getThreadGuid()), SortOrder.DESC);
        while(searchResponseIterator.hasNext()) {
            ElasticDataModel response = ElasticDataModel.fromJson(searchResponseIterator.next().getSourceAsString());
            if(!entryUuid.contains(response.getEntryGuid())) {
                if(!getArchivedResponse && response.getArchived() != null) {
                    break; // do not search archived response
                }
                responseRoot.addAnswers(response);
                entryUuid.add(response.getEntryGuid());
            }
            searchAllEntries(response,entryUuid,searchUpdateHistory, getArchivedResponse);
        }
        return responseRoot;
    }

    private void checkAndAddHistory(ElasticDataModel entry,boolean getUpdateHistory) {
        if(getUpdateHistory && entry != null) {
            Iterator<SearchHit> historyIterator = getSearchResponse(String.format(Queries.QUERY_BY_GUID, ESIndexFields.ENTRY.getEsFieldName(),
                    entry.getEntryGuid()), SortOrder.DESC);
            if(historyIterator.hasNext()) {
                historyIterator.next();
            }
            while(historyIterator.hasNext()) {
                ElasticDataModel history = ElasticDataModel.fromJson(historyIterator.next().getSourceAsString());
                entry.addHistory(history);
            }
        }
    }
    private Date getCurrentDate() {
        return new Date();
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
}