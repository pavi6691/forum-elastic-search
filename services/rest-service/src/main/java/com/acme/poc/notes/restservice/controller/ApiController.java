package com.acme.poc.notes.restservice.controller;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.service.pgsqlservice.PGSQLNotesService;
import com.acme.poc.notes.restservice.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_POSTGRESQL)
public class ApiController {

    PGSQLNotesService PGSQLNotesService;

    public ApiController(PGSQLNotesService PGSQLNotesService) {
        this.PGSQLNotesService = PGSQLNotesService;
    }

    @Operation(summary = "Create a new entry", description = "Create a new entry (note, remark etc) either as the root entry or as a response to another entry", tags = { NotesConstants.POSTGRESQL_NOTES_TAG })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "DESCRIPTION", content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "DESCRIPTION", content = @Content)
    })
    @PostMapping(NotesConstants.API_ENDPOINT_NOTES_CREATE)
    public ResponseEntity<PGNoteEntity> create(@Valid @RequestBody PGNoteEntity pgNoteEntity) {
        log.debug("{}", LogUtil.method());
        pgNoteEntity.setIsDirty(true);
        return ResponseEntity.ok(PGSQLNotesService.create(pgNoteEntity));
    }

    @Operation(summary = "Update an existing entry by guid/entryGuid", description = "Update an existing entry by guid/entryGuid. Current data will be archived as a previous version", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_UPDATE)
    public ResponseEntity<PGNoteEntity> updateByGuid(@RequestBody PGNoteEntity pgNoteEntity) {
        log.debug("{}", LogUtil.method());
        pgNoteEntity.setIsDirty(true);
        return ResponseEntity.ok(PGSQLNotesService.update(pgNoteEntity));
    }

    @Operation(summary = "Retrieve entry by guid", description = "Retrieve entry by guid.", tags = { NotesConstants.POSTGRESQL_NOTES_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_GUID)
    public ResponseEntity<PGNoteEntity> getByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(PGSQLNotesService.get(UUID.fromString(guid.toString())));
    }

    @Operation(summary = "Retrieve entry by entryGuid", description = "Retrieve entry by entryGuid.", tags = { NotesConstants.POSTGRESQL_NOTES_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_ENTRY_GUID)
    public ResponseEntity<List<PGNoteEntity>> searchByEntryGuid(
            @PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid,
            @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(required = false) String searchAfter,
            @RequestParam(required = false, defaultValue = "0") int size,
            @RequestParam(required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(PGSQLNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Get all notes by externalGuid", description = "Retrieve all notes by externalGuid", tags = { NotesConstants.POSTGRESQL_NOTES_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_ADMIN_GET_ALL_BY_EXTERNAL_GUID)
    public ResponseEntity<List<PGNoteEntity>> getByExternalGuid(
            @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid,
            @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(required = false) String searchAfter,
            @RequestParam(required = false, defaultValue = "0") int size,
            @RequestParam(required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(PGSQLNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .size(size)
                .searchAfter(searchAfter)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Delete all notes by externalGuid", description = "Delete all notes by externalGuid", tags = { NotesConstants.POSTGRESQL_NOTES_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<PGNoteEntity>> deleteByExternalGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(PGSQLNotesService.delete(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete all notes by entryGuid", description = "Delete all notes by entryGuid", tags = { NotesConstants.POSTGRESQL_NOTES_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_ENTRY_GUID)
    public ResponseEntity<List<PGNoteEntity>> deleteByEntryGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(PGSQLNotesService.delete(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete all notes by threadGuid", description = "Delete all notes by threadGuid", tags = { NotesConstants.POSTGRESQL_NOTES_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_THREAD_GUID)
    public ResponseEntity<List<PGNoteEntity>> deleteByThreadGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_THREAD_GUID) UUID threadGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(PGSQLNotesService.delete(QueryRequest.builder()
                .searchField(Field.THREAD)
                .searchData(threadGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete note by guid", description = "Delete note by guid", tags = { NotesConstants.POSTGRESQL_NOTES_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_GUID)
    public ResponseEntity<PGNoteEntity> deleteByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(PGSQLNotesService.delete(guid));
    }
    
}