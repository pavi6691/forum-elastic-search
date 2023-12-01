package com.acme.poc.notes.restservice.generics.interfaces;

import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;

import java.util.List;
import java.util.UUID;


public interface INotesCrudOperations<E> {

    E create(E entity);
    E get(UUID guid);
    List<E> get(IQueryRequest iQueryRequest);
    List<E> delete(IQueryRequest iQueryRequest);
    E delete(UUID keyGuid);
    List<E> getAll(IQueryRequest iQueryRequest);
    E update(E entry);

}
