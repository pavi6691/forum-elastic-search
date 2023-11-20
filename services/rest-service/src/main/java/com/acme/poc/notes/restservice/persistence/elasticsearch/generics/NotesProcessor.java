package com.acme.poc.notes.restservice.persistence.elasticsearch.generics;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.enums.ResultFormat;

import java.util.*;


/**
 * Search and build response for thread of entries along with update histories. Version 3
 */
@Slf4j
@Service("NotesProcessor")
public class NotesProcessor<E> extends AbstractNotesProcessor<E> {


    /**
     * Out of all multiple entries and their threads and histories, figures out and process -
     * 1. Individual entry - which has no parent
     * 2. Threads - which has parent
     * 3. Histories - which has already an older entry, replace it with new and add the current one to histories
     * 4. For request by entryGuid, query gets all entries that are created after the requested one. so result set from elasticsearch may contain 
     *    other entries that not belongs to requested entry thread. as they may have been created/updated for others but after this entry is created. 
     *    So further filter is done as below -
     *      - For entry request, exclude all other entries that doesn't belongs to the request one. only one record stored in results list
     *        and threadMapping is done only for this record
     *      - For selection of multi entries within requested entry due to some criteria(Ex - only archived entries), 
     *        then map(archived - for archived filter) that will have only archived entries. 
     *        these entries are presented within the requested entry thread.
     *
     * <p>This method takes O(n) linear time</p>
     * 
     * @param query
     * @return process results in tree format if {@link ResultFormat#TREE} else FLATTEN if {@link ResultFormat#FLATTEN}
     */
    @Override
    public List<NotesData> process(IQuery query, Iterator<SearchHit<E>> esResults) {
        log.debug("Processing request = {}", query.getClass().getSimpleName());
        Map<UUID, NotesData> threadMapping = new HashMap<>();
        List<NotesData> results = new LinkedList<>();
        Set<UUID> entriesAddedToResults = new HashSet<>();
        if (esResults != null) {
            while (esResults.hasNext()) {
                NotesData entry = (NotesData) esResults.next().getContent();
                if (filterArchived(query, entry, results)) {
                    continue;
                }
                if (threadMapping.containsKey(entry.getEntryGuid())) {
                    NotesData thread = threadMapping.get(entry.getEntryGuid());
                    updateVersions(thread, entry, query,results);
                } else if (threadMapping.containsKey(entry.getEntryGuidParent())) {
                    if(query.getResultFormat() == ResultFormat.TREE) {
                        addChild(threadMapping.get(entry.getEntryGuidParent()), entry, query);
                    } else if(query.getResultFormat() == ResultFormat.FLATTEN) {
                        if(entriesAddedToResults.contains(entry.getEntryGuidParent())) {
                            entriesAddedToResults.add(entry.getEntryGuid());
                            results.add(entry);
                        }
                    }
                }
                if (!threadMapping.containsKey(entry.getEntryGuid())) {
                    if (!threadMapping.containsKey(entry.getEntryGuidParent())) {
                        // New thread, for SearchByEntryGuid, SearchArchivedByEntryGuid only first entry
                        if ((!(query instanceof SearchByEntryGuid || query instanceof SearchArchivedByEntryGuid) || threadMapping.isEmpty()) || 
                                query.searchAfter() != null) {
                            threadMapping.put(entry.getEntryGuid(), entry);
                            if (!(query instanceof SearchArchivedByEntryGuid) || (entry.getArchived() != null && 
                                    !entriesAddedToResults.contains(entry.getEntryGuidParent()))) {
                                addNewThread(results, entry, query);
                                entriesAddedToResults.add(entry.getEntryGuid());
                            }
                        }
                    }
                    // This is the specific use case to find archived entries by entryGuid
                    if (!(query instanceof SearchArchivedByEntryGuid)) {
                        threadMapping.put(entry.getEntryGuid(), entry);
                    } else if (threadMapping.containsKey(entry.getEntryGuidParent())) {
                        threadMapping.put(entry.getEntryGuid(), entry);
                        if (entry.getArchived() != null && !entriesAddedToResults.contains(entry.getEntryGuidParent()) && 
                                threadMapping.get(entry.getEntryGuidParent()).getArchived() == null) {
                            addNewThread(results, entry, query);
                            entriesAddedToResults.add(entry.getEntryGuid());
                        }
                    }
                }
            }
        }
        return results;
    }

}
