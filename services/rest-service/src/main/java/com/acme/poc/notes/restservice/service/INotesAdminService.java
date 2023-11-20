package com.acme.poc.notes.restservice.service;

import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByExternalGuid;

import java.util.List;
import java.util.UUID;


public interface INotesAdminService<E> {

    List<E> getAll(String indexName);
    List<E> searchByExternalGuid(SearchByExternalGuid query);
    List<E> deleteByExternalGuid(UUID externalGuid);
    List<E> deleteByEntryGuid(UUID entryGuid);
    List<E> deleteByThreadGuid(UUID threadGuid);
    E deleteByGuid(UUID guid);
    String createIndex(String indexName);
    
}
