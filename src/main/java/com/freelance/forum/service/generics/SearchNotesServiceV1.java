package com.freelance.forum.service.generics;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.SearchRequest;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.RequestType;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import java.util.*;

@Service("searchNotesServiceV1")
public class SearchNotesServiceV1 extends AbstractSearchNotesService {

    @Override
    public List<NotesData> search(SearchRequest searchRequest) {
        List<NotesData> results = new ArrayList<>();
        Set<String> doNotSearchFurtherForHistory = new HashSet<>();
            Iterator<SearchHit<NotesData>> searchResponseIterator = getSearchResponse(searchRequest);
            if (searchResponseIterator != null) {
                while (searchResponseIterator.hasNext()) {
                    NotesData entry = searchResponseIterator.next().getContent();
                    if (searchRequest.getRequestType() == RequestType.CONTENT) {
                        results.add(entry);
                        continue;
                    }
                    if (entry != null && (searchRequest.getArchivedResponse() || entry.getArchived() == null)/*do not search archived threads*/) {
                        if(!doNotSearchFurtherForHistory.contains(entry.getEntryGuid().toString())) {
                            results.add(searchThreadsAndHistories(entry, new HashSet<>(), searchRequest.getUpdateHistory(),
                                    searchRequest.getArchivedResponse()));
                            doNotSearchFurtherForHistory.add(entry.getEntryGuid().toString());
                        } 
                    }
                }
            }
        if(results.isEmpty()) {
            System.out.println(String.format("No entries found for given searchRequest. %s = %s",
                    searchRequest.getSearchField().getEsFieldName(),searchRequest.getSearch()));
        }
        return results;
    }
 
    private Iterator<SearchHit<NotesData>> getSearchResponse(SearchRequest searchRequest) {
        SearchHits<NotesData> searchHits = execSearchQuery(searchRequest);
        if(searchHits != null) {
            return searchHits.stream().iterator();
        }
        return null;
    }
    
    private NotesData searchThreadsAndHistories(NotesData threadRoot, Set<String> entryThreadUuid, boolean getUpdateHistory,
                                       boolean getArchivedResponse) {
        checkAndAddHistory(threadRoot,getUpdateHistory);
        Iterator<SearchHit<NotesData>> searchResponseIterator = getSearchResponse(new SearchRequest.Builder()
                        .setSearchField(ESIndexNotesFields.PARENT_THREAD)
                        .setRequestType(RequestType.ENTRIES)
                        .setSearch(threadRoot.getThreadGuid().toString())
                        .setSortOrder(SortOrder.DESC).build());
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
            Iterator<SearchHit<NotesData>> historyIterator = getSearchResponse(new SearchRequest.Builder()
                    .setSearchField(ESIndexNotesFields.ENTRY)
                    .setRequestType(RequestType.ENTRIES)
                    .setSearch(entry.getEntryGuid().toString())
                    .setSortOrder(SortOrder.DESC).build());
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