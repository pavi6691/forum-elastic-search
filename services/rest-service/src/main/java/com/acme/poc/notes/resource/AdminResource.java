package com.acme.poc.notes.resource;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.SearchByExternalGuid;
import com.acme.poc.notes.service.INotesAdminService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_ADMIN)
public class AdminResource {

    INotesAdminService notesAdminService;


    public AdminResource(INotesAdminService notesAdminService) {
        this.notesAdminService = notesAdminService;
    }


    @GetMapping(NotesConstants.API_ENDPOINT_ADMIN_GET_ALL)
    public ResponseEntity<List<NotesData>> getAll(@RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INDEX_NAME) String indexName) {
        log.debug("getAll()");
        return ResponseEntity.ok(notesAdminService.getAll(indexName));
    }

    @GetMapping(NotesConstants.API_ENDPOINT_ADMIN_GET_ALL_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> getByExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID externalGuid,
                                                        @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                    @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
                                                    @RequestParam(required = false) String searchAfter,
                                                    @RequestParam(required = false, defaultValue = "0") int size,
                                                    @RequestParam(required = false) SortOrder sortOrder) {
        log.debug("getByExternalGuid()");
        return ResponseEntity.ok(notesAdminService.searchByExternalGuid(SearchByExternalGuid.builder()
                .searchGuid(externalGuid.toString())
                .includeVersions(includeVersions)
                .includeArchived(includeArchived)
                .size(size)
                .searchAfter(searchAfter)
                .sortOrder(sortOrder)
                .build()));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> deleteByExternalGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
        log.debug("deleteByExternalGuid()");
        return ResponseEntity.ok(notesAdminService.deleteByExternalGuid(externalGuid));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> deleteByEntryGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
        log.debug("deleteByEntryGuid()");
        return ResponseEntity.ok(notesAdminService.deleteByEntryGuid(entryGuid));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_THREAD_GUID)
    public ResponseEntity<List<NotesData>> deleteByThreadGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_THREAD_GUID) UUID threadGuid) {
        log.debug("deleteByThreadGuid()");
        return ResponseEntity.ok(notesAdminService.deleteByThreadGuid(threadGuid));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_GUID)
    public ResponseEntity<NotesData> deleteByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID guid) {
        return ResponseEntity.ok(notesAdminService.deleteByGuid(guid));
    }

}
