package com.freelance.forum.service.generics;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.IQuery;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.elasticsearch.queries.RequestType;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import java.util.*;

@Service("searchNotesServiceV1")
public class SearchNotesServiceV1 extends AbstractSearchNotesService {

    @Override
    public List<NotesData> search(IQuery query) {
        List<NotesData> results = new ArrayList<>();
        Set<String> doNotSearchFurtherForHistory = new HashSet<>();
            Iterator<SearchHit<NotesData>> searchResponseIterator = getSearchResponse(query);
            if (searchResponseIterator != null) {
                while (searchResponseIterator.hasNext()) {
                    NotesData entry = searchResponseIterator.next().getContent();
                    if (query.getRequestType() == RequestType.CONTENT) {
                        results.add(entry);
                        continue;
                    }
                    if (entry != null && (query.getArchived() || entry.getArchived() == null)/*do not search archived threads*/) {
                        if(!doNotSearchFurtherForHistory.contains(entry.getEntryGuid().toString())) {
                            results.add(searchThreadsAndHistories(entry, new HashSet<>(), query.getUpdateHistory(),
                                    query.getArchived()));
                            doNotSearchFurtherForHistory.add(entry.getEntryGuid().toString());
                        } 
                    }
                }
            }
        if(query.getRequestType() == RequestType.ARCHIVE) {
            filterArchived(results);
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
    
    private NotesData searchThreadsAndHistories(NotesData threadRoot, Set<String> entryThreadUuid, boolean getUpdateHistory,
                                       boolean getArchivedResponse) {
        checkAndAddHistory(threadRoot,getUpdateHistory);
        Iterator<SearchHit<NotesData>> searchResponseIterator = getSearchResponse(new Queries.SearchByEntryGuid()
                .setEntryGuid(threadRoot.getThreadGuid().toString())
                .setSearchField(ESIndexNotesFields.PARENT_THREAD));
        while(searchResponseIterator.hasNext()) {
            NotesData thread = searchResponseIterator.next().getContent();
            // below if to make sure to avoid history entries here as search Entry id will have history entries as well
            if(!entryThreadUuid.contains(thread.getEntryGuid().toString())) { 
                if(!getArchivedResponse && thread.getArchived() != null) {
                    break; // do not search archived thread
                }
                threadRoot.addThreads(thread);
                entryThreadUuid.add(thread.getEntryGuid().toString());
                searchThreadsAndHistories(thread,entryThreadUuid,getUpdateHistory, getArchivedResponse);
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