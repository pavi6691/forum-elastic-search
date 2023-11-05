package com.acme.poc.notes.elasticsearch.generics;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;

import java.util.List;

public interface INotesOperations {
    List<NotesData> search(IQuery query);
}
