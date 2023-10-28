package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.IQuery;
import com.freelance.forum.elasticsearch.queries.Queries;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Search and build response for thread of entries along with update histories. Version 2
 */
@Service("searchNotesV3")
public class SearchNotesV3 extends AbstractSearchNotes {

    /**
     * Strategy is to search for root entry first(for externalGuid/entryGuid), then call for all subsequent entries created after for given guid.
     * then while reading responses, threads and histories are built.
     *      
     * This build method takes O(n+m)(where m is number of root entries).
     * 
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
        for (Map<UUID, NotesData> entries : rootEntriesMap.values()) {
            for (NotesData veryFirstEntry : entries.values()) {
                if (query.getArchived() || veryFirstEntry.getArchived() == null) {
                    // Since query is by external id, we only need results after first of entry these entries,
                    IQuery entryQuery = new Queries.SearchByEntryGuid()
                            .setGetArchived(query.getArchived())
                            .setGetUpdateHistory(query.getUpdateHistory())
                            .setEntryGuid(veryFirstEntry.getExternalGuid().toString())
                            .setSearchField(ESIndexNotesFields.ENTRY).setSearchField(ESIndexNotesFields.EXTERNAL)
                            .setSortOrder(SortOrder.ASC)
                            .setCreatedDateTime(veryFirstEntry.getCreated().getTime());
                    prepThreadsAndHistories(query,entryQuery, veryFirstEntry);
                    results.add(veryFirstEntry);
                }
            }
        }
        if (query instanceof Queries.SearchArchived) {
            results = filterArchived(results);
        }
        if (results.isEmpty()) {
            System.out.println("No entries found for given request");
        }
        return results;
    }

    /**
     * Prepare map of all entries in such a way that, entry(or multiple external entries) with threads and history can be built with O(n) time complexity
     * @param query
     * @return
     */
    private NotesData prepThreadsAndHistories(IQuery mainQuery, IQuery query,NotesData mostRecentUpdatedEntry) {
        Iterator<SearchHit<NotesData>> threads = null;
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        if(searchHits != null)
            threads = searchHits.stream().iterator();
        Map<UUID,Map<UUID,NotesData>> results = new HashMap<>();
        results.put(mostRecentUpdatedEntry.getThreadGuid(),new HashMap<>());
        results.get(mostRecentUpdatedEntry.getThreadGuid()).put(mostRecentUpdatedEntry.getThreadGuid(),mostRecentUpdatedEntry);
        if(threads != null) {
            while (threads.hasNext()) {
                NotesData notesData = threads.next().getContent();
                if ((!query.getArchived() && notesData.getArchived() != null) || 
                        (mainQuery instanceof Queries.SearchArchived && notesData.getArchived() == null)) {
                    // Discard archived entries OR Select only archived entries
                    continue;
                }
                if (results.containsKey(notesData.getThreadGuid())) {
                    NotesData thread = results.get(notesData.getThreadGuid()).get(notesData.getThreadGuid());
                    addHistory(thread,notesData,query);
                } else if(results.containsKey(notesData.getThreadGuidParent()) && 
                        results.get(notesData.getThreadGuidParent()).containsKey(notesData.getThreadGuidParent())) {
                    results.get(notesData.getThreadGuidParent()).get(notesData.getThreadGuidParent()).addThreads(notesData);
                } 
                if(!results.containsKey(notesData.getThreadGuid())) {
                    results.put(notesData.getThreadGuid(),new HashMap<>());
                    results.get(notesData.getThreadGuid()).put(notesData.getThreadGuid(),notesData);
                }
            }
        }
        return mostRecentUpdatedEntry;
    }
}
