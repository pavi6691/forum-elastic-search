package com.acme.poc.notes.restservice.service.generics.interfaces;

import com.acme.poc.notes.restservice.service.generics.queries.generics.IQuery;

import java.util.List;
import java.util.UUID;


public interface INotesCrudOperations<E> {

    E create(E entity);
    E get(UUID guid);
    List<E> get(IQuery query);
    List<E> delete(IQuery query);
    E delete(UUID keyGuid);
    List<E> getAll(IQuery query);
    E update(E entry);
}
