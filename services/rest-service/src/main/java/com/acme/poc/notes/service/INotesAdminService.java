package com.acme.poc.notes.service;

import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.SearchByExternalGuid;

import java.util.List;
import java.util.UUID;

public interface INotesAdminService {
    List<NotesData> getAll(String indexName);
    List<NotesData> searchByExternalGuid(SearchByExternalGuid query);
    List<NotesData> deleteByExternalGuid(UUID externalGuid);
    List<NotesData> deleteByEntryGuid(UUID entryGuid);
    List<NotesData> deleteByThreadGuid(UUID threadGuid);
    NotesData deleteByGuid(UUID guid);
    String createIndex(String indexName);
    
}
