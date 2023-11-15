package com.acme.poc.notes.restservice.base;

import com.acme.poc.notes.restservice.persistence.elasticsearch.esrepo.ESNotesRepository;
import com.acme.poc.notes.restservice.persistence.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.restservice.data.ElasticSearchData;
import com.acme.poc.notes.restservice.service.INotesAdminService;
import com.acme.poc.notes.restservice.service.INotesService;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class BaseTest {

    @Autowired
    protected INotesService notesService;
    @Autowired
    protected INotesAdminService notesAdminService;
    @Autowired
    protected ESNotesRepository repository;


    protected NotesData createNewEntry(NotesData newExternalEntry) {
        NotesData entryCreated = notesService.create(newExternalEntry);
        assertEquals(newExternalEntry.getExternalGuid(), entryCreated.getExternalGuid());
        assertEquals(newExternalEntry.getContent(), entryCreated.getContent());
        assertNull(entryCreated.getHistory());
        assertNull(entryCreated.getThreads());
        assertNull(entryCreated.getEntryGuidParent());
        assertNull(entryCreated.getArchived());
        assertNotNull(entryCreated.getEntryGuid());
        assertNotNull(entryCreated.getCreated());
        assertNotNull(entryCreated.getThreadGuid());
        return entryCreated;
    }

    protected NotesData createThread(NotesData existingEntry, String content) {
        NotesData newThread = new NotesData();
        newThread.setContent(content);
        newThread.setEntryGuidParent(existingEntry.getEntryGuid());
        NotesData newThreadCreated = notesService.create(newThread);
        assertEquals(newThread.getExternalGuid(), newThreadCreated.getExternalGuid());
        assertNull(newThreadCreated.getHistory());
        assertNull(newThreadCreated.getThreads());
        assertNull(newThreadCreated.getArchived());
        assertEquals(existingEntry.getEntryGuid(), newThreadCreated.getEntryGuidParent());
        assertNotEquals(existingEntry.getEntryGuid(), newThreadCreated.getEntryGuid());
        assertNotEquals(existingEntry.getCreated(), newThreadCreated.getCreated());
        assertNotEquals(existingEntry.getContent(), newThreadCreated.getContent());
        assertEquals(existingEntry.getThreadGuid(), newThreadCreated.getThreadGuid());
        return newThreadCreated;
    }

    protected NotesData updateGuid(NotesData existingEntry, String content) {
        NotesData newEntry = new NotesData();
        newEntry.setGuid(existingEntry.getGuid());
        newEntry.setContent(content);
        newEntry.setEntryGuid(existingEntry.getEntryGuid());
        newEntry.setCreated(existingEntry.getCreated());
        NotesData newThreadUpdated = notesService.updateByGuid(newEntry);
        assertEquals(existingEntry.getExternalGuid(), newThreadUpdated.getExternalGuid());
        assertEquals(existingEntry.getEntryGuid(), newThreadUpdated.getEntryGuid());
        assertEquals(existingEntry.getThreadGuid(), newThreadUpdated.getThreadGuid());
        assertEquals(existingEntry.getEntryGuidParent(), newThreadUpdated.getEntryGuidParent());
        assertNotEquals(existingEntry.getCreated(), newThreadUpdated.getCreated());
        assertNotEquals(existingEntry.getContent(), newThreadUpdated.getContent());
        return newThreadUpdated;
    }

    protected void validateAll(List<NotesData> result, int entrySize, int expectedTotalCount, int expectedThreadCount, int expectedHistoryCount) {
        List<NotesData> total = new ArrayList<>();
        List<NotesData> totalThreads = new ArrayList<>();
        List<NotesData> totalHistories = new ArrayList<>();
        assertEquals(entrySize, result.size());
        result.forEach(r -> flatten(r, total));
        assertEquals(expectedTotalCount, total.size());
        result.forEach(r -> flattenThreads(r, totalThreads));
        assertEquals(expectedThreadCount, totalThreads.size());
        result.forEach(r -> flattenHistories(r, totalHistories));
        assertEquals(expectedHistoryCount, totalHistories.size());

        // this is to check each individual external entries will have different entryGuid
        IntStream.range(0, result.size())
                .forEach(i -> IntStream.range(i + 1, result.size())
                        .forEach(j -> assertNotEquals(result.get(i).getEntryGuid(), result.get(j).getEntryGuid())));

        // this is to check each external entry and its history will have the same entryGuid
        for (NotesData notesData : result) {
            if (notesData.getHistory() != null) {
                IntStream.range(0, notesData.getHistory().size())
                        .forEach(i -> assertEquals(notesData.getEntryGuid(), notesData.getHistory().get(i).getEntryGuid()));
            }
        }

        // this is to check each threads will have same thread guid as their parent 
        for (NotesData notesData : result) {
            if (notesData.getThreads() != null) {
                IntStream.range(0, notesData.getThreads().size())
                        .forEach(i -> assertEquals(notesData.getEntryGuid(), notesData.getThreads().get(i).getEntryGuidParent()));
            }
        }

        checkDuplicates(result);
    }

    protected void checkDuplicates(List<NotesData> result){
        List<NotesData> flattenEntries = new ArrayList<>();
        Set<String> entryCount = new HashSet<>();
        int j = 0;
        for (NotesData notesData : result) {
            flatten(notesData, flattenEntries);
            while (j < flattenEntries.size()) {
                String guidKey = flattenEntries.get(j).getGuid().toString();
                if (entryCount.contains(guidKey)) { // this check is for debug just in case
                    assertFalse(entryCount.contains(guidKey));
                }
                entryCount.add(guidKey);
                j++;
            }
        }
    }

    protected void flatten(NotesData root, List<NotesData> entries) {
        entries.add(root);
        if (root.getThreads() != null)
            root.getThreads().forEach(e -> flatten(e, entries));
        if (root.getHistory() != null)
            root.getHistory().forEach(e -> flatten(e, entries));
    }

    protected void flattenThreads(NotesData root, List<NotesData> entries) {
        if (root.getThreads() != null)
            root.getThreads().forEach(e -> {
                entries.add(e);
                flattenThreads(e, entries);
            });
    }

    protected void flattenHistories(NotesData root, List<NotesData> entries) {
        if (root.getThreads() != null)
            root.getThreads().forEach(e -> {
                flattenHistories(e, entries);
            });
        if (root.getHistory() != null)
            root.getHistory().forEach(e -> {
                entries.add(e);
                flattenHistories(e, entries);
            });
    }

    protected Map<String,NotesData> getEntries() {
        Map<String, NotesData> entries = new HashMap<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
            for (int i = 0; i < jsonArray.length(); i++) {
                NotesData data = NotesData.fromJson(jsonArray.getString(i));
                entries.put(data.getGuid().toString(), data);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

}
