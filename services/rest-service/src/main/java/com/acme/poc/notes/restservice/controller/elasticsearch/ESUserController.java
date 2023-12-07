package com.acme.poc.notes.restservice.controller.elasticsearch;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.models.NoteEntry;
import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.service.esservice.ESNotesService;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.util.DTOMapper;
import com.acme.poc.notes.restservice.util.LogUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_NOTES_ELASTICSEARCH_USER)
public class ESUserController {

    ESNotesService notesService;

    @Autowired
    public ESUserController(ESNotesService notesService) {
        this.notesService = notesService;
    }


    @Hidden
    @Operation(summary = "Create a new entry", description = "Create a new entry (note, remark etc) either as the root entry or as a response to another entry", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "DESCRIPTION", content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "DESCRIPTION", content = @Content)
    })
    @PostMapping(NotesConstants.API_ENDPOINT_NOTES_CREATE)
    public ResponseEntity<ESNoteEntity> create(@Valid @RequestBody NoteEntry payloadEntry) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.create(DTOMapper.INSTANCE.toESEntity(payloadEntry)));
    }

    @Hidden
    @Operation(summary = "Update an existing entry provided guid/entryGuid in the payload", description = "Update an existing entry by guid/entryGuid. Current data will be archived as a previous version", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_UPDATE)
    public ResponseEntity<ESNoteEntity> update(@Valid @RequestBody NoteEntry payloadEntry) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.update(DTOMapper.INSTANCE.toESEntity(payloadEntry)));
    }

    @Hidden
    @Operation(summary = "Retrieve entry by guid", description = "Retrieve entry by guid.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_GUID)
    public ResponseEntity<ESNoteEntity> getByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.get(guid));
    }

    @Hidden
    @Operation(summary = "Retrieve entry by entryGuid", description = "Retrieve entry by entryGuid.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_BY_ENTRY_GUID)
    public ResponseEntity<List<ESNoteEntity>> getByEntryGuid(
            @PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED, required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Hidden
    @Operation(summary = "Retrieve entry by threadGuid", description = "Retrieve entries by threadGuid.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_BY_THREAD_GUID)
    public ResponseEntity<List<ESNoteEntity>> getByThreadGuid(
            @PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_THREAD_GUID) UUID threadGuid,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED, required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.get(QueryRequest.builder()
                .searchField(Field.THREAD)
                .searchData(threadGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Search content", description = "Return entries from content matching", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_CONTENT)
    public ResponseEntity<List<ESNoteEntity>> searchContent(
            @RequestParam String searchData,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_ARCHIVED, required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.get(QueryRequest.builder()
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

    @Hidden
    @Operation(summary = "Archive entry by guid", description = "Archive entry by guid. Will also archive all response to this entry.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_GUID)
    public ResponseEntity<List<ESNoteEntity>> archiveByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.archive(guid));
    }

    @Hidden
    @Operation(summary = "Archive all entries by externalGuid", description = "Archive all entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<ESNoteEntity>> archiveExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.archive(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .allEntries(true)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build()));
    }

    @Hidden
    @Operation(summary = "Archive all entries by entryGuid", description = "Archive all entries by entryGuid. Will also archive all responses to this entry.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID)
    public ResponseEntity<List<ESNoteEntity>> archiveEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.archive(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build()));
    }

    @Hidden
    @Operation(summary = "Search archived entries by externalGuid", description = "Search all entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<ESNoteEntity>> getArchivedByExternalGuid(
            @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Hidden
    @Operation(summary = "Search archived entries by entryGuid", description = "Search all entries by entryGuid.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID)
    public ResponseEntity<List<ESNoteEntity>> getArchivedByEntryGuid(
            @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_VERSIONS, required = false, defaultValue = "false") boolean includeVersions,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SEARCHAFTER, required = false) String searchAfter,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SIZE, required = false, defaultValue = "0") int size,
            @RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INCLUDE_SORTORDER, required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.get(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Hidden
    @Operation(summary = "Delete all archived entries by externalGuid", description = "Delete all archived entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_EXTERNAL_GUID)
    public ResponseEntity<List<ESNoteEntity>> deleteArchivedByExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.delete((QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .allEntries(true)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build())));
    }

    @Hidden
    @Operation(summary = "Delete archived entry by entryGuid", description = "Delete archived entry by entryGuid. Will also delete all responses to this entry.", tags = { NotesConstants.OPENAPI_NOTES_ELASTICSEARCH_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_ENTRY_GUID)
    public ResponseEntity<List<ESNoteEntity>> deleteArchivedByEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.delete(QueryRequest.builder()
                .searchField(Field.ENTRY)
                .allEntries(true)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build()));
    }

}
