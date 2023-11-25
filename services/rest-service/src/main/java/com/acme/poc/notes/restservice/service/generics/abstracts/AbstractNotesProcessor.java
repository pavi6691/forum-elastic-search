package com.acme.poc.notes.restservice.service.generics.abstracts;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByExternalGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByThreadGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.enums.ResultFormat;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


/**
 * Abstraction for executing search query.
 */
@Slf4j
@Service
public abstract class AbstractNotesProcessor<E extends INoteEntity<E>> {
    
    /**
     * 1. Gets all entries along with threads and histories for externalGuid and content field
     * 2. Process response and build threads and histories
     *
     * @param query - query containing search details
     * @return list of entries with threads and histories
     * - filter only archived
     * - filter only history
     * - filter both archived and histories
     */
    protected List<E> getProcessed(IQuery query) {
        log.debug("Fetching entries for request = {}", query.getClass().getSimpleName());
        List<E> searchHits = getUnprocessed(query);
        if (searchHits != null && searchHits.size() > 0) {
            log.debug("Number of results from elastic search = {}", searchHits.size());
            return process(query, searchHits.iterator());
        } else {
            log.error("no results found for request = {}", query.getClass().getSimpleName());
        }
        return new ArrayList<>();
    }

    /**
     * Execute search queries on elastic search
     * It's not possible to search all threads and history entries by entryGuid. So
     *   - Search for main(root) entry first(for externalGuid/entryGuid), 
     *   - then call for all subsequent entries by externalGuid and entries created after main entry that's requested.
     *   - there will be some other entries created after and updated later, handled in process method
     *
     * @param query
     * @return
     */
    protected List<E> getUnprocessed(IQuery query) {
        List<E> searchHits = null;
        try {
            searchHits = execSearchQuery(query);
            if (query instanceof SearchByEntryGuid || query instanceof SearchArchivedByEntryGuid) {
                // Search by entryGuid doesn't fetch all entries, so fetch by externalEntry and created after this entry
                if (searchHits != null) {
                    Iterator<E> rootEntries = searchHits.stream().iterator();
                    if (rootEntries != null && rootEntries.hasNext()) {
                        E rootEntry = rootEntries.next();
                        if (query.includeArchived() || rootEntry.getArchived() == null) { // Need results after first of entry these entries,
                            IQuery entryQuery = SearchByThreadGuid.builder()
                                    .size(query.getSize())
                                    .sortOrder(query.getSortOrder())
                                    .searchAfter(((AbstractQuery) query).getSearchAfter())
                                    .includeArchived(query.includeArchived())
                                    .includeVersions(query.includeVersions())
                                    .searchGuid(rootEntry.getThreadGuid().toString())
                                    .createdDateTime(rootEntry.getCreated().getTime())
                                    .build();
                            searchHits = execSearchQuery(entryQuery);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throwRestError(NotesAPIError.ERROR_SERVER);
        }
        return searchHits;
    }

    /**
     * Executes IQuery
     * @param query 
     * @return search result from elastics search response
     */
    protected abstract List<E> execSearchQuery(IQuery query);

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
    protected List<E> process(IQuery query, Iterator<E> esResults) {
        log.debug("Processing request = {}", query.getClass().getSimpleName());
        Map<UUID, E> threadMapping = new HashMap<>();
        List<E> results = new LinkedList<>();
        Set<UUID> entriesAddedToResults = new HashSet<>();
        if (esResults != null) {
            while (esResults.hasNext()) {
                E entry = esResults.next();
                if (filterArchived(query, entry, results)) {
                    continue;
                }
                if (threadMapping.containsKey(entry.getEntryGuid())) {
                    updateVersions(threadMapping.get(entry.getEntryGuid()), entry, query,results);
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

    /**
     * Check if request needs all versions of entries, 
     * then accommodate updated entry and include all previous versions in versions list
     *
     * @param existingEntry
     * @param updatedEntry
     * @param query
     */
    protected void updateVersions(E existingEntry, E updatedEntry, IQuery query,Collection<E> results) {
        if (!existingEntry.getGuid().equals(updatedEntry.getGuid())) {
            if (query.includeVersions()) {
                E history = existingEntry.getInstance(); // TODO is getInstance proper way? make these setters dynamic, when any field added/removed
                history.setGuid(existingEntry.getGuid());
                history.setExternalGuid(existingEntry.getExternalGuid());
                history.setThreadGuid(existingEntry.getThreadGuid());
                history.setEntryGuid(existingEntry.getEntryGuid());
                history.setEntryGuidParent(existingEntry.getEntryGuidParent());
                history.setType(existingEntry.getType());
                history.setArchived(existingEntry.getArchived());
                history.setContent(existingEntry.getContent());
                history.setCreated(existingEntry.getCreated());
                history.setCustomJson(existingEntry.getCustomJson());
                if (query.getResultFormat() == ResultFormat.TREE) {
                    if (query.getSortOrder() == SortOrder.ASC) {
                        existingEntry.addHistory(history, existingEntry.getHistory() != null ? existingEntry.getHistory().size() : 0);
                    } else {
                        existingEntry.addHistory(history, 0);
                    }
                } else if(query.getResultFormat() == ResultFormat.FLATTEN) {
                    results.add(history);
                }
            }
            existingEntry.setGuid(updatedEntry.getGuid());
            existingEntry.setContent(updatedEntry.getContent());
            existingEntry.setCreated(updatedEntry.getCreated());
            existingEntry.setArchived(updatedEntry.getArchived());
        }
    }

    /**
     * This method is called when there is a child for the existing entry. so add it
     *
     * @param existingEntry
     * @param newEntry
     * @param query
     */
    protected void addChild(E existingEntry,E newEntry, IQuery query) {
        if (query.getSortOrder() == SortOrder.ASC) {
            existingEntry.addThreads(newEntry, existingEntry.getThreads() != null ? existingEntry.getThreads().size() : 0);
        } else {
            existingEntry.addThreads(newEntry, 0);
        }
    }

    /**
     * This creates new thread within same entryGuid/externalGuid. 
     * Multiple threads are tend to be created when not all entries are selected but for specific criteria. ex - archived
     *
     * @param results
     * @param newEntry
     * @param query
     */
    protected void addNewThread(List<E> results,E newEntry, IQuery query) {
        if(query.getResultFormat() == ResultFormat.TREE) {
            if (query.getSortOrder() == SortOrder.ASC) {
                results.add(newEntry);
            } else {
                results.add(0, newEntry);
            }
        } else if(query.getResultFormat() == ResultFormat.FLATTEN) {
            results.add(newEntry);
        }
    }

    /**
     * Either discard archived entries OR Select only archived entries
     * @param query
     * @param entry
     * @return true when not to select archived entry. And true for only archived request
     */
    protected boolean filterArchived(IQuery query, E entry, Collection<E> results) {
        return ((!query.includeArchived() && entry.getArchived() != null) ||
                (query instanceof SearchArchivedByExternalGuid) && entry.getArchived() == null && !results.isEmpty());
    }

    protected static boolean getTimeUnit(long timeValue) {
        long millisInUnit = TimeUnit.MILLISECONDS.toMillis(1);
        if (timeValue % millisInUnit == 0) {
            return true;
        }
        return false; // Unknown time unit
    }

}
