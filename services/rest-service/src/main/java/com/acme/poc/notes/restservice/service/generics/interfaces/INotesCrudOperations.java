package com.acme.poc.notes.restservice.service.generics.interfaces;

import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;

import java.util.List;
import java.util.UUID;

public interface INotesCrudOperations<E> {
    E getByGuid(UUID guid);
    List<E> search(IQuery query);
    List<E> delete(IQuery query);
    E delete(UUID keyGuid);
    List<E> getAllEntries(IQuery query);
    E updateByGuid(E entity);
    E updateByEntryGuid(E entity);
}
