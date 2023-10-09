package com.freelance.forum.resource;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexFields;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.service.ESService;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ESResource {

    @Autowired
    ESService service;

    @PostMapping("/create")
    public ResponseEntity<NotesData> saveNew(@RequestBody NotesData notesData) {
        return new ResponseEntity(service.saveNew(notesData),HttpStatus.OK);
    }

    @GetMapping("/search/guid")
    public ResponseEntity<NotesData> searchByGuid(@RequestParam String guid) {
        return new ResponseEntity(service.searchByGuid(guid),HttpStatus.OK);
    }

    @PutMapping("/update/entry")
    public ResponseEntity<NotesData> updateByEntryGuid(@RequestBody NotesData notesData) {
        return new ResponseEntity(service.update(notesData, ESIndexFields.ENTRY),HttpStatus.OK);
    }

    @PutMapping("/update/guid")
    public ResponseEntity<NotesData> updateByGuid(@RequestBody NotesData notesData) {
        return new ResponseEntity(service.update(notesData, ESIndexFields.GUID),HttpStatus.OK);
    }

    @GetMapping("/search/external")
    public ResponseEntity<NotesData> searchByExternalGuid(@RequestParam String externalGuid,
                                                          @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                          @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(service.searchEntries(externalGuid,ESIndexFields.EXTERNAL,
                                    getUpdateHistory,getArchivedResponse,true,SortOrder.DESC),HttpStatus.OK);
    }

    @GetMapping("/search/entry")
    public ResponseEntity<NotesData> searchByEntryGuid(@RequestParam String entryGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(service.searchEntries(entryGuid,ESIndexFields.ENTRY,getUpdateHistory,
                                    getArchivedResponse,true,SortOrder.ASC),HttpStatus.OK);
    }

    @PutMapping("/archive/external")
    public ResponseEntity<NotesData> archiveExternalGuid(@RequestParam String externalGuid) {
        return new ResponseEntity(service.archive(externalGuid, ESIndexFields.EXTERNAL),HttpStatus.OK);
    }

    @PutMapping("/archive/entry")
    public ResponseEntity<NotesData> archiveEntryGuid(@RequestParam String entryGuid) {
        return new ResponseEntity(service.archive(entryGuid, ESIndexFields.ENTRY),HttpStatus.OK);
    }

    @GetMapping("search/archive/external")
    public ResponseEntity<NotesData> searchArchivedEntriesByExternalGuid(@RequestParam String externalGuid) {
        return new ResponseEntity(service.searchArchive(externalGuid, ESIndexFields.EXTERNAL),HttpStatus.OK);
    }

    @GetMapping("search/archive/entry")
    public ResponseEntity<NotesData> searchArchivedEntriesByEntryGuid(@RequestParam String entryGuid) {
        return new ResponseEntity(service.searchArchive(entryGuid, ESIndexFields.ENTRY),HttpStatus.OK);
    }

    @DeleteMapping("/delete/external")
    public ResponseEntity<List<NotesData>> deleteExternalGuid(@RequestParam String externalGuid, @RequestParam String entriesToDelete) {
        return new ResponseEntity(service.delete(externalGuid, ESIndexFields.EXTERNAL,entriesToDelete),HttpStatus.OK);
    }

    @DeleteMapping("/delete/entry")
    public ResponseEntity<List<NotesData>> deleteEntryGuidGuid(@RequestParam String entryGuid, @RequestParam String entriesToDelete) {
        return new ResponseEntity(service.delete(entryGuid, ESIndexFields.ENTRY,entriesToDelete),HttpStatus.OK);
    }

    @PostMapping("/createIndex")
    public ResponseEntity<String> createIndex(@RequestParam String indexName) {
        return new ResponseEntity(service.createIndex(indexName),HttpStatus.OK);
    }
}