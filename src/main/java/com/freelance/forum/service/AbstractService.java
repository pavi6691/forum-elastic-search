package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.configuration.EsConfig;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.Request;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public abstract class AbstractService<T> implements ISearchService {

    @Value("${index.name}")
    public String indexName;

    @Value("${max.number.of.history.and.threads}")
    public int max_number_of_history_and_threads;

    @Autowired
    EsConfig esConfig;
    
    public abstract Iterator<T> execQueryOnEs(String query);
    public abstract NotesData parseSearchHitToNoteData(T searchHit);
    public Map<String,List<NotesData>> getThreads(String queryByExternalId, Request request) {
        Iterator<T> threads = execQueryOnEs(queryByExternalId);
        Map<String,List<NotesData>> results = new HashMap<>();
        if(threads != null) {
            while (threads.hasNext()) {
                NotesData notesData = parseSearchHitToNoteData(threads.next());
                String uuid = null; 
                if(notesData.getThreadGuidParent() != null) {
                    uuid  = notesData.getThreadGuidParent().toString();
                }
                if (!results.containsKey(uuid)) {
                    results.put(uuid, new ArrayList<>()); // null key is allowed and holds root element
                }
                results.get(uuid).add(notesData);
            }
        }
        return results;
    }

    @Override
    public List<NotesData> search(Request request) {
        List<NotesData> results = new ArrayList<>();
        Iterator<T> rootEntries = execQueryOnEs(getRootQuery(request));
        Map<String,List<NotesData>> rootEntriesMap = new HashMap<>();
        if(rootEntries != null) {
            while (rootEntries.hasNext()) {
                NotesData rootNotesData = parseSearchHitToNoteData(rootEntries.next());
                String externalUuid = rootNotesData.getExternalGuid().toString();
                if (!rootEntriesMap.containsKey(externalUuid)) {
                    rootEntriesMap.put(externalUuid, new ArrayList<>());
                }
                rootEntriesMap.get(externalUuid).add(rootNotesData);
            }
            for (List<NotesData> entries : rootEntriesMap.values()) {
                NotesData mostRecentUpdatedEntry = null;
                for (int i = 0; i < entries.size(); i++) {
                    if (i == 0) {
                        mostRecentUpdatedEntry = entries.get(i);
                        // Since query is by external id, we only need results after first of entry these entries,
                        String queryByExternalId = String.format(Queries.QUERY_ALL_ENTRIES, ESIndexNotesFields.EXTERNAL.getEsFieldName(),
                                mostRecentUpdatedEntry.getExternalGuid(), entries.get(entries.size() - 1).getCreated().getTime());
                        Map<String, List<NotesData>> threads = getThreads(queryByExternalId, request);
                        if (threads != null && !threads.isEmpty()) {
                            results.add(buildThreads(mostRecentUpdatedEntry, threads, new HashSet<>(), request));
                        }
                    }
                }
            }
        }
        return results;
    }
    
    private String getRootQuery(Request request) {
        String query = "";
        if(request.getEsIndexNotesField() == ESIndexNotesFields.EXTERNAL) {
            query = String.format(Queries.QUERY_ROOT_EXTERNAL_ENTRIES, request.getSearch());
        } else if(request.getEsIndexNotesField() == ESIndexNotesFields.ENTRY) {
            query = String.format(Queries.QUERY_ALL_ENTRIES,request.getEsIndexNotesField().getEsFieldName(), request.getSearch(),0);
        } else if(request.getEsIndexNotesField() == ESIndexNotesFields.CONTENT) {
            query = String.format(Queries.QUERY_CONTENT_ROOT_EXTERNAL_ENTRIES, request.getSearch());
        }
        return query;
    }

    private NotesData buildThreads(NotesData threadEntry,Map<String, List<NotesData>> results, Set<String> entryThreadUuid,Request request) {
        String parentThreadGuid = null;
        if(threadEntry.getThreadGuidParent() != null) {
            parentThreadGuid  = threadEntry.getThreadGuidParent().toString();
        }
        if(request.isUpdateHistory() && threadEntry != null && results.containsKey(parentThreadGuid)) {
            for(int i = 1; i < results.get(parentThreadGuid).size(); i++) {
                threadEntry.addHistory(results.get(parentThreadGuid).get(i));
            }
        }
        List<NotesData> threads = results.get(threadEntry.getThreadGuid().toString());
        if(threads != null) {
            for(int i = 0; i < threads.size(); i++) {
                String entryUuid = threads.get(i).getEntryGuid().toString();
                // entryThreadUuid set is to make sure to avoid history entries here as search Entry id will have history entries as well
                if (!entryThreadUuid.contains(entryUuid)) {
                    if (!request.isArchivedResponse() && threads.get(i).getArchived() != null) {
                        break; // do not search archived thread
                    }
                    threadEntry.addThreads(threads.get(i));
                    entryThreadUuid.add(entryUuid);
                    buildThreads(threads.get(i), results, entryThreadUuid, request);
                }
            }
        }
        return threadEntry;
    }
    
}
