package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import com.freelance.forum.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.freelance.forum.elasticsearch.queries.SearchByEntryGuid;
import org.springframework.data.elasticsearch.core.SearchHit;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Search and build response for thread of entries along with update histories.
 */
@Service("searchNotesV2")
public class SearchNotesV2 extends AbstractSearchNotes {

    @Override
    List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults) {
        List<NotesData> results = new ArrayList<>();
        Map<UUID,Map<UUID,List<NotesData>>> processMap = new HashMap<>();
        Set<UUID> onlyThreads = new HashSet<>();
        if(esResults != null) {
            while (esResults.hasNext()) {
                NotesData notesData = esResults.next().getContent();
                if (!processMap.containsKey(notesData.getThreadGuidParent())) {
                    // null key is allowed and holds root element
                    processMap.put(notesData.getThreadGuidParent(), new LinkedHashMap<>());
                }
                if(!processMap.get(notesData.getThreadGuidParent()).containsKey(notesData.getEntryGuid())) {
                    processMap.get(notesData.getThreadGuidParent()).put(notesData.getEntryGuid(), new ArrayList<>());
                }
                if (!onlyThreads.contains(notesData.getThreadGuid()) &&
                        !onlyThreads.contains(notesData.getThreadGuidParent())) {
                    if (filterArchived(query,notesData)) {
                        continue;
                    }
                    if(results.isEmpty() || !(query instanceof SearchByEntryGuid)) {
                        results.add(notesData);   
                    }
                }
                onlyThreads.add(notesData.getThreadGuid());
                processMap.get(notesData.getThreadGuidParent()).get(notesData.getEntryGuid()).add(notesData);
            }
            results.stream().forEach(e -> buildThreads(e,processMap,new HashSet<>(),query));
        }
        return results;
    }

    /**
     * Recursively builds threads and histories from stored entries in given results map
     * @param threadEntry
     * @param results
     * @param entryThreadUuid
     * @param query
     * @return - Entries with threads and update histories
     */
    private NotesData buildThreads(NotesData threadEntry, Map<UUID,Map<UUID,List<NotesData>>> results, Set<UUID> entryThreadUuid, IQuery query) {
        if(query.getUpdateHistory() && threadEntry != null && results.containsKey(threadEntry.getThreadGuidParent()) &&
                results.get(threadEntry.getThreadGuidParent()).containsKey(threadEntry.getEntryGuid())) {
            int nrOfUpdates = results.get(threadEntry.getThreadGuidParent()).get(threadEntry.getEntryGuid()).size();
            for(int i = 0; i < nrOfUpdates; i++) { 
                addHistory(threadEntry,results.get(threadEntry.getThreadGuidParent()).get(threadEntry.getEntryGuid()).get(i),query);
            }
        }
        List<NotesData> threads = new ArrayList<>();
        if(threadEntry.getThreadGuid() != null && results.containsKey(threadEntry.getThreadGuid())) {
            results.get(threadEntry.getThreadGuid()).values().stream().forEach(l -> threads.addAll(l));
        }
        if(!threads.isEmpty()) {
            for(int i = 0; i < threads.size(); i++) {
                // entryThreadUuid set is to make sure to avoid history entries here as search Entry id will have history entries as well
                if (!entryThreadUuid.contains(threads.get(i).getEntryGuid())) {
                    if (filterArchived(query,threads.get(i))) {
                        // Either discard archived entries OR Select only archived entries
                        break;
                    }
                    threadEntry.addThreads(threads.get(i));
                    entryThreadUuid.add(threads.get(i).getEntryGuid());
                    buildThreads(threads.get(i), results, entryThreadUuid, query);
                }
            }
        }
        return threadEntry;
    }
}
