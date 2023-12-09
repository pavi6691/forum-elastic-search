package com.acme.poc.notes.restservice.controller.postgresql;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.models.NoteEntry;
import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.models.validation.CreateValidationGroup;
import com.acme.poc.notes.models.validation.UpdateValidationGroup;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.service.pgsqlservice.PGSQLNotesService;
import com.acme.poc.notes.restservice.util.DTOMapper;
import com.acme.poc.notes.restservice.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_NOTES_POSTGRESQL_USER)
public class PGUserController {

    PGSQLNotesService pgsqlNotesService;

    @Autowired
    public PGUserController(PGSQLNotesService pgsqlNotesService) {
        this.pgsqlNotesService = pgsqlNotesService;
    }


    @Operation(summary = "Create a new entry", description = "Create a new entry (note, remark etc) either as the root entry or as a response to another entry", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "DESCRIPTION", content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE) }),
            @ApiResponse(responseCode = "400", description = "DESCRIPTION", content = @Content)
    })
    @PostMapping(NotesConstants.API_ENDPOINT_NOTES_CREATE)
    public ResponseEntity<PGNoteEntity> create(@Validated(CreateValidationGroup.class) @RequestBody NoteEntry payloadEntry) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.create(DTOMapper.INSTANCE.toEntity(payloadEntry)));
    }

    @Operation(summary = "Update existing entry by guid/entryGuid in the payload", description = "Update existing entry by guid/entryGuid. Current data will be archived as a previous version", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_UPDATE)
    public ResponseEntity<PGNoteEntity> update(@Validated(UpdateValidationGroup.class) @RequestBody NoteEntry payloadEntry) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.update(DTOMapper.INSTANCE.toEntity(payloadEntry)));
    }

    @Operation(summary = "Retrieve entry by guid", description = "Retrieve entry by guid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_GUID)
    public ResponseEntity<PGNoteEntity> getByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.get(guid));
    }

    @Operation(summary = "Retrieve entries by entryGuid", description = "Retrieve entries by entryGuid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_ENTRY_GUID)
    public ResponseEntity<List<PGNoteEntity>> getByEntryGuid(
                                                        @PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED, required = false, defaultValue = "false") boolean includeArchived,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Retrieve entries by threadGuid", description = "Retrieve entries by threadGuid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_THREAD_GUID)
    public ResponseEntity<List<PGNoteEntity>> getByThreadGuid(
            @PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_THREAD_GUID) UUID threadGuid,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED, required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.THREAD)
                .searchData(threadGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Search entries", description = "Retrieve entries that matches the search.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_CONTENT)
    public ResponseEntity<List<PGNoteEntity>> searchContent(
                                                        @RequestParam String searchData,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED, required = false, defaultValue = "false") boolean includeArchived,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.CONTENT)
                .searchData(searchData)
                .resultFormat(ResultFormat.FLATTEN)
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Archive entry by guid", description = "Archive entry by guid. Will also archive all response to this entry.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_GUID)
    public ResponseEntity<List<PGNoteEntity>> archiveByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.archive(guid));
    }

    @Operation(summary = "Archive entries by externalGuid", description = "Archive entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<PGNoteEntity>> archiveByExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.archive(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .allEntries(true)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build()));
    }

    @Operation(summary = "Archive entries by entryGuid", description = "Archive entries by entryGuid. Will also archive all responses to this entryGuid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID)
    public ResponseEntity<List<PGNoteEntity>> archiveByEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.archive(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build()));
    }

    @Operation(summary = "Retrieve archived entries by externalGuid", description = "Retrieve archived entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<PGNoteEntity>> getArchivedByExternalGuid(
                                                        @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Retrieve archived entries by entryGuid", description = "Retrieve archived entries by entryGuid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID)
    public ResponseEntity<List<PGNoteEntity>> getArchivedByEntryGuid(
                                                        @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
                                                        @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Delete archived entries by externalGuid", description = "Delete archived entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_EXTERNAL_GUID)
    public ResponseEntity<List<PGNoteEntity>> deleteArchivedByExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.markDelete((QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .allEntries(true)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build())));
    }

    @Operation(summary = "Delete archived entries by entryGuid", description = "Delete archived entries by entryGuid. Will also delete all responses to this entryGuid.", tags = { NotesConstants.OPENAPI_NOTES_POSTGRESQL_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_ENTRY_GUID)
    public ResponseEntity<List<PGNoteEntity>> deleteArchivedByEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(pgsqlNotesService.markDelete(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build()));
    }

}
