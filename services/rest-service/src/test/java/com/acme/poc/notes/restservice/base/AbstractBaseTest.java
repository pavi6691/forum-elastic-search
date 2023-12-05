package com.acme.poc.notes.restservice.base;

import com.acme.poc.notes.restservice.generics.models.INoteEntity;
import com.acme.poc.notes.restservice.generics.interfaces.INotesOperations;
import com.acme.poc.notes.restservice.base.data.ElasticSearchData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;
public abstract class AbstractBaseTest<E extends INoteEntity<E>> extends TestContainers {
    
    protected INotesOperations<E> notesService;
    protected AbstractBaseTest(INotesOperations<E> notesService) {
        this.notesService = notesService;
    }

    protected INoteEntity createNewEntry(E newExternalEntry) {
        INoteEntity entryCreated = notesService.create(newExternalEntry);
        assertEquals(newExternalEntry.getExternalGuid(), entryCreated.getExternalGuid());
        assertEquals(newExternalEntry.getContent(), entryCreated.getContent());
        assertNull(((E) entryCreated).getHistory());
        assertNull(((E) entryCreated).getThreads());
        assertNull(entryCreated.getEntryGuidParent());
        assertNull(entryCreated.getArchived());
        assertNotNull(entryCreated.getEntryGuid());
        assertNotNull(entryCreated.getCreated());
        assertNotNull(entryCreated.getThreadGuid());
        return entryCreated;
    }

    protected INoteEntity createThread(INoteEntity<E> existingEntry, String content) {
        E newThread = existingEntry.newInstance();
        newThread.setContent(content);
        newThread.setEntryGuidParent(existingEntry.getEntryGuid());
        INoteEntity newThreadCreated = notesService.create(newThread);
        assertEquals(newThread.getExternalGuid(), newThreadCreated.getExternalGuid());
        assertNull(((E) newThreadCreated).getHistory());
        assertNull(((E) newThreadCreated).getThreads());
        assertNull(newThreadCreated.getArchived());
        assertEquals(existingEntry.getEntryGuid(), newThreadCreated.getEntryGuidParent());
        assertNotEquals(existingEntry.getEntryGuid(), newThreadCreated.getEntryGuid());
        assertNotEquals(existingEntry.getCreated(), newThreadCreated.getCreated());
        assertNotEquals(existingEntry.getContent(), newThreadCreated.getContent());
        assertEquals(existingEntry.getThreadGuid(), newThreadCreated.getThreadGuid());
        return newThreadCreated;
    }

    protected INoteEntity updateGuid(INoteEntity<E> existingEntry, String content) {
        E newEntry = existingEntry.copyThis();
        newEntry.setGuid(existingEntry.getGuid());
        newEntry.setContent(content);
        newEntry.setEntryGuid(existingEntry.getEntryGuid());
        newEntry.setCreated(existingEntry.getCreated());
        INoteEntity newThreadUpdated = notesService.update(newEntry);
        assertEquals(existingEntry.getExternalGuid(), newThreadUpdated.getExternalGuid());
        assertEquals(existingEntry.getEntryGuid(), newThreadUpdated.getEntryGuid());
        assertEquals(existingEntry.getThreadGuid(), newThreadUpdated.getThreadGuid());
        assertEquals(existingEntry.getEntryGuidParent(), newThreadUpdated.getEntryGuidParent());
        assertNotEquals(existingEntry.getCreated(), newThreadUpdated.getCreated());
        assertNotEquals(existingEntry.getContent(), newThreadUpdated.getContent());
        return newThreadUpdated;
    }

    protected void validateAll(List<E> result, int entrySize, int expectedTotalCount, int expectedThreadCount, int expectedHistoryCount) {
        List<E> total = new ArrayList<>();
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
            if (((E) noteEntity).getHistory() != null) {
                IntStream.range(0, ((E) noteEntity).getHistory().size())
                        .forEach(i -> assertEquals(noteEntity.getEntryGuid(), ((E) noteEntity).getHistory().get(i).getEntryGuid()));
            }
        }

        // this is to check each threads will have same thread guid as their parent 
        for (INoteEntity noteEntity : result) {
            if (((E) noteEntity).getThreads() != null) {
                IntStream.range(0, ((E) noteEntity).getThreads().size())
                        .forEach(i -> assertEquals(noteEntity.getEntryGuid(), ((E) noteEntity).getThreads().get(i).getEntryGuidParent()));
            }
        }

        checkDuplicates(result);
    }

    protected void checkDuplicates(List<E> result){
        Set<String> entryCount = new HashSet<>();
        for (INoteEntity noteEntity : result) {
            List<E> flattenEntries = new ArrayList<>();
            flatten(noteEntity, flattenEntries);
            checkDuplicates(flattenEntries, entryCount);
        }
    }
    
    protected void checkDuplicates(List<E> result, Set<String> entryCount) {
        for (INoteEntity e : result) {
            String guidKey = e.getGuid().toString();
            if (entryCount.contains(guidKey)) { // this check is for debug just in case
                assertFalse(entryCount.contains(guidKey));
            }
            entryCount.add(guidKey);
        }
    }

    protected void flatten(INoteEntity root, List<E> entries) {
        entries.add((E) root);
        if (((E) root).getThreads() != null)
            ((E) root).getThreads().forEach(e -> flatten(e, entries));
        if (((E) root).getHistory() != null)
            ((E) root).getHistory().forEach(e -> flatten(e, entries));
    }

    protected void flattenThreads(INoteEntity root, List<INoteEntity> entries) {
        if (((E) root).getThreads() != null)
            ((E) root).getThreads().forEach(e -> {
                entries.add(e);
                flattenThreads(e, entries);
            });
    }

    protected void flattenHistories(INoteEntity root, List<INoteEntity> entries) {
        if (((E) root).getThreads() != null)
            ((E) root).getThreads().forEach(e -> {
                flattenHistories(e, entries);
            });
        if (((E) root).getHistory() != null)
            ((E) root).getHistory().forEach(e -> {
                entries.add(e);
                flattenHistories(e, entries);
            });
    }

    protected Map<String,E> getEntries() {
        Map<String, E> entries = new HashMap<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
            for (int i = 0; i < jsonArray.length(); i++) {
                ObjectMapper mapper = new ObjectMapper();
                E data = mapper.readValue(jsonArray.getString(i), 
                        mapper.getTypeFactory().constructType(notesService.getClass()));
                entries.put(data.getGuid().toString(), data);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

}
