package com.acme.poc.notes.elasticsearch.generics;

import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.Iterator;
import java.util.List;

public interface INotesOperations {
    List<NotesData> fetchAndProcessEsResults(IQuery query);
    SearchHits<NotesData> getEsResults(IQuery query);
    List<NotesData> process(IQuery query, Iterator<SearchHit<NotesData>> esResults);
}
