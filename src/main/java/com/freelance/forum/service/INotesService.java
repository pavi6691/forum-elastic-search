package com.freelance.forum.service;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.SearchRequest;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;

import java.util.List;
import java.util.UUID;

public interface INotesService {
    NotesData saveNew(NotesData notesData);
    NotesData searchByGuid(UUID guid);
    NotesData update(NotesData notesData, ESIndexNotesFields esIndexNotesFields);
    List<NotesData> archive(String guid, ESIndexNotesFields esIndexNotesFields);
    List<NotesData> delete(String guid, ESIndexNotesFields esIndexNotesFields, String deleteEntries);
    String createIndex(String indexName);

    List<NotesData> search(SearchRequest searchRequest);
    
}
