package com.acme.poc.notes.elasticsearch.generics;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.elasticsearch.queries.SearchByContent;
import com.acme.poc.notes.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.elasticsearch.queries.SearchByParentThreadGuid;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Search and build response for thread of entries along with update histories
 */
@Service("notesProcessorV1")
public class NotesProcessorV1 extends AbstractNotesOperations {


    /**
     * we have custom search here, so this doesn't need an implementation
     * @param query
     * @param esResults
     * @return
     */
    @Override
    List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults) {
        return null;
    }

    /**
     * Strategy is to search for root entry first(for externalGuid/entryGuid), then build threads and history by incremental calls to 
     * elastic search until there are no more threads. Since there will be more network calls to elastic search with this approach V2 is done.
     * @param query - query containing search details
     * @return entries with all threads and histories
     * - can return only archived
     * - can return only history
     * - can return both archived and histories
     */
    @Override
    public List<NotesData> search(IQuery query) {
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
                    if (entry != null && (query.getArchived() || entry.getArchived() == null)/*do not search archived threads*/) {
                        if(!doNotSearchFurtherForHistory.contains(entry.getEntryGuid().toString())) {
                            results.add(searchThreadsAndHistories(query,entry, new HashSet<>()));
                            doNotSearchFurtherForHistory.add(entry.getEntryGuid().toString());
                        } 
                    }
                }
            }
        if(results.isEmpty()) {
            System.out.println("No entries found");
        }
        return results;
    }
 
    private Iterator<SearchHit<NotesData>> getSearchResponse(IQuery query) {
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        if(searchHits != null) {
            return searchHits.stream().iterator();
        }
        return null;
    }

    /**
     * recursive function to build threads and histories
     * @param query
     * @param threadRoot
     * @param entryThreadUuid
     * @return
     */
    private NotesData searchThreadsAndHistories(IQuery query, NotesData threadRoot, Set<String> entryThreadUuid) {
        checkAndAddHistory(threadRoot,query.getUpdateHistory(),query);
        Iterator<SearchHit<NotesData>> searchResponseIterator = getSearchResponse(new SearchByParentThreadGuid()
                .setSearchBy(threadRoot.getThreadGuid().toString()));
        while(searchResponseIterator.hasNext()) {
            NotesData thread = searchResponseIterator.next().getContent();
            // below if to make sure to avoid history entries here as search Entry id will have history entries as well
            if(!entryThreadUuid.contains(thread.getEntryGuid().toString())) {
                if (filterArchived(query,thread,new ArrayList<>())) {
                    // Either discard archived entries OR Select only archived entries
                    break;
                }
                addThreads(threadRoot,thread,query);
                entryThreadUuid.add(thread.getEntryGuid().toString());
                searchThreadsAndHistories(query,thread,entryThreadUuid);
            }
        }
        return threadRoot;
    }

    private void checkAndAddHistory(NotesData entry, boolean getUpdateHistory,IQuery query) {
        if(getUpdateHistory && entry != null) {
            Iterator<SearchHit<NotesData>> historyIterator = getSearchResponse(new SearchByEntryGuid()
                    .setSearchBy(entry.getEntryGuid().toString()));
            if(historyIterator.hasNext()) {
                historyIterator.next();
            }
            while(historyIterator.hasNext()) {
                NotesData history = historyIterator.next().getContent();
                addHistory(entry,history,query);
            }
        }
    }
}