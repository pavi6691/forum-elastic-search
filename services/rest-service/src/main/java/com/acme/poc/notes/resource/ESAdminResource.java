package com.acme.poc.notes.resource;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.service.INotesAdminService;
import com.acme.poc.notes.util.LogUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_ADMIN + NotesConstants.API_ENDPOINT_ADMIN_ES)
public class ESAdminResource {

    INotesAdminService notesAdminService;


    public ESAdminResource(INotesAdminService notesAdminService) {
        this.notesAdminService = notesAdminService;
    }


    @Operation(summary = "Create initial index", description = "Create the initial index in Elasticsearch for Notes support", tags = { NotesConstants.OPENAPI_ELASTICSEARCH_ADMIN_TAG })
    @PostMapping(NotesConstants.API_ENDPOINT_ADMIN_ES_INDEX_CREATE)
    public ResponseEntity<String> createIndex(@RequestParam(name = NotesConstants.API_ENDPOINT_QUERY_PARAMETER_INDEX_NAME) String indexName) {
        log.debug("{} index: {}", LogUtil.method(), indexName);
        return ResponseEntity.ok(notesAdminService.createIndex(indexName));
    }

}
