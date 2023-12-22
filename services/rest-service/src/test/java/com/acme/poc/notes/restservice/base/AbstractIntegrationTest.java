package com.acme.poc.notes.restservice.base;

import com.acme.poc.notes.restservice.generics.models.INoteEntity;
import com.acme.poc.notes.restservice.base.data.NotesDataForTesting;
import com.acme.poc.notes.restservice.generics.interfaces.INotesOperations;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
public abstract class AbstractIntegrationTest<E extends INoteEntity<E>> extends AbstractBaseTest<E> {
    E entity;
    INoteEntity newEntryCreated;
    IQueryRequest querySearchByExternalGuid;
    INoteEntity thread1;
    INoteEntity thread2;
    INoteEntity thread1_2;
    INoteEntity thread1_1;
    INoteEntity thread3;
    INoteEntity thread4;
    IQueryRequest queryArchivedRootEntry;
    IQueryRequest querySearchArchivedByExternal;
    IQueryRequest querySearchArchivedByEntry;
    public AbstractIntegrationTest(INotesOperations<E> esNotesService, E entity) {
        super(esNotesService);
        this.entity = entity;
    }

    @Test
    @Order(1)
    void createNewEntry() {
        E newEntry  = entity.newInstance();
        newEntry.setExternalGuid(UUID.randomUUID());
        newEntry.setContent("New External Entry - 1");
        newEntryCreated = createNewEntry(newEntry);
        querySearchByExternalGuid = QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(newEntryCreated.getExternalGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 1, 0, 0);
    }

