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
@Service("searchNotesV3")
public class SearchNotesV3 extends AbstractSearchNotes {

    /**
     * Strategy is to search for root entry first(for externalGuid/entryGuid), then call for all subsequent entries created after for given guid.
     * then while reading responses, threads and histories are built.
     *      
     * This build method takes O(n+m) -> where m is number of root entries.
     * 
     * @param mainQuery - query containing search details
     * @return entries with all threads and histories
     * - can return only archived
     * - can return only history
     * - can return both archived and histories
     */
    @Override
    public List<NotesData> search(IQuery mainQuery) {
        List<NotesData> results = new ArrayList<>();
        Map<UUID, Map<UUID,List<NotesData>>> rootEntriesMap = getRootEntries(mainQuery);
        for (Map<UUID, List<NotesData>> entries : rootEntriesMap.values()) {
            for (List<NotesData> firstEntryAtIndexZero : entries.values()) {
                if (!firstEntryAtIndexZero.isEmpty() && (mainQuery.getArchived() || firstEntryAtIndexZero.get(0).getArchived() == null)) {
                    // Since query is by external id, we only need results after first of entry these entries,
                    IQuery entryQuery = new Queries.SearchByEntryGuid()
                            .setGetArchived(mainQuery.getArchived())
                            .setGetUpdateHistory(mainQuery.getUpdateHistory())
                            .setSearchField(ESIndexNotesFields.EXTERNAL)
                            .setSearchBy(firstEntryAtIndexZero.get(0).getExternalGuid().toString())
                            .setCreatedDateTime(firstEntryAtIndexZero.get(0).getCreated().getTime());
                    if (mainQuery instanceof Queries.SearchByEntryGuid) {
                        results.add(prepThreadsAndHistories(mainQuery,entryQuery).get(0));
                    } else {
                        results.addAll(prepThreadsAndHistories(mainQuery,entryQuery));
                    }
                }
                break;
            }
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
    private List<NotesData> prepThreadsAndHistories(IQuery mainQuery, IQuery query) {
        Iterator<SearchHit<NotesData>> threads = null;
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        List<NotesData> results = new ArrayList<>();
        if(searchHits != null)
            threads = searchHits.stream().iterator();
        Map<UUID,Map<UUID,NotesData>> buildThreadsAndHistory = new HashMap<>();
        if(threads != null) {
            while (threads.hasNext()) {
                NotesData notesData = threads.next().getContent();
                if ((!query.getArchived() && notesData.getArchived() != null) ||
                    ((mainQuery instanceof Queries.SearchArchivedByEntryGuid || 
                            mainQuery instanceof Queries.SearchArchivedByExternalGuid) && notesData.getArchived() == null)) {
                    // Either discard archived entries OR Select only archived entries
                    continue;
                }
                if (buildThreadsAndHistory.containsKey(notesData.getThreadGuid())) {
                    NotesData thread = buildThreadsAndHistory.get(notesData.getThreadGuid()).get(notesData.getThreadGuid());
                    addHistory(thread,notesData,query);
                } else if(buildThreadsAndHistory.containsKey(notesData.getThreadGuidParent()) &&
                        buildThreadsAndHistory.get(notesData.getThreadGuidParent()).containsKey(notesData.getThreadGuidParent())) {
                    buildThreadsAndHistory.get(notesData.getThreadGuidParent()).get(notesData.getThreadGuidParent()).addThreads(notesData);
                } 
                if(!buildThreadsAndHistory.containsKey(notesData.getThreadGuid())) {
                    buildThreadsAndHistory.put(notesData.getThreadGuid(),new HashMap<>());
                    buildThreadsAndHistory.get(notesData.getThreadGuid()).put(notesData.getThreadGuid(),notesData);
                    if(!buildThreadsAndHistory.containsKey(notesData.getThreadGuidParent())) {
                        results.add(notesData);
                    }
                }
            }
        }
        return results;
    }
}
