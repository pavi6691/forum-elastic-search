package com.freelance.forum.resource;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.SearchRequest;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.RequestType;
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

    @PutMapping("/update/entry")
    public ResponseEntity<NotesData> updateByEntryGuid(@RequestBody NotesData notesData) {
        return new ResponseEntity(notesService.update(notesData, ESIndexNotesFields.ENTRY),HttpStatus.OK);
    }

    @PutMapping("/update/guid")
    public ResponseEntity<NotesData> updateByGuid(@RequestBody NotesData notesData) {
        return new ResponseEntity(notesService.update(notesData, ESIndexNotesFields.GUID),HttpStatus.OK);
    }

    @GetMapping("/search/external")
    public ResponseEntity<List<NotesData>> searchByExternalGuid(@RequestParam String externalGuid,
                                                          @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                          @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(notesService.search(new SearchRequest.Builder().setSearch(externalGuid)
                        .setSearchField(ESIndexNotesFields.EXTERNAL).setSearchHistory(getUpdateHistory)
                        .setRequestType(RequestType.EXTERNAL_ENTRIES).setSearchArchived(getArchivedResponse).build()),HttpStatus.OK);
    }

    @GetMapping("/search/entry")
    public ResponseEntity<NotesData> searchByEntryGuid(@RequestParam String entryGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(notesService.search(new SearchRequest.Builder().setSearch(entryGuid)
                .setSearchField(ESIndexNotesFields.ENTRY).setSearchHistory(getUpdateHistory)
                .setRequestType(RequestType.ENTRIES).setSearchArchived(getArchivedResponse).build()),HttpStatus.OK);
    }

    @GetMapping("/search/content")
    public ResponseEntity<List<NotesData>> searchContent(@RequestParam String search,
                                                                @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                                @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse) {
        return new ResponseEntity(notesService.search(new SearchRequest.Builder().setSearch(search)
                .setSearchField(ESIndexNotesFields.CONTENT).setSearchHistory(getUpdateHistory)
                .setRequestType(RequestType.CONTENT).setSearchArchived(getArchivedResponse).build()),HttpStatus.OK);
    }

    @PutMapping("/archive/external")
    public ResponseEntity<List<NotesData>> archiveExternalGuid(@RequestParam String externalGuid) {
        return new ResponseEntity(notesService.archive(externalGuid, ESIndexNotesFields.EXTERNAL),HttpStatus.OK);
    }

    @PutMapping("/archive/entry")
    public ResponseEntity<List<NotesData>> archiveEntryGuid(@RequestParam String entryGuid) {
        return new ResponseEntity(notesService.archive(entryGuid, ESIndexNotesFields.ENTRY),HttpStatus.OK);
    }

    @GetMapping("search/archive/external")
    public ResponseEntity<NotesData> searchArchivedEntriesByExternalGuid(@RequestParam String externalGuid) {
        return new ResponseEntity(notesService.search(new SearchRequest.Builder().setSearch(externalGuid)
                .setSearchField(ESIndexNotesFields.EXTERNAL).setRequestType(RequestType.ARCHIVE)
                .setSearchArchived(true).setSearchThreads(true).setSearchHistory(true).build()),HttpStatus.OK);
    }

    @GetMapping("search/archive/entry")
    public ResponseEntity<NotesData> searchArchivedEntriesByEntryGuid(@RequestParam String entryGuid) {
        return new ResponseEntity(notesService.search(new SearchRequest.Builder().setSearch(entryGuid)
                .setSearchField(ESIndexNotesFields.ENTRY).setRequestType(RequestType.ARCHIVE)
                .setSearchArchived(true).setSearchThreads(true).setSearchHistory(true).build()),HttpStatus.OK);
    }

    @DeleteMapping("/delete/external")
    public ResponseEntity<List<NotesData>> deleteExternalGuid(@RequestParam String externalGuid, @RequestParam String entriesToDelete) {
        return new ResponseEntity(notesService.delete(externalGuid, ESIndexNotesFields.EXTERNAL,entriesToDelete),HttpStatus.OK);
    }

    @DeleteMapping("/delete/entry")
    public ResponseEntity<List<NotesData>> deleteEntryGuidGuid(@RequestParam String entryGuid, @RequestParam String entriesToDelete) {
        return new ResponseEntity(notesService.delete(entryGuid, ESIndexNotesFields.ENTRY,entriesToDelete),HttpStatus.OK);
    }

    @PostMapping("/createIndex")
    public ResponseEntity<String> createIndex(@RequestParam String indexName) {
        return new ResponseEntity(notesService.createIndex(indexName),HttpStatus.OK);
    }
}