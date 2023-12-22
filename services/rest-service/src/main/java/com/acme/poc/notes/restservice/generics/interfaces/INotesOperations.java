package com.acme.poc.notes.restservice.generics.interfaces;

import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;


public interface INotesOperations<E> {

    E create(E entity);
    E get(UUID guid);
    List<E> get(IQueryRequest iQueryRequest);
    List<E> delete(IQueryRequest iQueryRequest,OperationStatus operationStatus);
    E delete(UUID keyGuid, OperationStatus operationStatus);
    E update(E entry);
    E restore(UUID keyGuid);
    List<E> restore(IQueryRequest iQueryRequest);
    List<E> archive(IQueryRequest iQueryRequest);
    List<E> archive(UUID guid);
    List<E> get(String indexName);
    CrudRepository getCrudRepository();
}
