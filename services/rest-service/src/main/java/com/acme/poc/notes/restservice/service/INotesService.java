package com.acme.poc.notes.restservice.service;

import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByExternalGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByContent;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.service.generics.ICommonOperations;

import java.util.List;
import java.util.UUID;


public interface INotesService<E> {
    List<E> archive(IQuery iQuery);
    List<E> archive(UUID guid);
    List<E> searchByEntryGuid(SearchByEntryGuid iQuery);
    List<E> searchByContent(SearchByContent iQuery);
    List<E> searchArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery);
    List<E> searchArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery);
    List<E> deleteArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery);
    List<E> deleteArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery);

}
