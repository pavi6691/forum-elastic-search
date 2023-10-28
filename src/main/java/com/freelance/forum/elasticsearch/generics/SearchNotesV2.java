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
 * Search and build response for thread of entries along with update histories. Version 2
 */
@Service("searchNotesV2")
public class SearchNotesV2 extends AbstractSearchNotes {

    /**
     * Strategy is to search for root entry first(for externalGuid/entryGuid), then call for all subsequent entries created after for given guid.
     * seconds call responses are store in temporary map and then processed to build threads and histories. This strategy only requires two calls
     * elastic search for every search request made.
     * @param query - query containing search details
     * @return entries with all threads and histories
     * - can return only archived
     * - can return only history
     * - can return both archived and histories
     */
    @Override
    public List<NotesData> search(IQuery query) {
        List<NotesData> results = new ArrayList<>();
        Map<UUID, Map<UUID,NotesData>> rootEntriesMap = getRootEntries(query);
        for (Map<UUID,NotesData> entries : rootEntriesMap.values()) {
            for (NotesData veryFirstEntry : entries.values()) {
                if (query.getArchived() || veryFirstEntry.getArchived() == null) {
                    // Since query is by external id, we only need results after first of entry these entries,
                    Map<UUID, Map<UUID, List<NotesData>>> threads = getThreads(new Queries.SearchByEntryGuid()
                            .setEntryGuid(veryFirstEntry.getExternalGuid().toString())
                            .setSearchField(ESIndexNotesFields.ENTRY).setSearchField(ESIndexNotesFields.EXTERNAL)
                            .setCreatedDateTime(rootEntriesMap.get(veryFirstEntry.getExternalGuid())
                                    .get(veryFirstEntry.getEntryGuid()).getCreated().getTime()));
                    if (threads != null && !threads.isEmpty()) {
                        buildThreads(veryFirstEntry, threads, new HashSet<>(), query);
                    }
                    results.add(veryFirstEntry);
                }
            }
        }
        if(query instanceof Queries.SearchArchived) {
            results = filterArchived(results);
        }
        if(results.isEmpty()) {
            System.out.println("No entries found for given request");
        }
        return results;
    }

    /**
     * Prepare map of all entries in such a way that, entry(or multiple external entries) with threads and history can be built
     * @param query
     * @return
     */
    private Map<UUID,Map<UUID,List<NotesData>>> getThreads(IQuery query) {
        Iterator<SearchHit<NotesData>> threads = null;
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        if(searchHits != null)
            threads = searchHits.stream().iterator();
        Map<UUID,Map<UUID,List<NotesData>>> results = new HashMap<>();
        if(threads != null) {
            while (threads.hasNext()) {
                NotesData notesData = threads.next().getContent();
                if (!results.containsKey(notesData.getThreadGuidParent())) {
                    // null key is allowed and holds root element
                    results.put(notesData.getThreadGuidParent(), new LinkedHashMap<>());
                }
                if(!results.get(notesData.getThreadGuidParent()).containsKey(notesData.getEntryGuid())) {
                    results.get(notesData.getThreadGuidParent()).put(notesData.getEntryGuid(), new ArrayList<>());
                }
                results.get(notesData.getThreadGuidParent()).get(notesData.getEntryGuid()).add(notesData);
            }
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
                    if ((!query.getArchived() && threads.get(i).getArchived() != null) ||
                            (query instanceof Queries.SearchArchived && threads.get(i).getArchived() == null)) {
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
