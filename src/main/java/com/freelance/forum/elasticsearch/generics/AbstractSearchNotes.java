package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.IQuery;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

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
    protected SearchHits<NotesData> execSearchQuery(IQuery query) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(query.buildQuery()))
                .withSort(Sort.by(Sort.Order.asc(ESIndexNotesFields.CREATED.getEsFieldName())))
                .build();
        searchQuery.setMaxResults(max_number_of_history_and_threads);
        return elasticsearchOperations.search(searchQuery, NotesData.class);
    }

    /**
     * return very first entries for given search criteria. search criteria includes query by externalGuid/entryGuid.
     * externalGuid can have more than one entry. entryGuid can have only entry
     * @param query
     * @return processed list of entries with threads and histories
     */
    protected Map<UUID, Map<UUID,List<NotesData>>> getRootEntries(IQuery query) {
        List<NotesData> results = new ArrayList<>();
        Iterator<SearchHit<NotesData>> rootEntries = null;
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        if(searchHits != null)
            rootEntries = searchHits.stream().iterator();
        Map<UUID, Map<UUID,List<NotesData>>> rootEntriesMap = new HashMap<>();
        if(rootEntries != null) {
            while (rootEntries.hasNext()) {
                NotesData rootNotesData = rootEntries.next().getContent();
                if (!rootEntriesMap.containsKey(rootNotesData.getExternalGuid())) {
                    rootEntriesMap.put(rootNotesData.getExternalGuid(), new HashMap<>());
                }
                if(!rootEntriesMap.get(rootNotesData.getExternalGuid()).containsKey(rootNotesData.getEntryGuid())) {
                    rootEntriesMap.get(rootNotesData.getExternalGuid()).put(rootNotesData.getEntryGuid(), new ArrayList<>());
                }
                rootEntriesMap.get(rootNotesData.getExternalGuid()).get(rootNotesData.getEntryGuid()).add(rootNotesData);
            }
        }
        return rootEntriesMap;
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
     * filter and return only archived
     * @param results
     */
    protected List<NotesData> filterArchived(List<NotesData> results) {
        List<NotesData> archivedResults = new ArrayList<>();
        for(NotesData result : results) {
            if (result != null && result.getArchived() == null && result.getThreads() != null && !result.getThreads().isEmpty()) {
                archivedResults.addAll(result.getThreads());
            } else {
                archivedResults.add(result);
            }
        }
        return archivedResults;
    }
}
