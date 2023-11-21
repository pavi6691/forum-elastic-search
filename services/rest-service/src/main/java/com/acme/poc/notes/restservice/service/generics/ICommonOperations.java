package com.acme.poc.notes.restservice.service.generics;

import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import java.util.List;
import java.util.UUID;


public interface ICommonOperations<E> {

    E getByGuid(UUID guid);
    E create(E entity);
    E updateByGuid(E entity);
    E updateByEntryGuid(E entity);
    List<E> delete(IQuery query);
    E delete(UUID keyGuid);

}
