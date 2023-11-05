package com.acme.poc.notes.base;

import com.acme.poc.notes.data.ElasticSearchData;
import com.acme.poc.notes.elasticsearch.esrepo.ESNotesRepository;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.service.INotesService;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@Service
public class BaseTest {

    @Autowired
    protected INotesService notesService;

    @Autowired
    protected ESNotesRepository repository;

    protected NotesData createNewEntry(NotesData newExternalEntry) {
        NotesData entryCreated = notesService.saveNew(newExternalEntry);
        assertEquals(newExternalEntry.getExternalGuid(), entryCreated.getExternalGuid());
        assertEquals(newExternalEntry.getContent(), entryCreated.getContent());
        assertEquals(null, entryCreated.getHistory());
        assertEquals(null, entryCreated.getThreads());
        assertEquals(null, entryCreated.getThreadGuidParent());
        assertEquals(null, entryCreated.getArchived());
        assertNotEquals(null, entryCreated.getEntryGuid());
        assertNotEquals(null, entryCreated.getCreated());
        assertNotEquals(null, entryCreated.getThreadGuid());
        return entryCreated;
    }


    protected NotesData createThread(NotesData existingEntry, String content) {
        NotesData newThread = new NotesData();
        newThread.setContent(content);
        newThread.setThreadGuidParent(existingEntry.getThreadGuid());
        NotesData newThreadCreated = notesService.saveNew(newThread);
        assertEquals(newThread.getExternalGuid(), newThreadCreated.getExternalGuid());
        assertEquals(null, newThreadCreated.getHistory());
        assertEquals(null, newThreadCreated.getThreads());
        assertEquals(null, newThreadCreated.getArchived());
        assertEquals(existingEntry.getThreadGuid(), newThreadCreated.getThreadGuidParent());
        assertNotEquals(existingEntry.getEntryGuid(), newThreadCreated.getEntryGuid());
        assertNotEquals(existingEntry.getCreated(), newThreadCreated.getCreated());
        assertNotEquals(existingEntry.getContent(), newThreadCreated.getContent());
        assertNotEquals(existingEntry.getThreadGuid(), newThreadCreated.getThreadGuid());
        return newThreadCreated;
    }

    protected NotesData updateGuid(NotesData existingEntry, String content) {
        NotesData newEntry = new NotesData();
        newEntry.setGuid(existingEntry.getGuid());
        newEntry.setContent(content);
        newEntry.setEntryGuid(existingEntry.getEntryGuid());
        NotesData newThreadUpdated = notesService.update(newEntry);
        assertEquals(existingEntry.getExternalGuid(), newThreadUpdated.getExternalGuid());
        assertEquals(existingEntry.getEntryGuid(), newThreadUpdated.getEntryGuid());
        assertEquals(existingEntry.getThreadGuid(), newThreadUpdated.getThreadGuid());
        assertEquals(existingEntry.getThreadGuidParent(), newThreadUpdated.getThreadGuidParent());
        assertNotEquals(existingEntry.getCreated(), newThreadUpdated.getCreated());
        assertNotEquals(existingEntry.getContent(), newThreadUpdated.getContent());
        return newThreadUpdated;
    }

    protected void validateAll(List<NotesData> result, int entrySize, int expectedTotalCount, int expectedThreadCount, int expectedHistoryCount) {
        List<NotesData> total = new ArrayList<>();
        List<NotesData> totalThreads = new ArrayList<>();
        List<NotesData> totalHistories = new ArrayList<>();
        assertEquals(entrySize,result.size());
        result.forEach(r -> flatten(r, total));
        assertEquals(expectedTotalCount,total.size());
        result.forEach(r -> flattenThreads(r, totalThreads));
        assertEquals(expectedThreadCount,totalThreads.size());
        result.forEach(r -> flattenHistories(r, totalHistories));
        assertEquals(expectedHistoryCount,totalHistories.size());

        // this is to check each individual external entries will have different entryGuid
        for(int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                assertNotEquals(result.get(i).getEntryGuid(),result.get(j).getEntryGuid());
            }
        }

        // this is to check each external entry and its history will have the same entryGuid
        for(int i = 0; i < result.size(); i++) {
            if(result.get(i).getHistory() != null) {
                for (int j = 0; j < result.get(i).getHistory().size(); j++) {
                    assertEquals(result.get(i).getEntryGuid(),result.get(i).getHistory().get(j).getEntryGuid());
                }
            }
        }

        // this is to check each threads will have same thread guid as their parent 
        for(int i = 0; i < result.size(); i++) {
            if(result.get(i).getThreads() != null) {
                for (int j = 0; j < result.get(i).getThreads().size(); j++) {
                    assertEquals(result.get(i).getThreadGuid(),result.get(i).getThreads().get(j).getThreadGuidParent());
                }
            }
        }

        checkDuplicates(result);
    }

    protected void checkDuplicates(List<NotesData> result){
        // check for duplicate entries
        List<NotesData> flattenEntries = new ArrayList<>();
        Set<String> entryCount = new HashSet<>();
        int j = 0;
        for(int i = 0; i < result.size(); i++) {
            flatten(result.get(i),flattenEntries);
            while(j < flattenEntries.size()) {
                String guidKey = flattenEntries.get(j).getGuid().toString();
                if(entryCount.contains(guidKey)) { // this check is for debug just in case
                    assertFalse(entryCount.contains(guidKey));
                }
                entryCount.add(guidKey);
                j++;
            }
        }
    }

    protected void flatten(NotesData root, List<NotesData> entries) {
        entries.add(root);
        if(root.getThreads() != null)
            root.getThreads().forEach(e -> flatten(e,entries));
        if(root.getHistory() != null)
            root.getHistory().forEach(e -> flatten(e,entries));
    }

    protected void flattenThreads(NotesData root, List<NotesData> entries) {
        if(root.getThreads() != null)
            root.getThreads().forEach(e -> {
                entries.add(e);
                flattenThreads(e,entries);
            });
    }

    protected void flattenHistories(NotesData root, List<NotesData> entries) {
        if(root.getThreads() != null)
            root.getThreads().forEach(e -> {
                flattenHistories(e,entries);
            });
        if(root.getHistory() != null)
            root.getHistory().forEach(e -> {
                entries.add(e);
                flattenHistories(e,entries);
            });
    }

    protected Map<String,NotesData> getEntries() {
        Map<String,NotesData> entries = new HashMap<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
            for(int i=0; i< jsonArray.length(); i++) {
                NotesData data = NotesData.fromJson(jsonArray.getString(i));
                entries.put(data.getGuid().toString(),data);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }
}
