package com.acme.poc.notes.restservice.generics.interfaces;

import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;

import java.util.List;
import java.util.UUID;


public interface INotesClientOperations<E> {

    List<E> archive(IQueryRequest iQueryRequest);
    List<E> archive(UUID guid);
    List<E> get(IQueryRequest iQueryRequest);
    List<E> delete(IQueryRequest iQueryRequest);
}
