package com.acme.poc.notes.restservice.generics.interfaces;

import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;

import java.util.List;
import java.util.UUID;


public interface INotesAdminOperations<E> {

    List<E> get(String indexName);
    List<E> get(IQueryRequest iQueryRequest);
    List<E> delete(IQueryRequest externalGuid);
    E delete(UUID guid);
}
