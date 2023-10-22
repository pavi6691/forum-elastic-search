package com.freelance.forum.service.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.SearchRequest;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.elasticsearch.queries.RequestType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public abstract class AbstractSearchNotesService implements ISearchNotesService {
    @Value("${max.number.of.history.and.threads}")
    private int max_number_of_history_and_threads;
    @Autowired
    ElasticsearchOperations elasticsearchOperations;
    
    public SearchHits<NotesData> execSearchQuery(SearchRequest searchRequest) {
        Sort.Order _sortOrder = Sort.Order.desc(ESIndexNotesFields.CREATED.getEsFieldName());
        if(searchRequest.getSortOrder() == SortOrder.ASC) {
            _sortOrder = Sort.Order.asc(ESIndexNotesFields.CREATED.getEsFieldName());
        }
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(getSearchQuery(searchRequest)))
                .withSort(Sort.by(_sortOrder))
                .build();
        searchQuery.setMaxResults(max_number_of_history_and_threads);
        return elasticsearchOperations.search(searchQuery, NotesData.class);
    }

    private String getSearchQuery(SearchRequest searchRequest) {
        String query = "";
        if(searchRequest.getRequestType() == RequestType.EXTERNAL_ENTRIES ||
                (searchRequest.getSearchField() == ESIndexNotesFields.EXTERNAL && searchRequest.getRequestType() == RequestType.ARCHIVE)) {
            query = String.format(Queries.QUERY_ROOT_EXTERNAL_ENTRIES, searchRequest.getSearch());
        } else if(searchRequest.getRequestType() == RequestType.ENTRIES || 
                (searchRequest.getSearchField() == ESIndexNotesFields.ENTRY && searchRequest.getRequestType() == RequestType.ARCHIVE)) {
            query = String.format(Queries.QUERY_ENTRIES, searchRequest.getSearchField().getEsFieldName(), searchRequest.getSearch(), 
                    searchRequest.getTimeToSearchEntriesAfter());
        } else if(searchRequest.getRequestType() == RequestType.CONTENT) {
            query = String.format(Queries.QUERY_CONTENT_ENTRIES, searchRequest.getSearch());
        }
        return query;
    }

    /**
     * filter and return only archived
     * @param results
     */
    public void filterArchived(List<NotesData> results) {
        List<NotesData> archivedResults = new ArrayList<>();
        for(NotesData result : results) {
            if (result != null) {
                while (result.getArchived() == null && result.getThreads() != null && !result.getThreads().isEmpty()) {
                    result = result.getThreads().get(0);
                }
            }
            if(result.getArchived() != null) {
                archivedResults.add(result);
            }
        }
        results.clear();
        results.addAll(archivedResults);
    }
}