    @Test
    @Order(2)
    void createNewThread_1() {
        thread1 = createThread(newEntryCreated, "New External Entry-Thread-1");
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1,2, 1, 0);
    }

    @Test
    @Order(3)
    void createNewThread_2() {
        thread2 = createThread(newEntryCreated, "New External Entry-Thread-2");
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 3, 2, 0);
    }

    @Test
    @Order(4)
    void createNewThread_3() {
        thread3 = createThread(newEntryCreated, "New External Entry-Thread-3");
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 4, 3, 0);
    }

    @Test
    @Order(5)
    void createNewThread_4_5_6_And_Archive_thread_5() {
        thread4 = createThread(newEntryCreated, "New External Entry-Thread-4");
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 5, 4, 0);
        INoteEntity thread5 = createThread(thread4, "New External Entry-Thread-5");
        INoteEntity thread6 = createThread(thread5, "New External Entry-Thread-6");
        searchResult = notesService.archive(QueryRequest.builder()
                .allEntries(true)
                .searchField(Field.ENTRY)
                .searchData(thread5.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 1, 2, 1, 0);
    }

    @Test
    @Order(6)
    void createThread_Under_Thread_1() {
        thread1_1 = createThread(thread1, "New External Entry-Thread-1-1");
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 8, 7, 0);
    }

    @Test
    @Order(7)
    void update_thread1() {
        updateGuid(thread1,"New External Entry-Thread-1-Updated");
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 9, 7, 1);
    }

    @Test
    @Order(8)
    void createAnotherThread_Under_Thread_1() {
        thread1_2 = createThread(thread1, "New External Entry-Thread-1-2");
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 10, 8, 1);
    }

    @Test
    @Order(9)
    void updateByEntryGuid() {
        thread1.setGuid(null); // if guid is null, entry will be updated by entryGuid
        updateGuid(thread1_2, "New External Entry-Thread-1-2-Updated");
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 11, 8, 2);
    }

    @Test
    @Order(10)
    void getByEntryGuid() {
        IQueryRequest querySearchByEntryGuid = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(thread1.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(querySearchByEntryGuid);
        validateAll(searchResult, 1, 5, 2, 2);
    }

    @Test
    @Order(11)
    void getByEntryGuid_excludeVersions() {
        IQueryRequest searchBy_Thread_1_Entry = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(thread1.getEntryGuid().toString())
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(searchBy_Thread_1_Entry);
        validateAll(searchResult, 1, 3, 2, 0);
    }

    @Test
    @Order(12)
    void archive_Thread1_2() {
        IQueryRequest searchBy_Thread_1_Entry = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(thread1_2.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build();
        List<E> searchResult = notesService.archive(searchBy_Thread_1_Entry);
        validateAll(searchResult, 1, 2, 0, 1);
    }

    @Test
    @Order(13)
    void searchArchived_By_ExternalGuid() {
        IQueryRequest querySearchArchived_thread1_2 = QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(thread1_2.getExternalGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(querySearchArchived_thread1_2);
        validateAll(searchResult, 2, 4, 1, 1);
    }

    @Test
    @Order(14)
    void searchArchived_By_ExternalGuid_excludeVersions() {
        IQueryRequest querySearchArchived_thread1_2_no_histories = QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(thread1_2.getExternalGuid().toString())
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(querySearchArchived_thread1_2_no_histories);
        validateAll(searchResult, 2, 3, 1, 0);
    }

    @Test
    @Order(15)
    void searchArchived_By_EntryGuid_test1() {
        IQueryRequest thread1_query = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(thread1.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(thread1_query);
        validateAll(searchResult, 1, 2, 0, 1);
    }

    @Test
    @Order(16)
    void searchArchived_By_EntryGuid_test2() {
        IQueryRequest thread1_2_query = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(thread1_2.getEntryGuid().toString())
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(thread1_2_query);
        validateAll(searchResult, 1, 1, 0, 0);
    }

    @Test
    @Order(17)
    void createThread_1_1_1() {
        createThread(thread1_1,"New External Entry-Thread-1-1-1");
        List<E> searchResult = notesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(newEntryCreated.getExternalGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 1, 12, 9, 2);
    }


    // Archive another entry (total two different threads archived), expected multiple entries, when searched by parent entry which is not archived
    @Test
    @Order(18)
    void archiveEntry_ForMultipleDifferentThreads() {
        IQueryRequest archive_1_1 = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(thread1_1.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build();
        List<E> searchResult = notesService.archive(archive_1_1);
        validateAll(searchResult, 1, 2, 1, 0);
    }

    @Test
    @Order(19)
    void searchArchived_ByExternalGuid() {
        querySearchArchivedByExternal = QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(newEntryCreated.getExternalGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(querySearchArchivedByExternal);
        validateAll(searchResult, 3, 6, 2, 1);
    }

    @Test
    @Order(20)
    void searchArchived_ByEntryGuid() {
        querySearchArchivedByEntry = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(newEntryCreated.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(querySearchArchivedByEntry);
        validateAll(searchResult, 3, 6, 2, 1);
    }

    // Search multiple threads archived. both may have further threads. by entry guid
    @Test
    @Order(21)
    void searchArchived_byEntryGuid_whichIsNotArchived() {
        IQueryRequest querySearchArchived_thread1 = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(thread1.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(querySearchArchived_thread1);
        validateAll(searchResult, 2, 4, 1, 1);
    }

    @Test
    @Order(22)
    void delete_by_threadEntryGuid() {
        List<E> resultDelete = notesService.delete(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(thread1_1.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build(),OperationStatus.DELETE);
        validateAll(resultDelete, 1, 2, 1, 0);
    }

    @Test
    @Order(23)
    void get_by_externalGuid_after_delete() {
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 1, 10, 7, 2);
    }

    // Archive another entry (total two different threads archived), expected multiple results
    @Test
    @Order(24)
    void archive_for_multiple_threads_on_same_layer() {
        IQueryRequest query_thread1 = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(thread1.getEntryGuid().toString())
                .allEntries(true)
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(query_thread1);
        validateAll(searchResult, 1, 4, 1, 2);
        searchResult = notesService.archive(query_thread1);
        validateAll(searchResult, 1, 4, 1, 2);
    }

    // Select one archived entry of many threads on the same layer. and some other is also archived
    @Test
    @Order(25)
    void archived_for_multiple_threads_on_same_layer() {
        IQueryRequest archive_thread3 = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(thread3.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build();
        notesService.archive(archive_thread3);
        IQueryRequest search_archived_thread1 = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(thread1.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> searchResult = notesService.get(search_archived_thread1);
        validateAll(searchResult, 1, 4, 1, 2);
    }

    // Search multiple threads archived. both may have further threads. by externalGuid
    @Test
    @Order(26)
    void search_archived_byExternalGuid_for_multiple_threads() {
        List<E> searchResult = notesService.get(querySearchArchivedByExternal);
        validateAll(searchResult, 3, 7, 2, 2);
    }

    // Search multiple threads archived. both may have further threads. by entryGuid
    @Test
    @Order(27)
    void search_archived_byEntryGuid_for_multiple_threads() {
        List<E> searchResult = notesService.get(querySearchArchivedByEntry);
        validateAll(searchResult, 3, 7, 2, 2);
    }


    // Create another external entry with same externalGuid
    @Test
    @Order(28)
    void create_another_externalEntry_with_same_externalGuid() {
        E newEntry  = entity.newInstance();
        newEntry.setExternalGuid(newEntryCreated.getExternalGuid());
        newEntry.setContent("New External Entry - 2");
        createNewEntry(newEntry);
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 2, 11, 7, 2);
    }

    @Test
    @Order(29)
    void delete_archived_by_entryGuid() {
        queryArchivedRootEntry = QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(newEntryCreated.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build();
        List<E> results = notesService.delete(queryArchivedRootEntry,OperationStatus.DELETE);
        validateAll(results, 3, 7, 2, 2);

        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 2, 4, 2, 0);
    }

    @Test
    @Order(30)
    void create_thread_4_1() {
        INoteEntity thread_4_1 = createThread(thread4, "New External Entry-Thread-4-1");
        List<E> searchResult = notesService.archive(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(thread_4_1.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 1, 1, 0, 0);
    }

    @Test
    @Order(31)
    void archive_from_thread2() {
        List<E> searchResult = notesService.archive(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(thread2.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 1, 1, 0, 0);
        searchResult = notesService.get(queryArchivedRootEntry);
        validateAll(searchResult, 2, 2, 0, 0);
    }

    // This make sure it archives entire entry. where some thread entry have already been archived.
    // So when searched results should contain one entry with all threads
    @Test
    @Order(32)
    void archive_by_entryGuid_where_some_entries_within_are_already_archived() {
        List<E> searchResult = notesService.archive(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(newEntryCreated.getEntryGuid().toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 1, 4, 3, 0);
        searchResult = notesService.get(queryArchivedRootEntry);
        validateAll(searchResult, 1, 4, 3, 0);
    }

    @Test
    @Order(33)
    void delete_by_entryGuid_where_some_entries_within_are_already_archived() {
        List<E> results = notesService.delete(queryArchivedRootEntry,OperationStatus.DELETE);
        validateAll(results, 1, 4, 3, 0);
    }
    
    @Test
    @Order(34)
    void deleteImmediate() {
        List<E> resultDelete = notesService.delete(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .allEntries(true)
                .searchData(querySearchByExternalGuid.getSearchData())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build(),OperationStatus.DELETE);
        validateAll(resultDelete, 1, 1, 0, 0);
        List<E> searchResult = notesService.get(querySearchByExternalGuid);
        validateAll(searchResult, 0, 0, 0, 0);
    }

    @Test
    void searchContent() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.CONTENT)
                .searchData("content")
                .resultFormat(ResultFormat.FLATTEN)
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        checkDuplicates(result,new HashSet<>());
        assertEquals(11, result.size());
    }
    
    @BeforeAll
    protected void beforeAll() {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(NotesDataForTesting.ENTRIES);
            for (int i = 0; i < jsonArray.length(); i++) {
                List<E> entries = new ArrayList<>();
                String jsonStringToStore = jsonArray.getString(i);
                flatten(INoteEntity.fromJson(jsonStringToStore,entity.getClass()), entries);
                entries.forEach(e -> {
                    e.setThreads(null);
                    e.setHistory(null);
                    E pgNoteEntity = notesService.create(e);
                    assertEquals(pgNoteEntity.getGuid(), e.getGuid());
                    assertEquals(pgNoteEntity.getExternalGuid(), e.getExternalGuid());
                    assertEquals(pgNoteEntity.getEntryGuid(), e.getEntryGuid());
                    assertEquals(pgNoteEntity.getThreadGuid(), e.getThreadGuid());
                    assertEquals(pgNoteEntity.getEntryGuidParent(), e.getEntryGuidParent());
                    assertEquals(pgNoteEntity.getContent(), e.getContent());
                    assertEquals(pgNoteEntity.getCreated(), e.getCreated());
                    assertEquals(pgNoteEntity.getArchived(), e.getArchived());
                    assertEquals(pgNoteEntity.getThreads(), e.getThreads());
                    assertEquals(pgNoteEntity.getHistory(), e.getHistory());
                });
            }
            notesService.archive(QueryRequest.builder()
                    .allEntries(true)
                    .searchField(Field.ENTRY)
                    .filters(Set.of(Filter.INCLUDE_VERSIONS,Filter.EXCLUDE_ARCHIVED))
                    .searchData("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                    .build());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
    @AfterAll
    protected void afterAll() {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(NotesDataForTesting.ENTRIES);
            for (int i = 0; i < jsonArray.length(); i++) {
                List<E> entries = new ArrayList<>();
                String jsonStringToStore = jsonArray.getString(i);
                flatten(INoteEntity.fromJson(jsonStringToStore,entity.getClass()),entries);
                entries.forEach(e -> {
                    if (e.getThreads() != null)
                        e.getThreads().clear();
                    if (e.getHistory() != null)
                        e.getHistory().clear();
                    notesService.delete(e.getGuid(), OperationStatus.DELETE);
                });
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void getByExternalGuid_all() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(result, 3, 11, 6, 2);
    }

    @Test
    void getByExternalGuid_noHistories() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(result, 3, 9, 6, 0);
    }

    @Test
    void getByExternalGuid_noArchive() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(result, 3, 7, 3, 1);
    }

    @Test
    void getByExternalGuid_noHistoryAndArchives() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(result, 3, 6, 3, 0);
    }

    @Test
    void getByEntryGuid_all() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(result, 1, 4, 2, 1);
    }

    @Test
    void getByEntryGuid_all_test_1() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(result, 1, 8, 5, 2);
    }

    @Test
    void getByEntryGuid_all_test_2() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(result, 1, 6, 4, 1);
    }

    @Test
    void getByEntryGuid_noHistories() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(result, 1, 3, 2, 0);
    }

    @Test
    void getByEntryGuid_noHistories_test_1() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(result, 1, 6, 5, 0);
    }

    @Test
    void getByEntryGuid_noArchived_test_1() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        assertEquals(0, result.size());
    }

    @Test
    void getByEntryGuid_noArchived_test_2() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("16b8d331-92ab-424b-b69a-3181f6d80f5a")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(result, 1, 1, 0, 0);
    }

    @Test
    void getByEntryGuid_noArchived_test_3() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(result, 1, 4, 2, 1);
    }

    @Test
    void getByEntryGuid_NoHistoriesAndArchives_test_1() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(result, 0, 0, 0, 0);
    }

    @Test
    void getByEntryGuid_NoHistoriesAndArchives_test_2() {
        List<E> result = notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .filters(Set.of(Filter.EXCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build());
        validateAll(result, 1, 3, 2, 0);
    }

}
