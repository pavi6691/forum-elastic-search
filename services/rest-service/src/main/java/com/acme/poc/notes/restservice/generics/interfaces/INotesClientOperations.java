package com.acme.poc.notes.restservice.generics.interfaces;

import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;

import java.util.List;
import java.util.UUID;


public interface INotesClientOperations<E> {

    List<E> archive(IQueryRequest iQueryRequest);
    List<E> archive(UUID guid);
    List<E> searchByEntryGuid(IQueryRequest iQueryRequest);
    List<E> searchByContent(IQueryRequest iQueryRequest);
    List<E> searchArchivedByExternalGuid(IQueryRequest iQueryRequest);
    List<E> searchArchivedByEntryGuid(IQueryRequest iQueryRequest);
    List<E> deleteArchivedByExternalGuid(IQueryRequest iQueryRequest);
    List<E> deleteArchivedByEntryGuid(IQueryRequest iQueryRequest);
}
