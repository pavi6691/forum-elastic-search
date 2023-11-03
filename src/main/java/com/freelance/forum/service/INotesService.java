package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;

import java.util.List;
import java.util.UUID;

public interface INotesService {
    NotesData saveNew(NotesData notesData);
    NotesData searchByGuid(UUID guid);
    NotesData update(NotesData notesData);
    List<NotesData> archive(IQuery iQuery);
    List<NotesData> delete(IQuery iQuery);
    String createIndex(String indexName);
    NotesData delete(String keyGuid);
    List<NotesData> search(IQuery query);
    
}
