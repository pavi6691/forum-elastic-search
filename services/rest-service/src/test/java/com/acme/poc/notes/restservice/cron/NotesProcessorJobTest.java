package com.acme.poc.notes.restservice.cron;
import com.acme.poc.notes.core.enums.NotesAPIError;
import com.acme.poc.notes.models.NoteType;
import com.acme.poc.notes.restservice.base.AbstractBaseTest;
import com.acme.poc.notes.restservice.base.data.NotesDataForTesting;
import com.acme.poc.notes.restservice.generics.models.INoteEntity;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.service.esservice.ESNotesService;
import com.acme.poc.notes.restservice.service.pgsqlservice.PGSQLNotesService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotesProcessorJobTest extends AbstractBaseTest {

    ESNotesService esNotesService;
    PGSQLNotesService pgsqlNotesService;
    NotesProcessorJob notesProcessorJob;
    PGNotesRepository pgNotesRepository;
    UUID keyGuid;
    @Autowired
    public NotesProcessorJobTest(PGSQLNotesService notesService, ESNotesService esNotesService,
                                 NotesProcessorJob notesProcessorJob, PGNotesRepository pgNotesRepository) {
        super(notesService);
        this.esNotesService = esNotesService;
        this.pgsqlNotesService = notesService;
        this.notesProcessorJob = notesProcessorJob;
        this.pgNotesRepository = pgNotesRepository;
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("notes.processor.job.enabled", () -> "false");
    }

    @BeforeAll
    protected void beforeAll() {
        try {
            if(testContainers) {
                pgsqlNotesService.getCrudRepository().deleteAll();
                esNotesService.getCrudRepository().deleteAll();
            }
        } catch (Exception e) {}
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(NotesDataForTesting.ENTRIES);
            for (int i = 0; i < jsonArray.length(); i++) {
                List<PGNoteEntity> entries = new ArrayList<>();
                String jsonStringToStore = jsonArray.getString(i);
                flatten(INoteEntity.fromJson(jsonStringToStore,PGNoteEntity.class), entries);
                entries.forEach(e -> {
                    e.setThreads(null);
                    e.setHistory(null);
                    PGNoteEntity pgNoteEntity = pgsqlNotesService.create(e);
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
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    @Order(1)
    void check_entries_on_postgresql() {
        List<PGNoteEntity> pgResults = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(pgResults, 3, 11, 6, 2);
    }

    @Test
    @Order(2)
    void check_entries_on_elasticsearch() {
        List<ESNoteEntity> esResults = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esResults, 0, 0, 0, 0);
        
        notesProcessorJob.execute();
        
        List<ESNoteEntity> result = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(result, 3, 11, 6, 2);
    }

    @Test
    @Order(3)
    void create_entry() {
        createNewEntry(newEntry("10a14259-ca84-4c7d-8d46-7ad398000002","New Entry -1"));
        List<PGNoteEntity> pgResults = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(pgResults, 4, 12, 6, 2);
    }

    @Test
    @Order(4)
    void create_entry_execution() {
        List<ESNoteEntity> esResults = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esResults, 3, 11, 6, 2);
        
        notesProcessorJob.execute();
        
        esResults = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esResults, 4, 12, 6, 2);
    }

    @Test
    @Order(5)
    void create_thread() {
        List<PGNoteEntity> entries = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("dcd0df45-8fe7-4b1c-ad30-f64c5a2b6e74")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(entries, 1, 1, 0, 0);
        
        createThread(entries.get(0),"Content-thread-6");
        
        List<PGNoteEntity> pgResults = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(pgResults, 4, 13, 7, 2);
    }

    @Test
    @Order(6)
    void create_thread_execution() {
        List<ESNoteEntity> esResults = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esResults, 4, 12, 6, 2);
        
        notesProcessorJob.execute();
        
        esResults = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esResults, 4, 13, 7, 2);
    }

    @Test
    @Order(7)
    void update_entries() {
        List<PGNoteEntity> entries = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        
        updateGuid(entries.get(0),"Content-thread-1-Updated");
        
        List<PGNoteEntity> pgResults = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(pgResults, 4, 14, 7, 3);
    }

    @Test
    @Order(8)
    void update_entries_execution() {
        
        List<ESNoteEntity> esResults = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esResults, 4, 13, 7, 2);
        
        notesProcessorJob.execute();
        
        esResults = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esResults, 4, 14, 7, 3);
    }

    @Test
    @Order(9)
    void soft_delete_by_entryGuid() {
        List<PGNoteEntity> entries = pgsqlNotesService.delete(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build(),OperationStatus.MARK_FOR_SOFT_DELETE);
        validateAll(entries, 1, 8, 5, 2);
        
        entries = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        
        validateAll(entries, 0, 0, 0, 0);
        List<ESNoteEntity> esEntries = esNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esEntries, 1, 8, 5, 2);
    }

    @Test
    @Order(10)
    void soft_delete_by_entryGuid_execution() {
        List<ESNoteEntity> esNoteEntities = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esNoteEntities, 4, 14, 7, 3);
        
        notesProcessorJob.execute();

        esNoteEntities = esNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esNoteEntities, 0, 0, 0, 0);
        
        esNoteEntities = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esNoteEntities, 4, 6, 1, 1);
    }

    @Test
    @Order(11)
    void restore_by_entryGuid() {
        List<PGNoteEntity> entries = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(entries, 4, 6, 1, 1);
        
        entries = pgsqlNotesService.restore(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(entries, 1, 8, 5, 2);
        
        entries = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(entries, 4, 14, 7, 3);
    }

    @Test
    @Order(12)
    void restore_by_entryGuid_execution() {
        List<ESNoteEntity> esEntries = esNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esEntries, 0, 0, 0, 0);
        
        notesProcessorJob.execute();
        
        esEntries = esNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esEntries, 1, 8, 5, 2);
        esEntries = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esEntries, 4, 14, 7, 3);
    }

    @Test
    @Order(13)
    void soft_delete_by_externalGuid() {
        List<PGNoteEntity> searchResult = pgsqlNotesService.delete(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build(), OperationStatus.MARK_FOR_SOFT_DELETE);
        validateAll(searchResult, 4, 14, 7, 3);
        
        searchResult = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 0, 0, 0, 0);
    }

    @Test
    @Order(14)
    void soft_delete_by_externalGuid_execution() {
        List<ESNoteEntity> esNoteEntities = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(esNoteEntities, 4, 14, 7, 3);
        
        notesProcessorJob.execute();
        
        List<ESNoteEntity> searchResult = esNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 0, 0, 0, 0);
    }

    @Test
    @Order(15)
    void soft_delete_by_keyGuid() {
        PGNoteEntity pgNoteEntity = (PGNoteEntity) createNewEntry(newEntry("10a14259-ca84-4c7d-8d46-7ad398000002","Hello"));
        notesProcessorJob.execute(); // so that it gets created in elasticsearch as well
        keyGuid = pgNoteEntity.getGuid();

        List<PGNoteEntity> searchResult = List.of(pgsqlNotesService.get(keyGuid));
        validateAll(searchResult, 1, 1, 0, 0);
        
        // Regression error validation, what if trying restore entry which is not soft-deleted, expected an exception
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> pgsqlNotesService.restore(keyGuid));
        assertEquals(exception.getReason(),NotesAPIError.ERROR_SOFT_DELETED_ENTRIES_NOT_FOUND.errorMessage());
        
        pgsqlNotesService.delete(keyGuid,OperationStatus.MARK_FOR_SOFT_DELETE);

        exception = assertThrows(ResponseStatusException.class, () -> pgsqlNotesService.get(keyGuid));
        assertEquals(exception.getReason(),NotesAPIError.ERROR_SOFT_DELETED.errorMessage());
    }

    @Test
    @Order(16)
    void soft_delete_by_keyGuid_execution() {
        
        List<ESNoteEntity> searchResult = List.of(esNotesService.get(keyGuid));
        validateAll(searchResult, 1, 1, 0, 0);
        
        notesProcessorJob.execute();

        assertEquals(null,esNotesService.get(keyGuid));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> pgsqlNotesService.get(keyGuid));
        assertEquals(exception.getReason(),NotesAPIError.ERROR_SOFT_DELETED.errorMessage());
    }

    @Test
    @Order(17)
    void restore_by_keyGuid() {
        
        List<PGNoteEntity> searchResult = List.of(pgsqlNotesService.restore(keyGuid));
        validateAll(searchResult, 1, 1, 0, 0);

        searchResult = List.of(pgsqlNotesService.get(keyGuid));
        validateAll(searchResult, 1, 1, 0, 0);
    }

    @Test
    @Order(18)
    void restore_by_keyGuid_execution() {
        assertEquals(null,esNotesService.get(keyGuid));

        notesProcessorJob.execute();

        List<ESNoteEntity> searchResult = List.of(esNotesService.get(keyGuid));
        validateAll(searchResult, 1, 1, 0, 0);
    }

    @Test
    @Order(19)
    void delete_forever_by_keyGuid() {
        // Mark for soft delete
        pgsqlNotesService.delete(keyGuid,OperationStatus.MARK_FOR_SOFT_DELETE);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> pgsqlNotesService.get(keyGuid));
        assertEquals(exception.getReason(),NotesAPIError.ERROR_SOFT_DELETED.errorMessage());
        
        // execute, so it gets deleted on elastic search
        notesProcessorJob.execute();
        assertEquals(null,esNotesService.get(keyGuid));
        
        // Mark for delete forever
        pgsqlNotesService.delete(keyGuid,OperationStatus.MARK_FOR_DELETE);
        List<PGNoteEntity> searchResult = List.of(pgsqlNotesService.get(keyGuid));
        validateAll(searchResult, 1, 1, 0, 0);
        
        // After execution, it gets permanently deleted
        notesProcessorJob.execute();
        assertEquals(null,pgsqlNotesService.get(keyGuid));
        
    }

    @Test
    @Order(20)
    void mark_soft_deleted_to_delete_by_externalGuid() { // this will delete entries from postgresql forever
        List<PGNoteEntity> searchResult = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED,Filter.ONLY_SOFT_DELETED))
                .build());
        validateAll(searchResult, 4, 14, 7, 3);
        
        searchResult = pgsqlNotesService.delete(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED,Filter.ONLY_SOFT_DELETED))
                .build(),OperationStatus.MARK_FOR_DELETE);
        validateAll(searchResult, 4, 14, 7, 3);
        
        searchResult = pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData("10a14259-ca84-4c7d-8d46-7ad398000002")
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 0, 0, 0, 0);
    }

    @Test
    @Order(21)
    void delete_marked_forever_by_externalGuid_on_postgresql() {
        List<PGNoteEntity> results = pgNotesRepository.findByOperationStatus(OperationStatus.MARK_FOR_DELETE);
        
        notesProcessorJob.execute();
        
        results.stream().forEach(e -> {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
                pgsqlNotesService.delete(e.getGuid(), OperationStatus.DELETE);
            });
            assertEquals(exception.getReason(),NotesAPIError.ERROR_NOT_FOUND.errorMessage());
        });
    }

    private PGNoteEntity newEntry(String externalGuid,String content) {
        return PGNoteEntity.builder()
                .type(NoteType.NOTE)
                .content(content)
                .customJson("{}")
                .externalGuid(UUID.fromString(externalGuid))
                .externalDataSource("ds")
                .externalItemGuid(UUID.randomUUID())
                .externalItemId(String.valueOf(UUID.randomUUID())).build();
    }
}











