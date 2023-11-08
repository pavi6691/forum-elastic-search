package com.acme.poc.notes.resource;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.service.INotesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_ADMIN)
public class AdminResource {

//    INotesAdminService notesAdminService;
//
//
//    public AdminResource(INotesAdminService notesAdminService) {
//        this.notesAdminService = notesAdminService;
//    }
//
//
//    @GetMapping(NotesConstants.API_ENDPOINT_ADMIN_GET_ALL)
//    public ResponseEntity<String> getAll(@RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INDEX_NAME) String indexName) {
//        log.debug("getAll()");
//        return ResponseEntity.ok(notesAdminService.getAll(indexName));
//    }
//
//    @GetMapping(NotesConstants.API_ENDPOINT_ADMIN_GET_ALL_BY_EXTERNAL_GUID)
//    public ResponseEntity<String> getByExternalGuid(@RequestParam(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
//        log.debug("getByExternalGuid()");
//        return ResponseEntity.ok(notesAdminService.getByExternalGuid(externalGuid));
//    }
//
//    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_EXTERNAL_GUID)
//    public ResponseEntity<String> deleteByExternalGuid(@RequestParam(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) UUID externalGuid) {
//        log.debug("deleteByExternalGuid()");
//        return ResponseEntity.ok(notesAdminService.deleteByExternalGuid(externalGuid));
//    }
//
//    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_ENTRY_GUID)
//    public ResponseEntity<String> deleteByEntryGuid(@RequestParam(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) UUID entryGuid) {
//        log.debug("deleteByEntryGuid()");
//        return ResponseEntity.ok(notesAdminService.deleteByEntryGuid(entryGuid));
//    }
//
//    @DeleteMapping(NotesConstants.API_ENDPOINT_ADMIN_DELETE_BY_THREAD_GUID)
//    public ResponseEntity<String> deleteByThreadGuid(@RequestParam(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_THREAD_GUID) UUID threadGuid) {
//        log.debug("deleteByThreadGuid()");
//        return ResponseEntity.ok(notesAdminService.deleteByThreadGuid(entryGuid));
//    }

}
