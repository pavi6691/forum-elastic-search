package com.acme.poc.notes.restservice.generics.abstracts;

import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.util.ESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.acme.poc.notes.restservice.util.ExceptionUtil.throwRestError;


/**
 * Abstraction for executing search query. And has implementation for processing raw response from database and provide tree structured output
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
    protected List<E> getProcessed(IQueryRequest query) {
        log.debug("Fetching entries for request = {}", query.getClass().getSimpleName());
        List<E> searchHits = getUnprocessed(query);
        if (searchHits != null && searchHits.size() > 0) {
            log.debug("Number of results from db search = {}", searchHits.size());
            return process(query, searchHits.iterator());
        } else {
            log.error("no results found for request = {}", query.getClass().getSimpleName());
        }
        return new ArrayList<>();
    }

    /**
     * Execute search queries on database
     * It's not possible to search all threads and history entries by entryGuid. So
     *   - Search for main(root) entry first(for externalGuid/entryGuid), 
     *   - then call for all subsequent entries by externalGuid and entries created after main entry that's requested.
     *   - there will be some other entries created after and updated later, handled in process method
     *
     * @param query
     * @return
     */
    protected List<E> getUnprocessed(IQueryRequest query) {
        List<E> searchHits = null;
        try {
            searchHits = search(query);
            if (query.getSearchField().equals(Field.ENTRY)) {
                // Search by entryGuid doesn't fetch all entries, so fetch by externalEntry and created after this entry
                if (searchHits != null) {
                    Iterator<E> rootEntries = searchHits.stream().iterator();
                    if (rootEntries != null && rootEntries.hasNext()) {
                        E rootEntry = rootEntries.next();
                        if (query.getFilters().contains(Filter.INCLUDE_ARCHIVED) ||
                                query.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED) || rootEntry.getArchived() == null) { // Need results after first of entry these entries,
                            IQueryRequest entryQuery = QueryRequest.builder()
                                    .searchField(Field.THREAD)
                                    .size(query.getSize())
                                    .sortOrder(query.getSortOrder())
                                    .searchAfter(((QueryRequest) query).getSearchAfter())
                                    .filters(query.getFilters())
                                    .searchData(rootEntry.getThreadGuid().toString())
                                    .createdDateTime(rootEntry.getCreated().getTime())
                                    .build();
                            searchHits = search(entryQuery);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("Error: {}", e);
            throwRestError(NotesAPIError.ERROR_SERVER, e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getMessage());
        }
        return searchHits;
    }

    /**
     * Executes IQueryRequest
     * @param query 
     * @return search result from database
     */
    protected abstract List<E> search(IQueryRequest query);

    /**
     * Out of all multiple entries and their threads and histories, figures out and process -
     * 1. Individual entry - which has no parent
     * 2. Threads - which has parent
     * 3. Histories - which has already an older entry, replace it with new and add the current one to histories
     * 4. For request by entryGuid, query gets all entries that are created after the requested one. so result set from database may contain 
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
    protected List<E> process(IQueryRequest query, Iterator<E> esResults) {
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
                    if (query.getResultFormat() == ResultFormat.TREE) {
                        addChild(threadMapping.get(entry.getEntryGuidParent()), entry, query);
                    } else if (query.getResultFormat() == ResultFormat.FLATTEN) {
                        if (entriesAddedToResults.contains(entry.getEntryGuidParent())) {
                            entriesAddedToResults.add(entry.getEntryGuid());
                            results.add(entry);
                        }
                    }
                }
                if (!threadMapping.containsKey(entry.getEntryGuid())) {
                    if (!threadMapping.containsKey(entry.getEntryGuidParent())) {
                        // New thread, for SearchByEntryGuid, SearchArchivedByEntryGuid only first entry
                        if ((!(query.getSearchField().equals(Field.ENTRY)) || threadMapping.isEmpty()) ||
                                query.getSearchAfter() != null) {
                            threadMapping.put(entry.getEntryGuid(), entry);
                            if (!(query.getSearchField().equals(Field.ENTRY) && query.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED)) || 
                                    (entry.getArchived() != null &&
                                    !entriesAddedToResults.contains(entry.getEntryGuidParent()))) {
                                addNewThread(results, entry, query);
                                entriesAddedToResults.add(entry.getEntryGuid());
                            }
                        }
                    }
                    // This is the specific use case to find archived entries by entryGuid
                    if (!(query.getSearchField().equals(Field.ENTRY) && query.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED))) {
                        threadMapping.put(entry.getEntryGuid(), entry);
                    } else if (threadMapping.containsKey(entry.getEntryGuidParent())) {
                        threadMapping.put(entry.getEntryGuid(), entry);
                        if (entry.getArchived() != null && !entriesAddedToResults.contains(entry.getEntryGuidParent()) && threadMapping.get(entry.getEntryGuidParent()).getArchived() == null) {
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
    protected void updateVersions(E existingEntry, E updatedEntry, IQueryRequest query, Collection<E> results) {
        if (!existingEntry.getGuid().equals(updatedEntry.getGuid())) {
            if (query.getFilters().contains(Filter.INCLUDE_VERSIONS)) {
                E history = existingEntry.copyThis();
                ESUtil.clearHistoryAndThreads(history);
                if (query.getResultFormat() == ResultFormat.TREE) {
                    if (query.getSortOrder() == NoteSortOrder.ASCENDING) {
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
    protected void addChild(E existingEntry,E newEntry, IQueryRequest query) {
        if (query.getSortOrder() == NoteSortOrder.ASCENDING) {
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
    protected void addNewThread(List<E> results,E newEntry, IQueryRequest query) {
        if (query.getResultFormat() == ResultFormat.TREE) {
            if (query.getSortOrder() == NoteSortOrder.ASCENDING) {
                results.add(newEntry);
            } else {
                results.add(0, newEntry);
            }
        } else if (query.getResultFormat() == ResultFormat.FLATTEN) {
            results.add(newEntry);
        }
    }

    /**
     * Either discard archived entries OR Select only archived entries
     * @param query
     * @param entry
     * @return true when not to select archived entry. And true for only archived request
     */
    protected boolean filterArchived(IQueryRequest query, E entry, Collection<E> results) {
        return ((!query.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED) && !query.getFilters().contains(Filter.INCLUDE_ARCHIVED) && 
                        entry.getArchived() != null) ||
                (query.getSearchField().equals(Field.EXTERNAL) && query.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED)) 
                        && entry.getArchived() == null && !results.isEmpty());
    }

    protected static boolean getTimeUnit(long timeValue) {
        long millisInUnit = TimeUnit.MILLISECONDS.toMillis(1);
        if (timeValue % millisInUnit == 0) {
            return true;
        }
        return false; // Unknown time unit
    }

}
