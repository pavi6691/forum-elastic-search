package com.acme.poc.notes.resource;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.service.INotesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_ADMIN + NotesConstants.API_ENDPOINT_ADMIN_ES)
public class ESAdminResource {

    INotesService notesService;


    public ESAdminResource(INotesService notesService) {
        this.notesService = notesService;
    }


    @PostMapping(NotesConstants.API_ENDPOINT_ADMIN_ES_INDEX_CREATE)
    public ResponseEntity<String> createIndex(@RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INDEX_NAME) String indexName) {
        return ResponseEntity.ok(notesService.createIndex(indexName));
    }

}
