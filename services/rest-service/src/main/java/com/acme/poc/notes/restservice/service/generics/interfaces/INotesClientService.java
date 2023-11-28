package com.acme.poc.notes.restservice.service.generics.interfaces;

import com.acme.poc.notes.restservice.service.generics.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.restservice.service.generics.queries.SearchArchivedByExternalGuid;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByContent;
import com.acme.poc.notes.restservice.service.generics.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.service.generics.queries.generics.IQuery;

import java.util.List;
import java.util.UUID;


public interface INotesClientService<E> {

    List<E> archive(IQuery iQuery);
    List<E> archive(UUID guid);
    List<E> searchByEntryGuid(SearchByEntryGuid iQuery);
    List<E> searchByContent(SearchByContent iQuery);
    List<E> searchArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery);
    List<E> searchArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery);
    List<E> deleteArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery);
    List<E> deleteArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery);

}
