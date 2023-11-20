package com.acme.poc.notes.restservice.persistence.elasticsearch.generics;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public interface INotesProcessor<E> {

    List<NotesData> fetchAndProcessEsResults(IQuery query);
    SearchHits<E> getEsResults(IQuery query);
    List<NotesData> process(IQuery query, Iterator<SearchHit<E>> esResults);

}
