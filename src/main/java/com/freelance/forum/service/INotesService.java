package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.IQuery;

import java.util.List;
import java.util.UUID;

public interface INotesService {
    NotesData saveNew(NotesData notesData);
    NotesData searchByGuid(UUID guid);
    NotesData update(NotesData notesData);
    List<NotesData> archive(IQuery iQuery);
    List<NotesData> delete(IQuery iQuery, String deleteEntries);
    String createIndex(String indexName);

    List<NotesData> search(IQuery query);
    
}
