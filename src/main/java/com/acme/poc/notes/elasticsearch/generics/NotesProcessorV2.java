package com.acme.poc.notes.elasticsearch.generics;

import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.elasticsearch.queries.SearchByEntryGuid;
import org.springframework.data.elasticsearch.core.SearchHit;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Search and build response for thread of entries along with update histories.
 */
@Service("notesProcessorV2")
public class NotesProcessorV2 extends AbstractNotesOperations {

    @Override
    List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults) {
        List<NotesData> results = new ArrayList<>();
        Map<UUID,Map<UUID,List<NotesData>>> processMap = new HashMap<>();
        Set<UUID> onlyThreads = new HashSet<>();
        if(esResults != null) {
            while (esResults.hasNext()) {
                NotesData entry = esResults.next().getContent();
                if (!processMap.containsKey(entry.getThreadGuidParent())) {
                    // null key is allowed and holds root element
                    processMap.put(entry.getThreadGuidParent(), new LinkedHashMap<>());
                }
                if(!processMap.get(entry.getThreadGuidParent()).containsKey(entry.getEntryGuid())) {
                    processMap.get(entry.getThreadGuidParent()).put(entry.getEntryGuid(), new ArrayList<>());
                }
                if (!onlyThreads.contains(entry.getThreadGuid()) &&
                        !onlyThreads.contains(entry.getThreadGuidParent())) {
                    if (filterArchived(query,entry,results)) {
                        continue;
                    }
                    if(results.isEmpty() || !(query instanceof SearchByEntryGuid)
                            || query.searchAfter() != null) {
                        addEntries(results,entry,query);   
                    }
                }
                onlyThreads.add(entry.getThreadGuid());
                processMap.get(entry.getThreadGuidParent()).get(entry.getEntryGuid()).add(entry);
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
     * @return - entries with threads and update histories
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
                    if (filterArchived(query,threads.get(i),new ArrayList<>())) {
                        // Either discard archived entries OR Select only archived entries
                        break;
                    }
                    addThreads(threadEntry,threads.get(i),query);
                    entryThreadUuid.add(threads.get(i).getEntryGuid());
                    buildThreads(threads.get(i), results, entryThreadUuid, query);
                }
            }
        }
        return threadEntry;
    }
}
