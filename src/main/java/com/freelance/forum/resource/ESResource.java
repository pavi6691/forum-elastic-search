package com.freelance.forum.resource;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.*;
import com.freelance.forum.elasticsearch.queries.generics.ESIndexNotesFields;
import com.freelance.forum.service.INotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ESResource {
    
    @Autowired
    INotesService notesService;

    @PostMapping("/create")
    public ResponseEntity<NotesData> saveNew(@RequestBody NotesData notesData) {
        return new ResponseEntity(notesService.saveNew(notesData),HttpStatus.OK);
    }

    @GetMapping("/search/guid")
    public ResponseEntity<NotesData> searchByGuid(@RequestParam String guid) {
        return new ResponseEntity(notesService.searchByGuid(UUID.fromString(guid)),HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<NotesData> update(@RequestBody NotesData notesData) {
        return new ResponseEntity(notesService.update(notesData),HttpStatus.OK);
    }

    @GetMapping("/search/external")
    public ResponseEntity<List<NotesData>> searchByExternalGuid(@RequestParam String externalGuid,
                                                          @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                          @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(notesService.search(new SearchByExternalGuid().setSearchBy(externalGuid)
                .setGetUpdateHistory(getUpdateHistory).setGetArchived(getArchivedResponse)),HttpStatus.OK);
    }

    @GetMapping("/search/entry")
    public ResponseEntity<NotesData> searchByEntryGuid(@RequestParam String entryGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(notesService.search(new SearchByEntryGuid().setSearchBy(entryGuid)
                .setGetUpdateHistory(getUpdateHistory).setGetArchived(getArchivedResponse)),HttpStatus.OK);
    }

    @GetMapping("/search/content")
    public ResponseEntity<List<NotesData>> searchContent(@RequestParam String search,
                                                                @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                                @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(notesService.search(new SearchByContent().setContentToSearch(search)
                .setGetUpdateHistory(getUpdateHistory).setGetArchived(getArchivedResponse)),HttpStatus.OK);
    }

    @PutMapping("/archive/external")
    public ResponseEntity<List<NotesData>> archiveExternalGuid(@RequestParam String externalGuid) {
        return new ResponseEntity(notesService.archive(new SearchByExternalGuid().setSearchBy(externalGuid)
                .setGetUpdateHistory(true).setGetArchived(true)),HttpStatus.OK);
    }

    @PutMapping("/archive/entry")
    public ResponseEntity<List<NotesData>> archiveEntryGuid(@RequestParam String entryGuid) {
        return new ResponseEntity(notesService.archive(new SearchByEntryGuid()
                .setSearchBy(entryGuid).setGetUpdateHistory(true).setGetArchived(true)),HttpStatus.OK);
    }

    @GetMapping("search/archive/external")
    public ResponseEntity<NotesData> searchArchivedEntriesByExternalGuid(@RequestParam String externalGuid) {
        return new ResponseEntity(notesService.search(new SearchArchivedByExternalGuid().setExternalGuid(externalGuid)
                .setGetUpdateHistory(true)),HttpStatus.OK);
    }

    @GetMapping("search/archive/entry")
    public ResponseEntity<NotesData> searchArchivedEntriesByEntryGuid(@RequestParam String entryGuid) {
        return new ResponseEntity(notesService.search(new SearchArchivedByEntryGuid().setEntryGuid(entryGuid)
                .setGetUpdateHistory(true))
                ,HttpStatus.OK);
    }

    @DeleteMapping("/delete/external")
    public ResponseEntity<List<NotesData>> deleteByExternalGuid(@RequestParam String externalGuid, @RequestParam String entriesToDelete) {
        return new ResponseEntity(notesService.delete((new SearchByExternalGuid().setSearchBy(externalGuid)
                .setGetUpdateHistory(true).setGetArchived(true)),entriesToDelete),HttpStatus.OK);
    }

    @DeleteMapping("/delete/entry")
    public ResponseEntity<List<NotesData>> deleteByEntryGuid(@RequestParam String entryGuid, @RequestParam String entriesToDelete) {
        return new ResponseEntity(notesService.delete(new SearchByEntryGuid().setSearchBy(entryGuid)
                .setGetUpdateHistory(true).setGetArchived(true),entriesToDelete),HttpStatus.OK);
    }

    @DeleteMapping("/delete/guid")
    public ResponseEntity<List<NotesData>> deleteByGuid(@RequestParam String guid) {
        return new ResponseEntity(notesService.delete(guid),HttpStatus.OK);
    }

    @PostMapping("/createIndex")
    public ResponseEntity<String> createIndex(@RequestParam String indexName) {
        return new ResponseEntity(notesService.createIndex(indexName),HttpStatus.OK);
    }
}