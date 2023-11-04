package com.freelance.forum.elasticsearch.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.*;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Search and build response for thread of entries along with update histories. Version 3
 */
@Service("notesProcessorV3")
public class NotesProcessorV3 extends AbstractNotesOperations {

    /**
     * Out of all multiple entries and their threads and histories, figures out and process -
     * 1. Individual entry - which has no parent
     * 2. Threads - which has parent
     * 3. Histories - which has already an older entry, replace it with new and add the current one to histories
     * 4. For single entry request, query is done for all entries created after the requested one. so result set from elasticsearch may contain 
     *    other entries that not belongs to requested entry threads, as they may have been created/updated for others but after this entry is created. 
     *    So further filter is done as below -
     *      - For Single entry request, exclude all other entries that doesn't belongs to the request one. only one record stored in results list
     *        and threadMapping is done only for this record
     *      - For selection of multi entries within requested entry due to some criteria(Ex - only archived entries), 
     *        then create map(archived - for archived filter) that will have only archived entries. 
     *        these entries are presented within the requested entry thread.
     *
     * This method takes O(n) linear time
     * 
     * @param query
     * @return
     */
    @Override
    public List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults) {
        Map<UUID,NotesData> threadMapping = new HashMap<>();
        List<NotesData> results = new LinkedList<>();
        Map<UUID,NotesData> archivedEntries = new HashMap<>();
        boolean firstEntry = true;
        if(esResults != null) {
            while (esResults.hasNext()) {
                NotesData entry = esResults.next().getContent();
                if (filterArchived(query,entry,results)) {
                    continue;
                }
                if (threadMapping.containsKey(entry.getThreadGuid())) {
                    NotesData thread = threadMapping.get(entry.getThreadGuid());
                    addHistory(thread,entry,query);
                } else if(threadMapping.containsKey(entry.getThreadGuidParent())) {
                    addThreads(threadMapping.get(entry.getThreadGuidParent()), entry, query);
                }
                if(!threadMapping.containsKey(entry.getThreadGuid())) {
                    if(!threadMapping.containsKey(entry.getThreadGuidParent())) {
                        if((!(query instanceof SearchByEntryGuid || query instanceof SearchArchivedByEntryGuid) || firstEntry)
                                || query.searchAfter() != null) {
                            firstEntry = false;
                            threadMapping.put(entry.getThreadGuid(),entry);
                            if(!(query instanceof SearchArchivedByEntryGuid) || (entry.getArchived() != null && 
                                    !archivedEntries.containsKey(entry.getThreadGuidParent()))) {
                                addEntries(results, entry, query);
                                archivedEntries.put(entry.getThreadGuid(), entry);
                            }
                        }
                    }
                    if(!(query instanceof SearchArchivedByEntryGuid)) {
                        threadMapping.put(entry.getThreadGuid(),entry);
                    } else if(threadMapping.containsKey(entry.getThreadGuidParent())) {
                        threadMapping.put(entry.getThreadGuid(),entry);
                        if(entry.getArchived() != null && !archivedEntries.containsKey(entry.getThreadGuidParent())) {
                            addEntries(results,entry,query);
                            archivedEntries.put(entry.getThreadGuid(), entry);
                        }
                    }
                }
            }
        }
        return results;
    }
}