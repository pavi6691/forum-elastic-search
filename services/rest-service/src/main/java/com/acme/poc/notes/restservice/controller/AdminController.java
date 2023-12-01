package com.acme.poc.notes.restservice.controller;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.models.NoteSortOrder;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.generics.interfaces.INotesAdminOperations;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Match;
import com.acme.poc.notes.restservice.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_ADMIN)
public class AdminController {

    INotesAdminOperations<NotesData> notesAdminService;


    public AdminController(@Qualifier("ESNotesAdminService") INotesAdminOperations notesAdminService) {
        this.notesAdminService = notesAdminService;
    }


    @Operation(summary = "Get all notes", description = "Retrieve all notes", tags = { NotesConstants.OPENAPI_ADMIN_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_ADMIN_GET_ALL)
    public ResponseEntity<List<NotesData>> getAll(@RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INDEX_NAME) String indexName) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesAdminService.get(indexName));
    }

    @Operation(summary = "Get all notes by externalGuid", description = "Retrieve all notes by externalGuid", tags = { NotesConstants.OPENAPI_ADMIN_TAG })
    @GetMapping(NotesConstants.API_ENDPOINT_ADMIN_GET_ALL_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> getByExternalGuid(
                                                    @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid,
                                                    @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                    @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
                                                    @RequestParam(required = false) String searchAfter,
                                                    @RequestParam(required = false, defaultValue = "0") int size,
                                                    @RequestParam(required = false) NoteSortOrder sortOrder) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesAdminService.get(QueryRequest.builder()
                .searchField(Match.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(includeVersions ? Filter.INCLUDE_VERSIONS : Filter.EXCLUDE_VERSIONS,
                        includeArchived ? Filter.INCLUDE_ARCHIVED : Filter.EXCLUDE_ARCHIVED))
                .size(size)
                .searchAfter(searchAfter)
                .sortOrder(sortOrder)
                .build()));
    }

    @Operation(summary = "Delete all notes by externalGuid", description = "Delete all notes by externalGuid", tags = { NotesConstants.OPENAPI_ADMIN_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> deleteByExternalGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesAdminService.delete(QueryRequest.builder()
                .searchField(Match.EXTERNAL)
                .searchData(externalGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete all notes by entryGuid", description = "Delete all notes by entryGuid", tags = { NotesConstants.OPENAPI_ADMIN_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> deleteByEntryGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesAdminService.delete(QueryRequest.builder()
                .searchField(Match.ENTRY)
                .searchData(entryGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete all notes by threadGuid", description = "Delete all notes by threadGuid", tags = { NotesConstants.OPENAPI_ADMIN_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_THREAD_GUID)
    public ResponseEntity<List<NotesData>> deleteByThreadGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_THREAD_GUID) UUID threadGuid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesAdminService.delete(QueryRequest.builder()
                .searchField(Match.THREAD)
                .searchData(threadGuid.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build()));
    }

    @Operation(summary = "Delete note by guid", description = "Delete note by guid", tags = { NotesConstants.OPENAPI_ADMIN_TAG })
    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_GUID)
    public ResponseEntity<NotesData> deleteByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID guid) {
        log.debug("{}", LogUtil.method());
        return ResponseEntity.ok(notesAdminService.delete(guid));
    }

}
