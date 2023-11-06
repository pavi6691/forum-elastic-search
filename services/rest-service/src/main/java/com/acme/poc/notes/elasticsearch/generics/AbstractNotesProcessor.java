package com.acme.poc.notes.elasticsearch.generics;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.elasticsearch.queries.SearchArchivedByExternalGuid;
import com.acme.poc.notes.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.elasticsearch.queries.SearchByExternalGuid;
import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;
import org.apache.http.HttpStatus;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * abstraction for executing search query.
 */
@Service
public abstract class AbstractNotesProcessor implements INotesOperations {
    @Value("${default.number.of.entries.to.return}")
    private int default_size_configured;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    private List<Object> sortValues = new ArrayList<>();

    abstract List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults);
    
    
    /**
     * 1. Gets all entries along with threads and histories for externalGuid and content field
     * 2. It's not possible to search threads and histories by entryGuid. So
     *     - Search for main(root) entry first(for externalGuid/entryGuid), 
     *     - then call for all subsequent entries by externalGuid and entries created after main entry that's requested.
     *     - there will be some other entries created after and updated later, handled in process method
     * 3. Process response and build threads and histories
     *
     * @param query - query containing search details
     * @return list of entries with threads and histories
     * - filter only archived
     * - filter only history
     * - filter both archived and histories
     */
    @Override
    public List<NotesData> search(IQuery query) {
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        if(query instanceof SearchByEntryGuid || query instanceof SearchArchivedByEntryGuid) {
            // Search by entryGuid doesn't fetch all entries, so fetch by externalEntry and created after this entry
            if (searchHits != null) {
                Iterator<SearchHit<NotesData>> rootEntries = searchHits.stream().iterator();
                if (rootEntries != null && rootEntries.hasNext()) {
                    NotesData rootEntry = rootEntries.next().getContent();
                    if (query.getArchived() || rootEntry.getArchived() == null) { // Need results after first of entry these entries,
                        IQuery entryQuery = SearchByExternalGuid.builder()
                                .size(((AbstractQuery) query).getSize())
                                .sortOrder(((AbstractQuery) query).getSortOrder())
                                .searchAfter(((AbstractQuery) query).getSearchAfter())
                                .getArchived(query.getArchived())
                                .getUpdateHistory(query.getUpdateHistory())
                                .searchGuid(rootEntry.getExternalGuid().toString())
                                .createdDateTime(rootEntry.getCreated().getTime()).build();
                        searchHits = execSearchQuery(entryQuery);
                    }
                }
            }
        }
        if(searchHits != null && searchHits.getSearchHits().size() > 0) {
            if(searchHits.getSearchHits().size() == 1) { // No need to process as its just one entry
                return List.of(searchHits.stream().iterator().next().getContent());
            }
            return process(query,searchHits.stream().iterator());
        }
        return new ArrayList<>();
    }

    /**
     * executes IQuery
     * @param query 
     * @return search result from elastics search response
     */
    protected SearchHits<NotesData> execSearchQuery(IQuery query) {
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.wrapperQuery(query.buildQuery()))
                .withSort(Sort.by(Sort.Order.asc(EsNotesFields.CREATED.getEsFieldName())));
        if(query.searchAfter() != null && !(query instanceof SearchByEntryGuid || query instanceof SearchArchivedByEntryGuid)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(NotesConstants.TIMESTAMP_ISO8601);
            try {
                searchQueryBuilder.withSearchAfter(List.of(dateFormat.parse(query.searchAfter().toString()).toInstant().toEpochMilli()));
            } catch (ParseException e) {
                throw new RestStatusException(HttpStatus.SC_BAD_REQUEST,"Incorrect date format on searchAfter param");
            }
        }
        NativeSearchQuery searchQuery  = searchQueryBuilder.build();
        searchQuery.setMaxResults(query.getSize() > 0 ? query.getSize() : default_size_configured);
        return elasticsearchOperations.search(searchQuery, NotesData.class);
    }

    protected void addHistory(NotesData existingEntry,NotesData updatedEntry, IQuery query) {
        if(!existingEntry.getGuid().equals(updatedEntry.getGuid())) {
            if (query.getUpdateHistory()) {
                NotesData history = new NotesData(existingEntry.getGuid(), existingEntry.getExternalGuid(), existingEntry.getThreadGuid(),
                        existingEntry.getEntryGuid(), existingEntry.getThreadGuidParent(), existingEntry.getContent(), existingEntry.getCreated(),
                        existingEntry.getArchived(), null, null);
                if(query.getSortOrder() == SortOrder.ASC) {
                    existingEntry.addHistory(history,existingEntry.getHistory() != null ? existingEntry.getHistory().size() : 0);
                } else {
                    existingEntry.addHistory(history,0);
                }
            }
            existingEntry.setGuid(updatedEntry.getGuid());
            existingEntry.setContent(updatedEntry.getContent());
            existingEntry.setCreated(updatedEntry.getCreated());
            existingEntry.setArchived(updatedEntry.getArchived());
        }
    }

    protected void addThreads(NotesData existingEntry,NotesData newEntry, IQuery query) {
        if(query.getSortOrder() == SortOrder.ASC) {
            existingEntry.addThreads(newEntry, existingEntry.getThreads() != null ? existingEntry.getThreads().size() : 0);   
        } else {
            existingEntry.addThreads(newEntry,0);
        }
    }

    protected void addEntries(List<NotesData> entries,NotesData newEntry, IQuery query) {
        if(query.getSortOrder() == SortOrder.ASC) {
            entries.add(newEntry);   
        } else {
            entries.add(0,newEntry);
        }
    }

    /**
     * Either discard archived entries OR Select only archived entries
     * @param query
     * @param entry
     * @return true when not to select archived entry. And true for only archived request
     */
    protected boolean filterArchived(IQuery query, NotesData entry, List<NotesData> results) {
        return ((!query.getArchived() && entry.getArchived() != null) ||
                (query instanceof SearchArchivedByExternalGuid) && entry.getArchived() == null && !results.isEmpty());
    }
}
