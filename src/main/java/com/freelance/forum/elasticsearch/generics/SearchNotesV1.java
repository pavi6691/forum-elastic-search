package com.freelance.forum.elasticsearch.generics;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.IQuery;
import com.freelance.forum.elasticsearch.queries.Queries;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Search and build response for thread of entries along with update histories
 */
@Service("searchNotesV1")
public class SearchNotesV1 extends AbstractSearchNotes {


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
                    if (query instanceof Queries.SearchByContent) {
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
        if(query instanceof Queries.SearchArchived) {
            results = filterArchived(results);
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
        checkAndAddHistory(threadRoot,query.getUpdateHistory());
        Iterator<SearchHit<NotesData>> searchResponseIterator = getSearchResponse(new Queries.SearchByEntryGuid()
                .setEntryGuid(threadRoot.getThreadGuid().toString())
                .setSearchField(ESIndexNotesFields.PARENT_THREAD));
        while(searchResponseIterator.hasNext()) {
            NotesData thread = searchResponseIterator.next().getContent();
            // below if to make sure to avoid history entries here as search Entry id will have history entries as well
            if(!entryThreadUuid.contains(thread.getEntryGuid().toString())) {
                if ((!query.getArchived() && thread.getArchived() != null) ||
                        (query instanceof Queries.SearchArchived && thread.getArchived() == null)) {
                    break;
                }
                threadRoot.addThreads(thread);
                entryThreadUuid.add(thread.getEntryGuid().toString());
                searchThreadsAndHistories(query,thread,entryThreadUuid);
            }
        }
        return threadRoot;
    }

    private void checkAndAddHistory(NotesData entry, boolean getUpdateHistory) {
        if(getUpdateHistory && entry != null) {
            Iterator<SearchHit<NotesData>> historyIterator = getSearchResponse(new Queries.SearchByEntryGuid()
                    .setEntryGuid(entry.getEntryGuid().toString())
                    .setSearchField(ESIndexNotesFields.ENTRY));
            if(historyIterator.hasNext()) {
                historyIterator.next();
            }
            while(historyIterator.hasNext()) {
                NotesData history = historyIterator.next().getContent();
                entry.addHistory(history);
            }
        }
    }
}