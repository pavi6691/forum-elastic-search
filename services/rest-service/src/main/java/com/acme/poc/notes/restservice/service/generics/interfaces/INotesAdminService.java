package com.acme.poc.notes.restservice.service.generics.interfaces;

import com.acme.poc.notes.restservice.service.generics.queries.IQueryRequest;

import java.util.List;
import java.util.UUID;


public interface INotesAdminService<E> {

    List<E> getAll(String indexName);
    List<E> searchByExternalGuid(IQueryRequest iQueryRequest);
    List<E> deleteByExternalGuid(UUID externalGuid);
    List<E> deleteByEntryGuid(UUID entryGuid);
    List<E> deleteByThreadGuid(UUID threadGuid);
    E deleteByGuid(UUID guid);

}
