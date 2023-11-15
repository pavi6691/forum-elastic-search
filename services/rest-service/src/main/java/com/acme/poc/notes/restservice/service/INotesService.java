package com.acme.poc.notes.restservice.service;

import com.acme.poc.notes.restservice.persistence.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchArchivedByExternalGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByContent;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByEntryGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;

import java.util.List;
import java.util.UUID;


public interface INotesService {

    NotesData create(NotesData notesData);
    NotesData getByGuid(UUID guid);
    NotesData updateByGuid(NotesData notesData);
    NotesData updateByEntryGuid(NotesData notesData);
    List<NotesData> archive(IQuery iQuery);
    List<NotesData> archive(UUID guid);
    List<NotesData> searchByEntryGuid(SearchByEntryGuid iQuery);
    List<NotesData> searchByContent(SearchByContent iQuery);
    List<NotesData> searchArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery);
    List<NotesData> searchArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery);
    List<NotesData> deleteArchivedByExternalGuid(SearchArchivedByExternalGuid iQuery);
    List<NotesData> deleteArchivedByEntryGuid(SearchArchivedByEntryGuid iQuery);

}
