package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.*;
import com.freelance.forum.elasticsearch.queries.generics.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import org.apache.http.HttpStatus;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import java.util.Iterator;
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


    abstract List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults);
    /**
     * 1. Search for external entries and content straight away get all results. But It's not possible to search all entries by entryGuid. So
     *     - Search for main(root) entry first(for externalGuid/entryGuid), 
     *     - then call for all subsequent entries by externalGuid created after this main entry. 
     *     - this allows to search all entries by entryGuid in single request.
     *     - there will be other entries, but only consider entry requested. handled in process method
     * 2. Process response and build threads and histories
     *
     * @param mainQuery - query containing search details
     * @return After processing, entries with all threads and histories
     * - return multiple individual entries with same external ID OR entries with different entryGuid
     * - return only archived
     * - return only history
     * - return both archived and histories
     */
    @Override
    public List<NotesData> search(IQuery mainQuery) {
        SearchHits<NotesData> searchHits = null;
        if(mainQuery instanceof SearchByContent || mainQuery instanceof SearchByExternalGuid) {
            searchHits = execSearchQuery(mainQuery);
        } else {
            searchHits = execSearchQuery(mainQuery);
            if (searchHits != null) {
                Iterator<SearchHit<NotesData>> rootEntries = searchHits.stream().iterator();
                if (rootEntries != null && rootEntries.hasNext()) {
                    NotesData rootEntry = rootEntries.next().getContent();
                    if (mainQuery.getArchived() || rootEntry.getArchived() == null) {
                        // Need results after first of entry these entries,
                        IQuery entryQuery = new SearchByExternalGuid()
                                .setGetArchived(mainQuery.getArchived())
                                .setGetUpdateHistory(mainQuery.getUpdateHistory())
                                .setSearchBy(rootEntry.getExternalGuid().toString())
                                .setCreatedDateTime(rootEntry.getCreated().getTime());
                        searchHits = execSearchQuery(entryQuery);
                    }
                }
            }
        }
        if(searchHits != null) {
            return process(mainQuery,searchHits.stream().iterator());
        } else {
            throw new RestStatusException(HttpStatus.SC_NO_CONTENT,"No entries found from elastic search for given search criteria");
        }
    }

    /**
     * executes IQuery
     * @param query 
     * @return search result from elastics search response
     */
    protected SearchHits<NotesData> execSearchQuery(IQuery query) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(query.buildQuery()))
                .withSort(Sort.by(Sort.Order.asc(ESIndexNotesFields.CREATED.getEsFieldName())))
                .build();
        searchQuery.setMaxResults(max_number_of_history_and_threads);
        return elasticsearchOperations.search(searchQuery, NotesData.class);
    }

    protected void addHistory(NotesData existingEntry,NotesData updatedEntry, IQuery query) {
        if(!existingEntry.getGuid().equals(updatedEntry.getGuid())) {
            if (query.getUpdateHistory()) {
                existingEntry.addHistory(new NotesData(existingEntry.getGuid(), existingEntry.getExternalGuid(), existingEntry.getThreadGuid(),
                        existingEntry.getEntryGuid(), existingEntry.getThreadGuidParent(), existingEntry.getContent(), existingEntry.getCreated(),
                        existingEntry.getArchived(), null, null));
            }
            existingEntry.setGuid(updatedEntry.getGuid());
            existingEntry.setContent(updatedEntry.getContent());
            existingEntry.setCreated(updatedEntry.getCreated());
            existingEntry.setArchived(updatedEntry.getArchived());
        }
    }

    /**
     * Either discard archived entries OR Select only archived entries
     * @param query
     * @param entry
     * @return true when not to select archived entry. And true for only archived request
     */
    protected boolean filterArchived(IQuery query, NotesData entry) {
        return ((!query.getArchived() && entry.getArchived() != null) ||
                ((query instanceof SearchArchivedByEntryGuid ||
                        query instanceof SearchArchivedByExternalGuid) && entry.getArchived() == null));
    }
}
