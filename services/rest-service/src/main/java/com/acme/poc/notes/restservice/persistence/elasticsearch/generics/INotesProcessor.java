package com.acme.poc.notes.restservice.persistence.elasticsearch.generics;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public interface INotesProcessor<E> {

    List<E> getProcessed(IQuery query);
    List<E> getUnprocessed(IQuery query);
    List<E> process(IQuery query, Iterator<E> esResults);

}
