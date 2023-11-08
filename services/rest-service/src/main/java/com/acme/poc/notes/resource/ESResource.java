package com.acme.poc.notes.resource;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.*;
import com.acme.poc.notes.service.INotesService;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(NotesConstants.API_ENDPOINT_PREFIX + NotesConstants.API_ENDPOINT_NOTES)
public class ESResource {

    INotesService notesService;


    public ESResource(INotesService notesService) {
        this.notesService = notesService;
    }


    @PostMapping(NotesConstants.API_ENDPOINT_NOTES_CREATE)
    public ResponseEntity<NotesData> create(@Valid @RequestBody NotesData notesData) {
        return ResponseEntity.ok(notesService.create(notesData));
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_UPDATE_BY_GUID)
    public ResponseEntity<NotesData> updateByGuid(@RequestBody NotesData notesData) {
        return ResponseEntity.ok(notesService.updateByGuid(notesData));
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_UPDATE_BY_ENTRY_GUID)
    public ResponseEntity<NotesData> updateByEntryGuid(@RequestBody NotesData notesData) {
        return ResponseEntity.ok(notesService.updateByEntryGuid(notesData));
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_GUID)
    public ResponseEntity<NotesData> getByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID guid) {
        return ResponseEntity.ok(notesService.getByGuid(UUID.fromString(guid.toString())));
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> getByExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID externalGuid,
                                                             @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                             @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
                                                             @RequestParam(required = false) String searchAfter,
                                                             @RequestParam(required = false, defaultValue = "0") int size,
                                                             @RequestParam(required = false) SortOrder sortOrder) {
        return ResponseEntity.ok(notesService.search(SearchByExternalGuid.builder()
                .searchGuid(externalGuid.toString())
                .includeVersions(includeVersions)
                .includeArchived(includeArchived)
                .size(size)
                .searchAfter(searchAfter)
                .sortOrder(sortOrder)
                .build()));
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_GET_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> searchByEntryGuid(@PathVariable(name = NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID entryGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                       @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
                                                       @RequestParam(required = false) String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return ResponseEntity.ok(notesService.search(SearchByEntryGuid.builder()
                .searchGuid(entryGuid.toString())
                .includeVersions(includeVersions)
                .includeArchived(includeArchived)
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_CONTENT)
    public ResponseEntity<List<NotesData>> searchContent(@RequestParam String search,
                                                       @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                       @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
                                                       @RequestParam(required = false) String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return ResponseEntity.ok(notesService.search(SearchByContent.builder()
                .contentToSearch(search)
                .includeVersions(includeVersions)
                .includeArchived(includeArchived)
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_GUID)
    public ResponseEntity<List<NotesData>> archiveByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID guid) {
        return ResponseEntity.ok(notesService.archive(guid));
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> archiveExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID externalGuid) {
        return ResponseEntity.ok(notesService.archive(SearchByExternalGuid.builder()
                .searchGuid(externalGuid.toString())
                .includeVersions(true)
                .includeArchived(false)
                .build()));
    }

    @PutMapping(NotesConstants.API_ENDPOINT_NOTES_ARCHIVE_BY_ENTRY_GUID) 
    public ResponseEntity<List<NotesData>> archiveEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID entryGuid) {
        return ResponseEntity.ok(notesService.archive(SearchByEntryGuid.builder()
                .searchGuid(entryGuid.toString())
                .includeVersions(true)
                .includeArchived(false)
                .build()));
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_ARCHIVED_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> searchArchivedEntriesByExternalGuid(
                                                       @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID externalGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                       @RequestParam(required = false) String searchAfter,
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return ResponseEntity.ok(notesService.search(SearchArchivedByExternalGuid.builder()
                .searchGuid(externalGuid.toString())
                .includeArchived(true)
                .includeVersions(includeVersions)
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @GetMapping(NotesConstants.API_ENDPOINT_NOTES_SEARCH_ARCHIVED_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> searchArchivedEntriesByEntryGuid(
                                                       @PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID entryGuid,
                                                       @RequestParam(required = false, defaultValue = "false") boolean includeVersions,
                                                       @RequestParam(required = false) String searchAfter, 
                                                       @RequestParam(required = false, defaultValue = "0") int size,
                                                       @RequestParam(required = false) SortOrder sortOrder) {
        return ResponseEntity.ok(notesService.search(SearchArchivedByEntryGuid.builder()
                .searchGuid(entryGuid.toString())
                .includeArchived(true)
                .includeVersions(includeVersions)
                .searchAfter(searchAfter)
                .size(size)
                .sortOrder(sortOrder)
                .build()));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> deleteByExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID externalGuid) {
        return ResponseEntity.ok(notesService.delete((SearchByExternalGuid.builder()
                .searchGuid(externalGuid.toString())
                .includeVersions(true)
                .includeArchived(true))
                .build()));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> deleteByEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID entryGuid) {
        return ResponseEntity.ok(notesService.delete(SearchByEntryGuid.builder()
                .searchGuid(entryGuid.toString())
                .includeVersions(true).includeArchived(true).build()));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_EXTERNAL_GUID)
    public ResponseEntity<List<NotesData>> deleteArchivedByExternalGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_EXTERNAL_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID externalGuid) {
        return ResponseEntity.ok(notesService.delete((SearchArchivedByExternalGuid.builder()
                .searchGuid(externalGuid.toString())
                .includeVersions(true).includeArchived(true)).build()));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_ARCHIVED_BY_ENTRY_GUID)
    public ResponseEntity<List<NotesData>> deleteArchivedByEntryGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_ENTRY_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID entryGuid) {
        return ResponseEntity.ok(notesService.delete(SearchArchivedByEntryGuid.builder()
                .searchGuid(entryGuid.toString())
                .includeVersions(true)
                .includeArchived(true)
                .build()));
    }

    @DeleteMapping(NotesConstants.API_ENDPOINT_NOTES_DELETE_BY_GUID)
    public ResponseEntity<NotesData> deleteByGuid(@PathVariable(NotesConstants.API_ENDPOINT_PATH_PARAMETER_GUID) /*@JsonDeserialize(using = UUIDDeserializer.class)*/ UUID guid) {
        return ResponseEntity.ok(notesService.delete(guid.toString()));
    }


}