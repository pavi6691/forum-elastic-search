package com.acme.poc.notes.restservice.controller;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.service.esservice.ESNotesClientService;
import com.acme.poc.notes.restservice.service.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.service.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.service.generics.queries.enums.Match;
import com.acme.poc.notes.restservice.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_NOTES)
public class ESController {

    ESNotesClientService notesService;


    public ESController(ESNotesClientService notesService) {
        this.notesService = notesService;
    }


    @Operation(summary = "Create a new entry", description = "Create a new entry (note, remark etc) either as the root entry or as a response to another entry", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "DESCRIPTION", content = { @Content(mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "DESCRIPTION", content = @Content)
    })
    @PostMapping(NotesConstants.API_ENDPOINT_NOTES_CREATE)
    public ResponseEntity<NotesData> create(@Valid @RequestBody NotesData notesData) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.create(notesData));
    }

    @Operation(summary = "Update an existing entry by guid/entryGuid", description = "Update an existing entry by guid/entryGuid. Current data will be archived as a previous version", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_UPDATE)
    public ResponseEntity<NotesData> updateByGuid(@RequestBody NotesData notesData) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.update(notesData));
    }

    @Operation(summary = "Retrieve entry by guid", description = "Retrieve entry by guid.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_GUID)
    public ResponseEntity<NotesData> getByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.get(UUID.fromString(guid.toString())));
    }

    @Operation(summary = "Retrieve entry by entryGuid", description = "Retrieve entry by entryGuid.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> searchByEntryGuid(
                                                        @PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid,
                                                        @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                        @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
                                                        @RequestParam(required = false) String searchAfter,
                                                        @RequestParam(required = false, defaultValue = "0") int size,
                                                        @RequestParam(required = false) SortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.searchByEntryGuid(QueryRequest.builder()
                .searchField(Match.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Search entries", description = "Return entries that matches the search.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_CONTENT)
    public ResponseEntity<List<NotesData>> searchContent(
                                                        @RequestParam String searchData,
                                                        @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                        @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
                                                        @RequestParam(required = false) String searchAfter,
                                                        @RequestParam(required = false, defaultValue = "0") int size,
                                                        @RequestParam(required = false) SortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.searchByContent(QueryRequest.builder()
                .searchField(Match.CONTENT)
                .searchData(searchData)
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Archive entry by guid", description = "Archive entry by guid. Will also archive all response to this entry.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_GUID)
    public ResponseEntity<List<NotesData>> archiveByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.archive(guid));
    }

    @Operation(summary = "Archive all entries by externalGuid", description = "Archive all entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> archiveExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.archive(QueryRequest.builder()
                .searchField(Match.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build()));
    }

    @Operation(summary = "Archive all entries by entryGuid", description = "Archive all entries by entryGuid. Will also archive all responses to this entry.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> archiveEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.archive(QueryRequest.builder()
                .searchField(Match.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.EXCLUDE_ARCHIVED))
                .build()));
    }

    @Operation(summary = "Search archived entries by externalGuid", description = "Search all entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_ARCHIVED_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> searchArchivedEntriesByExternalGuid(
                                                        @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid,
                                                        @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                        @RequestParam(required = false) String searchAfter,
                                                        @RequestParam(required = false, defaultValue = "0") int size,
                                                        @RequestParam(required = false) SortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.searchArchivedByExternalGuid(QueryRequest.builder()
                .searchField(Match.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Search archived entries by entryGuid", description = "Search all entries by entryGuid.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_ARCHIVED_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> searchArchivedEntriesByEntryGuid(
                                                        @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid,
                                                        @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                        @RequestParam(required = false) String searchAfter,
                                                        @RequestParam(required = false, defaultValue = "0") int size,
                                                        @RequestParam(required = false) SortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.searchArchivedByEntryGuid(QueryRequest.builder()
                .searchField(Match.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Delete all archived entries by externalGuid", description = "Delete all archived entries by externalGuid.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> deleteArchivedByExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.deleteArchivedByExternalGuid((QueryRequest.builder()
                .searchField(Match.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build())));
    }

    @Operation(summary = "Delete archived entry by entryGuid", description = "Delete archived entry by entryGuid. Will also delete all responses to this entry.", tags = { NotesConstants.OPENAPI_NOTES_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> deleteArchivedByEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesService.deleteArchivedByEntryGuid(QueryRequest.builder()
                .searchField(Match.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ONLY_ARCHIVED))
                .build()));
    }

}
