package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.*;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Search and build response for thread of entries along with update histories. Version 3
 */
@Service("searchNotesV3")
public class SearchNotesV3 extends AbstractSearchNotes {

    /**
     * Out of all multiple entries and their threads and histories, intelligently figures out and process -
     * 1. Individual entry - which has no parent
     * 2. Threads - which has parent
     * 3. Histories - which has already an older entry, replace it with new and add the current one to histories
     * Takes only O(n) time
     * @param query
     * @return
     */
    @Override
    public List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults) {
        Map<UUID,Map<UUID,NotesData>> buildThreadsAndHistory = new HashMap<>();
        List<NotesData> results = new ArrayList<>();
        if(esResults != null) {
            while (esResults.hasNext()) {
                NotesData notesData = esResults.next().getContent();
                if ((!query.getArchived() && notesData.getArchived() != null) || 
                        ((query instanceof SearchArchivedByEntryGuid ||
                                query instanceof SearchArchivedByExternalGuid) && notesData.getArchived() == null)) {
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
                    if(!buildThreadsAndHistory.containsKey(notesData.getThreadGuidParent()) &&
                            (results.isEmpty() || !(query instanceof SearchByEntryGuid))) {
                        results.add(notesData);
                    }
                }
            }
        }
        return results;
    }
}
