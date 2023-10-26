package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.IQuery;
import org.elasticsearch.index.query.QueryBuilders;
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

/**
 * abstraction for executing search query.
 */
@Service
public abstract class AbstractSearchNotes implements ISearchNotes {
    @Value("${max.number.of.history.and.threads}")
    private int max_number_of_history_and_threads;
    @Autowired
    ElasticsearchOperations elasticsearchOperations;

    /**
     * executes IQuery
     * @param query 
     * @return search result from elastics search response
     */
    public SearchHits<NotesData> execSearchQuery(IQuery query) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(query.buildQuery()))
                .withSort(Sort.by(Sort.Order.desc(ESIndexNotesFields.CREATED.getEsFieldName())))
                .build();
        searchQuery.setMaxResults(max_number_of_history_and_threads);
        return elasticsearchOperations.search(searchQuery, NotesData.class);
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
