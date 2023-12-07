package com.acme.poc.notes.restservice.controller.postgresql;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.service.pgsqlservice.PGSQLNotesService;
import com.acme.poc.notes.restservice.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_NOTES_POSTGRESQL_ADMIN)
public class PGAdminController {

    PGSQLNotesService pgsqlNotesService;

    @Autowired
    public PGAdminController(PGSQLNotesService esNotesService) {
        this.pgsqlNotesService = esNotesService;
    }


    @Operation(summary = "Get all notes", description = "Retrieve all notes", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_ADMIN_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_ALL)
    public ResponseEntity<List<PGNoteEntity>> getAll(@RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INDEX_NAME) String indexName) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.get(indexName));
    }

    @Operation(summary = "Get all notes by externalGuid", description = "Retrieve all notes by externalGuid", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_ADMIN_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_EXTERNAL_GUID)
    public ResponseEntity<List<PGNoteEntity>> getByExternalGuid(
                                                    @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid,
                                                    @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
                                                    @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED, required = false, defaultValue = "false") boolean includeArchived,
                                                    @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
                                                    @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
                                                    @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .size(size)
                .searchAfter(searchAfter)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Delete all notes by externalGuid", description = "Delete all notes by externalGuid", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_ADMIN_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<PGNoteEntity>> deleteByExternalGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.delete(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .allEntries(true)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete all notes by entryGuid", description = "Delete all notes by entryGuid", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_ADMIN_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_ENTRY_GUID)
    public ResponseEntity<List<PGNoteEntity>> deleteByEntryGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.delete(QueryRequest.builder()
                .allEntries(true)
                .searchField(Field.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete all notes by threadGuid", description = "Delete all notes by threadGuid", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_ADMIN_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_THREAD_GUID)
    public ResponseEntity<List<PGNoteEntity>> deleteByThreadGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_THREAD_GUID) UUID threadGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.delete(QueryRequest.builder()
                .allEntries(true)
                .searchField(Field.THREAD)
                .searchData(threadGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete note by guid", description = "Delete note by guid", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_ADMIN_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_GUID)
    public ResponseEntity<PGNoteEntity> deleteByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.delete(guid));
    }

}
