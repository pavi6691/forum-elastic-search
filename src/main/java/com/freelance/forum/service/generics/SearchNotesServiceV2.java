package com.freelance.forum.service.generics;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.IQuery;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.elasticsearch.queries.RequestType;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("searchNotesServiceV2")
public class SearchNotesServiceV2 extends AbstractSearchNotesService {

    @Override
    public List<NotesData> search(IQuery query) {
        List<NotesData> results = new ArrayList<>();
        Iterator<SearchHit<NotesData>> rootEntries = null;
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        if(searchHits != null)
            rootEntries = searchHits.stream().iterator();
        Map<String,Map<String,List<NotesData>>> rootEntriesMap = new HashMap<>();
        if(rootEntries != null) {
            while (rootEntries.hasNext()) {
                NotesData rootNotesData = rootEntries.next().getContent();
                if(query.getRequestType() == RequestType.CONTENT) {
                    results.add(rootNotesData);  
                    continue;
                }
                String externalUuid = rootNotesData.getExternalGuid().toString();
                if (!rootEntriesMap.containsKey(externalUuid)) {
                    rootEntriesMap.put(externalUuid, new HashMap<>());
                }
                String entryUuid = rootNotesData.getEntryGuid().toString();
                if(!rootEntriesMap.get(externalUuid).containsKey(entryUuid)) {
                    rootEntriesMap.get(externalUuid).put(entryUuid, new ArrayList<>());
                }
                rootEntriesMap.get(externalUuid).get(entryUuid).add(rootNotesData);
            }
            for (Map<String,List<NotesData>> entries : rootEntriesMap.values()) {
                NotesData mostRecentUpdatedEntry = null;
                for (List<NotesData> entryList : entries.values()) {
                    if (entryList.size() > 0) {
                        mostRecentUpdatedEntry = entryList.get(0);
                        if (query.getArchived() || mostRecentUpdatedEntry.getArchived() == null) {
                            // Since query is by external id, we only need results after first of entry these entries,
                            Map<String, Map<String, List<NotesData>>> threads = getThreads(new Queries.SearchByEntryGuid()
                                    .setEntryGuid(mostRecentUpdatedEntry.getExternalGuid().toString())
                                    .setSearchField(ESIndexNotesFields.ENTRY).setSearchField(ESIndexNotesFields.EXTERNAL)
                                    .setCreatedDateTime(entryList.get(entryList.size() - 1).getCreated().getTime()));
                            if (threads != null && !threads.isEmpty()) {
                                results.add(buildThreads(mostRecentUpdatedEntry, threads, new HashSet<>(), query));
                            }
                        }
                    }
                }
            }
        }
        if(query.getRequestType() == RequestType.ARCHIVE) {
            filterArchived(results);
        } 
        if(results.isEmpty()) {
            System.out.println("No entries found for given request");
        }
        return results;
    }
    private Map<String,Map<String,List<NotesData>>> getThreads(IQuery query) {
        Iterator<SearchHit<NotesData>> threads = null;
        SearchHits<NotesData> searchHits = execSearchQuery(query);
        if(searchHits != null)
            threads = searchHits.stream().iterator();
        Map<String,Map<String,List<NotesData>>> results = new HashMap<>();
        if(threads != null) {
            while (threads.hasNext()) {
                NotesData notesData = threads.next().getContent();
                String uuid = null;
                if(notesData.getThreadGuidParent() != null) {
                    uuid  = notesData.getThreadGuidParent().toString();
                }
                if (!results.containsKey(uuid)) {
                    // null key is allowed and holds root element
                    results.put(uuid, new LinkedHashMap<>());
                }
                String entryGuid = notesData.getEntryGuid().toString();
                if(!results.get(uuid).containsKey(entryGuid)) {
                    results.get(uuid).put(entryGuid, new ArrayList<>());
                }
                results.get(uuid).get(entryGuid).add(notesData);
            }
        }
        return results;
    }

    private NotesData buildThreads(NotesData threadEntry, Map<String,Map<String,List<NotesData>>> results, Set<String> entryThreadUuid, IQuery query) {
        String parentThreadGuid = null;
        if(threadEntry.getThreadGuidParent() != null) {
            parentThreadGuid  = threadEntry.getThreadGuidParent().toString();
        }
        String entryGuid = threadEntry.getEntryGuid().toString();
        if(query.getUpdateHistory() && threadEntry != null && results.containsKey(parentThreadGuid) &&
                results.get(parentThreadGuid).containsKey(entryGuid)) {
            for(int i = 1; i < results.get(parentThreadGuid).get(entryGuid).size(); i++) { 
                threadEntry.addHistory(results.get(parentThreadGuid).get(entryGuid).get(i));
            }
        }
        List<NotesData> threads = new ArrayList<>();
        if(threadEntry.getThreadGuid() != null && results.containsKey(threadEntry.getThreadGuid().toString())) {
            results.get(threadEntry.getThreadGuid().toString()).values().stream().forEach(l -> threads.addAll(l));
        }
        if(!threads.isEmpty()) {
            for(int i = 0; i < threads.size(); i++) {
                String entryUuid = threads.get(i).getEntryGuid().toString();
                // entryThreadUuid set is to make sure to avoid history entries here as search Entry id will have history entries as well
                if (!entryThreadUuid.contains(entryUuid)) {
                    if (!query.getArchived() && threads.get(i).getArchived() != null) {
                        break; // do not search archived thread
                    }
                    threadEntry.addThreads(threads.get(i));
                    entryThreadUuid.add(entryUuid);
                    buildThreads(threads.get(i), results, entryThreadUuid, query);
                }
            }
        }
        return threadEntry;
    }
    
}
