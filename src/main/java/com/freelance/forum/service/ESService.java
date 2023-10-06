package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.configuration.*;
import com.freelance.forum.elasticsearch.pojo.DeleteResponse;
import com.freelance.forum.elasticsearch.pojo.ElasticDataModel;
import com.freelance.forum.elasticsearch.pojo.GUIDType;
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

    public ElasticDataModel saveNew(ElasticDataModel elasticDataModel) {
        elasticDataModel.setGuid(UUID.randomUUID().toString());
        elasticDataModel.setEntryGuid(UUID.randomUUID().toString());
        elasticDataModel.setThreadGuid(UUID.randomUUID().toString());
        elasticDataModel.setCreated(getCurrentDate());
        if(elasticDataModel.getThreadGuidParent() != null &&
                (elasticDataModel.getThreadGuidParent().isBlank() || elasticDataModel.getThreadGuidParent().isEmpty())) {
            elasticDataModel.setThreadGuidParent(null);
        }
        return esRepository.save(elasticDataModel);
    }

    public ElasticDataModel searchByGuid(String guid) {
        return esRepository.findById(guid).get();
    }

    /**
     * To Update 
     *   - provide GUID that's being updated
     *   - provide entryGUID in payload. - same entryGUID is used in new version of updated entry
     *   - So to get history, we can query by entryGUID
     * @param elasticDataModel
     * @return
     */
    public ElasticDataModel updateByEntryGuid(ElasticDataModel elasticDataModel) {
        if(StringUtils.isEmpty(elasticDataModel.getEntryGuid())) {
            throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"Entry Id Required for update but is provided");
        }
        ElasticDataModel elasticDataModelToUpdate = searchByGuid(elasticDataModel.getGuid());
        elasticDataModelToUpdate.setGuid(UUID.randomUUID().toString());
        elasticDataModelToUpdate.setCreated(getCurrentDate());
        elasticDataModelToUpdate.setEntryGuid(elasticDataModel.getEntryGuid());
        elasticDataModelToUpdate.setContent(elasticDataModel.getContent());
        return esRepository.save(elasticDataModelToUpdate);
    }

    /**
     * Query by GUID. Supported GUIDs to query are
     *   1. externalGuid
     *   2. entryGuid
     * @param guid
     * @param searchUpdateHistory
     * @param sendArchivedResponse
     * @param guidType
     * @return
     */
    public ElasticDataModel searchByGuid(String guid,boolean searchUpdateHistory,boolean sendArchivedResponse,boolean searchResponses,
                                       GUIDType guidType) {
        Iterator<SearchHit> searchResponseIterator = null;
        if(GUIDType.EXTERNAL == guidType) {
            searchResponseIterator = getSearchResponse(String.format(Queries.QUERY_BY_EXTERNAL_GUID, guid), SortOrder.DESC);
        } else if(GUIDType.ENTRY == guidType) {
            searchResponseIterator = getSearchResponse(String.format(Queries.QUERY_BY_ENTRY_GUID, guid), SortOrder.DESC);
        }
        ElasticDataModel rootEntry = new ElasticDataModel();
        if(searchResponseIterator != null) {
            while (searchResponseIterator.hasNext()) {
                rootEntry = ElasticDataModel.fromJson(searchResponseIterator.next().getSourceAsString());
                if (rootEntry.getThreadGuidParent() == null) {
                    break;
                }
            }
            if(searchResponses && (sendArchivedResponse || rootEntry.getArchived() == null)/*do not search archived response*/) {
                return getResponsesForGuid(rootEntry, new HashSet<>(), searchUpdateHistory, sendArchivedResponse);
            }
        }
        return rootEntry;
    }

    public ElasticDataModel archive(String guid,GUIDType guidType) {
        ElasticDataModel rootEntry = searchByGuid(guid,false,false,false,guidType);
        rootEntry.setArchived(getCurrentDate());
        return esRepository.save(rootEntry);
    }
    
    public DeleteResponse delete(String guid, GUIDType guidType) {
        ElasticDataModel rootEntry = searchByGuid(guid,true,true,true,guidType);
        return new DeleteResponse(rootEntry,0);
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
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    private ElasticDataModel getResponsesForGuid(ElasticDataModel responseRoot,Set<String> entryUuid,
                                                 boolean searchUpdateHistory,boolean sendArchivedResponse) {
        checkAndAddHistory(responseRoot,searchUpdateHistory);
        Iterator<SearchHit> searchResponseIterator = getSearchResponse(String.format(Queries.QUERY_RESPONSES_BY_THREAD_GUID,
                responseRoot.getThreadGuid()), SortOrder.DESC);
        while(searchResponseIterator.hasNext()) {
            ElasticDataModel response = ElasticDataModel.fromJson(searchResponseIterator.next().getSourceAsString());
            if(!entryUuid.contains(response.getEntryGuid())) {
                if(!sendArchivedResponse && responseRoot.getArchived() != null) {
                    break; // do not search archived response
                }
                responseRoot.addAnswers(response);
                entryUuid.add(response.getEntryGuid());
            }
            getResponsesForGuid(response,entryUuid,searchUpdateHistory, sendArchivedResponse);
        }
        return responseRoot;
    }

    private void checkAndAddHistory(ElasticDataModel entry,boolean getUpdateHistory) {
        if(getUpdateHistory && entry != null) {
            Iterator<SearchHit> historyIterator = getSearchResponse(String.format(Queries.QUERY_BY_ENTRY_GUID,
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