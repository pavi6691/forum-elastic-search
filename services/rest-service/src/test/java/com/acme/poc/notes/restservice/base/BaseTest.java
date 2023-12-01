package com.acme.poc.notes.restservice.base;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.data.ElasticSearchData;
import com.acme.poc.notes.restservice.service.esservice.ESNotesAdminService;
import com.acme.poc.notes.restservice.service.esservice.ESNotesClientService;
import com.acme.poc.notes.restservice.service.pgsqlservice.PSQLNotesClientService;
import com.acme.poc.notes.restservice.generics.abstracts.disctinct.AbstractNotesClientOperations;
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
    protected ESNotesClientService esNotesService;

    @Autowired
    protected PSQLNotesClientService psqlNotesClientService;
    @Autowired
    protected PSQLNotesClientService psqlNotesService;
    @Autowired
    protected ESNotesAdminService notesAdminService;
    @Autowired
    protected ESNotesRepository repository;


    protected INoteEntity createNewEntry(INoteEntity newExternalEntry, AbstractNotesClientOperations iNotesService) {
        INoteEntity entryCreated = iNotesService.create(newExternalEntry);
        assertEquals(newExternalEntry.getExternalGuid(), entryCreated.getExternalGuid());
        assertEquals(newExternalEntry.getContent(), entryCreated.getContent());
        assertNull(((NotesData) entryCreated).getHistory());
        assertNull(((NotesData) entryCreated).getThreads());
        assertNull(entryCreated.getEntryGuidParent());
        assertNull(entryCreated.getArchived());
        assertNotNull(entryCreated.getEntryGuid());
        assertNotNull(entryCreated.getCreated());
        assertNotNull(entryCreated.getThreadGuid());
        return entryCreated;
    }

    protected INoteEntity createThread(INoteEntity existingEntry, String content) {
        NotesData newThread = new NotesData();
        newThread.setContent(content);
        newThread.setEntryGuidParent(existingEntry.getEntryGuid());
        INoteEntity newThreadCreated = esNotesService.create(newThread);
        assertEquals(newThread.getExternalGuid(), newThreadCreated.getExternalGuid());
        assertNull(((NotesData) newThreadCreated).getHistory());
        assertNull(((NotesData) newThreadCreated).getThreads());
        assertNull(newThreadCreated.getArchived());
        assertEquals(existingEntry.getEntryGuid(), newThreadCreated.getEntryGuidParent());
        assertNotEquals(existingEntry.getEntryGuid(), newThreadCreated.getEntryGuid());
        assertNotEquals(existingEntry.getCreated(), newThreadCreated.getCreated());
        assertNotEquals(existingEntry.getContent(), newThreadCreated.getContent());
        assertEquals(existingEntry.getThreadGuid(), newThreadCreated.getThreadGuid());
        return newThreadCreated;
    }

    protected INoteEntity updateGuid(INoteEntity existingEntry, String content) {
        NotesData newEntry = new NotesData();
        newEntry.setGuid(existingEntry.getGuid());
        newEntry.setContent(content);
        newEntry.setEntryGuid(existingEntry.getEntryGuid());
        newEntry.setCreated(existingEntry.getCreated());
        INoteEntity newThreadUpdated = esNotesService.update(newEntry);
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
        List<INoteEntity> totalThreads = new ArrayList<>();
        List<INoteEntity> totalHistories = new ArrayList<>();
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
        for (INoteEntity noteEntity : result) {
            if (((NotesData) noteEntity).getHistory() != null) {
                IntStream.range(0, ((NotesData) noteEntity).getHistory().size())
                        .forEach(i -> assertEquals(noteEntity.getEntryGuid(), ((NotesData) noteEntity).getHistory().get(i).getEntryGuid()));
            }
        }

        // this is to check each threads will have same thread guid as their parent 
        for (INoteEntity noteEntity : result) {
            if (((NotesData) noteEntity).getThreads() != null) {
                IntStream.range(0, ((NotesData) noteEntity).getThreads().size())
                        .forEach(i -> assertEquals(noteEntity.getEntryGuid(), ((NotesData) noteEntity).getThreads().get(i).getEntryGuidParent()));
            }
        }

        checkDuplicates(result);
    }

    protected void checkDuplicates(List<NotesData> result){
        Set<String> entryCount = new HashSet<>();
        for (INoteEntity noteEntity : result) {
            List<NotesData> flattenEntries = new ArrayList<>();
            flatten(noteEntity, flattenEntries);
            checkDuplicates(flattenEntries, entryCount);
        }
    }
    
    protected void checkDuplicates(List<NotesData> result, Set<String> entryCount) {
        for (INoteEntity e : result) {
            String guidKey = e.getGuid().toString();
            if (entryCount.contains(guidKey)) { // this check is for debug just in case
                assertFalse(entryCount.contains(guidKey));
            }
            entryCount.add(guidKey);
        }
    }

    protected void flatten(INoteEntity root, List<NotesData> entries) {
        entries.add((NotesData) root);
        if (((NotesData) root).getThreads() != null)
            ((NotesData) root).getThreads().forEach(e -> flatten(e, entries));
        if (((NotesData) root).getHistory() != null)
            ((NotesData) root).getHistory().forEach(e -> flatten(e, entries));
    }

    protected void flattenThreads(INoteEntity root, List<INoteEntity> entries) {
        if (((NotesData) root).getThreads() != null)
            ((NotesData) root).getThreads().forEach(e -> {
                entries.add(e);
                flattenThreads(e, entries);
            });
    }

    protected void flattenHistories(INoteEntity root, List<INoteEntity> entries) {
        if (((NotesData) root).getThreads() != null)
            ((NotesData) root).getThreads().forEach(e -> {
                flattenHistories(e, entries);
            });
        if (((NotesData) root).getHistory() != null)
            ((NotesData) root).getHistory().forEach(e -> {
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
                NotesData data = INoteEntity.fromJson(jsonArray.getString(i),NotesData.class);
                entries.put(data.getGuid().toString(), data);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

}
