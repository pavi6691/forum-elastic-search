package com.acme.poc.notes.resource;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.*;
import com.acme.poc.notes.service.INotesService;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX)
public class ESResource {
    
    @Autowired
    INotesService notesService;

    @PostMapping(NotesConstants.API_ENDPOINT_NOTES_CREATE)
    public ResponseEntity<NotesData> saveNew(@Valid @RequestBody NotesData notesData) {
        return new ResponseEntity(notesService.saveNew(notesData),HttpStatus.OK);
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_GUID)
    public ResponseEntity<NotesData> searchByGuid(@RequestParam @JsonDeserialize(using = UUIDDeserializer.class) UUID guid) {
        return new ResponseEntity(notesService.searchByGuid(UUID.fromString(guid.toString())),HttpStatus.OK);
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_UPDATE_BY_GUID)
    public ResponseEntity<NotesData> updateByGuid(@RequestBody NotesData notesData) {
        return new ResponseEntity(notesService.updateByGuid(notesData),HttpStatus.OK);
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_UPDATE_BY_ENTRY_GUID)
    public ResponseEntity<NotesData> updateByEntryGuid(@RequestBody NotesData notesData) {
        return new ResponseEntity(notesService.updateByEntryGuid(notesData),HttpStatus.OK);
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_EXTERNAL)
    public ResponseEntity<List<NotesData>> searchByExternalGuid(@RequestParam @JsonDeserialize(using = UUIDDeserializer.class) UUID externalGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse,
                                                       @RequestParam(required = false) String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return new ResponseEntity(notesService.search(SearchByExternalGuid.builder().searchGuid(externalGuid.toString()).
                getUpdateHistory(getUpdateHistory).getArchived(getArchivedResponse).size(size)
                .searchAfter(searchAfter).sortOrder(sortOrder).build()),HttpStatus.OK);
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_ENTRY)
    public ResponseEntity<NotesData> searchByEntryGuid(@RequestParam @JsonDeserialize(using = UUIDDeserializer.class) UUID entryGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse,
                                                       @RequestParam(required = false) String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return new ResponseEntity(notesService.search(SearchByEntryGuid.builder().searchGuid(entryGuid.toString())
                .getUpdateHistory(getUpdateHistory).getArchived(getArchivedResponse)
                .searchAfter(searchAfter).size(size).sortOrder(sortOrder).build()),HttpStatus.OK);
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_CONTENT)
    public ResponseEntity<List<NotesData>> searchContent(@RequestParam String search,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getArchivedResponse,
                                                       @RequestParam(required = false) String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return new ResponseEntity(notesService.search(SearchByContent.builder().contentToSearch(search)
                .getUpdateHistory(getUpdateHistory).getArchived(getArchivedResponse)
                .searchAfter(searchAfter).size(size).sortOrder(sortOrder).build()),HttpStatus.OK);
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_GUID)
    public ResponseEntity<List<NotesData>> archiveByGuid(@PathVariable @JsonDeserialize(using = UUIDDeserializer.class) UUID guid) {
        return new ResponseEntity(notesService.archive(guid),HttpStatus.OK);
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> archiveExternalGuid(@PathVariable @JsonDeserialize(using = UUIDDeserializer.class) UUID externalGuid) {
        return new ResponseEntity(notesService.archive(SearchByExternalGuid.builder().searchGuid(externalGuid.toString())
                .getUpdateHistory(true).getArchived(false).build()),HttpStatus.OK);
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID) 
    public ResponseEntity<List<NotesData>> archiveEntryGuid(@PathVariable @JsonDeserialize(using = UUIDDeserializer.class) UUID entryGuid) {
        return new ResponseEntity(notesService.archive(SearchByEntryGuid.builder()
                .searchGuid(entryGuid.toString()).getUpdateHistory(true).getArchived(false).build()),HttpStatus.OK);
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_EXTERNAL_ARCHIVED)
    public ResponseEntity<NotesData> searchArchivedEntriesByExternalGuid(@RequestParam @JsonDeserialize(using = UUIDDeserializer.class) 
                                                                             UUID externalGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                       @RequestParam(required = false) String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return new ResponseEntity(notesService.search(SearchArchivedByExternalGuid.builder().searchGuid(externalGuid.toString())
                .getArchived(true).getUpdateHistory(getUpdateHistory)
                .searchAfter(searchAfter).size(size).sortOrder(sortOrder).build()),HttpStatus.OK);
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_ENTRY_ARCHIVED)
    public ResponseEntity<NotesData> searchArchivedEntriesByEntryGuid(
                                                       @RequestParam @JsonDeserialize(using = UUIDDeserializer.class) UUID entryGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean getUpdateHistory,
                                                       @RequestParam(required = false) String searchAfter, 
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return new ResponseEntity(notesService.search(SearchArchivedByEntryGuid.builder().searchGuid(entryGuid.toString())
                .getArchived(true).getUpdateHistory(getUpdateHistory).searchAfter(searchAfter).size(size).sortOrder(sortOrder).build())
                ,HttpStatus.OK);
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> deleteByExternalGuid(
            @PathVariable @JsonDeserialize(using = UUIDDeserializer.class) UUID externalGuid) {
        return new ResponseEntity(notesService.delete((SearchByExternalGuid.builder().searchGuid(externalGuid.toString())
                .getUpdateHistory(true).getArchived(true)).build()),HttpStatus.OK);
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> deleteByEntryGuid(
            @PathVariable @JsonDeserialize(using = UUIDDeserializer.class) UUID entryGuid) {
        return new ResponseEntity(notesService.delete(SearchByEntryGuid.builder().searchGuid(entryGuid.toString())
                .getUpdateHistory(true).getArchived(true).build()),HttpStatus.OK);
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> deleteArchivedByExternalGuid(
            @PathVariable @JsonDeserialize(using = UUIDDeserializer.class) UUID externalGuid) {
        return new ResponseEntity(notesService.delete((SearchArchivedByExternalGuid.builder().searchGuid(externalGuid.toString())
                .getUpdateHistory(true).getArchived(true)).build()),HttpStatus.OK);
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> deleteArchivedByEntryGuid(
            @PathVariable @JsonDeserialize(using = UUIDDeserializer.class) UUID entryGuid) {
        return new ResponseEntity(notesService.delete(SearchArchivedByEntryGuid.builder().searchGuid(entryGuid.toString())
                .getUpdateHistory(true).getArchived(true).build()),HttpStatus.OK);
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_GUID)
    public ResponseEntity<List<NotesData>> deleteByGuid(@PathVariable @JsonDeserialize(using = UUIDDeserializer.class) UUID guid) {
        return new ResponseEntity(notesService.delete(guid.toString()),HttpStatus.OK);
    }
}