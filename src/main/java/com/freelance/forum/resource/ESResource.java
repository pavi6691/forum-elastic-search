package com.freelance.forum.resource;

import com.freelance.forum.elasticsearch.pojo.ElasticDataModel;
import com.freelance.forum.elasticsearch.queries.ESIndexFields;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.service.ESService;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ESResource {

    @Autowired
    ESService service;

    @PostMapping("/create")
    public ResponseEntity<ElasticDataModel> saveNew(@RequestBody ElasticDataModel elasticDataModel) {
        return new ResponseEntity(service.saveNew(elasticDataModel),HttpStatus.OK);
    }

    @GetMapping("/search/guid")
    public ResponseEntity<ElasticDataModel> searchByGuid(@RequestParam String guid) {
        return new ResponseEntity(service.searchByGuid(guid),HttpStatus.OK);
    }

    @PutMapping("/update/entry")
    public ResponseEntity<ElasticDataModel> updateByEntryGuid(@RequestBody ElasticDataModel elasticDataModel) {
        return new ResponseEntity(service.update(elasticDataModel, ESIndexFields.ENTRY),HttpStatus.OK);
    }

    @PutMapping("/update/guid")
    public ResponseEntity<ElasticDataModel> updateByGuid(@RequestBody ElasticDataModel elasticDataModel) {
        return new ResponseEntity(service.update(elasticDataModel, ESIndexFields.GUID),HttpStatus.OK);
    }

    @GetMapping("/search/external")
    public ResponseEntity<ElasticDataModel> searchByExternalGuid(@RequestParam String externalGuid,
                                                             @RequestParam(required = false, defaultValue = "false") boolean searchUpdateHistory,
                                                             @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(service.search(String.format(Queries.QUERY_BY_GUID, ESIndexFields.EXTERNAL.getEsFieldName(),externalGuid),
                                    searchUpdateHistory,getArchivedResponse,true,SortOrder.DESC),HttpStatus.OK);
    }

    @GetMapping("/search/entry")
    public ResponseEntity<ElasticDataModel> searchByEntryGuid(@RequestParam String entryGuid,
                                              @RequestParam(required = false, defaultValue = "false") boolean searchUpdateHistory,
                                              @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(service.search(String.format(Queries.QUERY_BY_GUID, ESIndexFields.ENTRY.getEsFieldName(),entryGuid),searchUpdateHistory,
                                    getArchivedResponse,true,SortOrder.ASC),HttpStatus.OK);
    }

    @PutMapping("/archive/external")
    public ResponseEntity<ElasticDataModel> archiveExternalGuid(@RequestParam String externalGuid) {
        return new ResponseEntity(service.archive(externalGuid, ESIndexFields.EXTERNAL),HttpStatus.OK);
    }

    @PutMapping("/archive/entry")
    public ResponseEntity<ElasticDataModel> archiveEntryGuid(@RequestParam String entryGuid) {
        return new ResponseEntity(service.archive(entryGuid, ESIndexFields.ENTRY),HttpStatus.OK);
    }

    @GetMapping("search/archive/external")
    public ResponseEntity<ElasticDataModel> searchArchivedEntriesByExternalGuid(@RequestParam String externalGuid) {
        return new ResponseEntity(service.searchArchive(externalGuid, ESIndexFields.EXTERNAL),HttpStatus.OK);
    }

    @GetMapping("search/archive/entry")
    public ResponseEntity<ElasticDataModel> searchArchivedEntriesByEntryGuid(@RequestParam String entryGuid) {
        return new ResponseEntity(service.searchArchive(entryGuid, ESIndexFields.ENTRY),HttpStatus.OK);
    }

    @DeleteMapping("/delete/external")
    public ResponseEntity<ElasticDataModel> deleteExternalGuid(@RequestParam String externalGuid,@RequestParam boolean deleteOnlyArchived) {
        return new ResponseEntity(service.delete(externalGuid, ESIndexFields.EXTERNAL,deleteOnlyArchived),HttpStatus.OK);
    }

    @DeleteMapping("/delete/entry")
    public ResponseEntity<ElasticDataModel> deleteEntryGuidGuid(@RequestParam String entryGuid,@RequestParam boolean deleteOnlyArchived) {
        return new ResponseEntity(service.delete(entryGuid, ESIndexFields.ENTRY,deleteOnlyArchived),HttpStatus.OK);
    }

    @PostMapping("/createIndex")
    public ResponseEntity<String> createIndex(@RequestParam String indexName) {
        return new ResponseEntity(service.createIndex(indexName),HttpStatus.OK);
    }
}