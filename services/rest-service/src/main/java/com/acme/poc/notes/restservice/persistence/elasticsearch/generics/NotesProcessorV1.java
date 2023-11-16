package com.acme.poc.notes.restservice.persistence.elasticsearch.generics;

import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByContent;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByParentEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Search and build response for thread of entries along with update histories
 */
@Slf4j
@Service("notesProcessorV1")
public class NotesProcessorV1 extends AbstractNotesProcessor {


    /**
     * We have custom search here, so this doesn't need an implementation
     *
     * @param query
     * @param esResults
     * @return
     */
    @Override
    public List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults) {
        return null;
    }

    /**
     * Strategy is to search for root entry first(for externalGuid/entryGuid), then build threads and history by incremental calls to 
     * elastic search until there are no more threads. Since there will be more network calls to elastic search with this approach V2 is done.
     *
     * @param query - query containing search details
     * @return entries with all threads and histories
     * - can return only archived
     * - can return only history
     * - can return both archived and histories
     */
    @Override
    public List<NotesData> fetchAndProcessEsResults(IQuery query) {
        List<NotesData> results = new ArrayList<>();
        Set<String> doNotSearchFurtherForHistory = new HashSet<>();
            Iterator<SearchHit<NotesData>> searchResponseIterator = getSearchResponse(query);
            if (searchResponseIterator != null) {
                while (searchResponseIterator.hasNext()) {
                    NotesData entry = searchResponseIterator.next().getContent();
                    if (query instanceof SearchByContent) {
                        results.add(entry);
                        continue;
                    }
                    if (entry != null && (query.includeArchived() || entry.getArchived() == null)/*do not search archived threads*/) {
                        if (!doNotSearchFurtherForHistory.contains(entry.getEntryGuid().toString())) {
                            results.add(searchThreadsAndHistories(query, entry, new HashSet<>()));
                            doNotSearchFurtherForHistory.add(entry.getEntryGuid().toString());
                        } 
                    }
                }
            }
        if (results.isEmpty()) {
            log.debug("No entries found");
        }
        return results;
    }

    /**
     * TODO Explain purpose of method
     *
     * @param query
     * @return
     */
    private Iterator<SearchHit<NotesData>> getSearchResponse(IQuery query) {
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        if (searchHits != null) {
            return searchHits.stream().iterator();
        }
        return null;
    }

    /**
     * Recursive function to build threads and histories
     *
     * @param query
     * @param threadRoot
     * @param entryThreadUuid
     * @return
     */
    private NotesData searchThreadsAndHistories(IQuery query, NotesData threadRoot, Set<String> entryThreadUuid) {
        checkAndAddHistory(threadRoot, query.includeVersions(),query);
        Iterator<SearchHit<NotesData>> searchResponseIterator = getSearchResponse(SearchByParentEntryGuid.builder()
                .searchGuid(threadRoot.getThreadGuid().toString())
                .build());
        while (searchResponseIterator.hasNext()) {
            NotesData thread = searchResponseIterator.next().getContent();
            // Below if to make sure to avoid history entries here as search Entry id will have history entries as well
            if (!entryThreadUuid.contains(thread.getEntryGuid().toString())) {
                if (filterArchived(query, thread, new ArrayList<>())) {
                    // Either discard archived entries OR Select only archived entries
                    break;
                }
                addChild(threadRoot, thread, query);
                entryThreadUuid.add(thread.getEntryGuid().toString());
                searchThreadsAndHistories(query, thread, entryThreadUuid);
            }
        }
        return threadRoot;
    }

    private void checkAndAddHistory(NotesData entry, boolean includeVersions, IQuery query) {
        if (includeVersions && entry != null) {
            Iterator<SearchHit<NotesData>> historyIterator = getSearchResponse(SearchByEntryGuid.builder()
                    .searchGuid(entry.getEntryGuid().toString())
                    .build());
            if (historyIterator.hasNext()) {
                historyIterator.next();
            }
            while (historyIterator.hasNext()) {
                NotesData history = historyIterator.next().getContent();
                updateVersions(entry, history, query);
            }
        }
    }

}
