package com.acme.poc.notes.service.generics;

import com.acme.poc.notes.persistence.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.persistence.elasticsearch.queries.generics.IQuery;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.util.List;

public interface IESCommonOperations {
    List<NotesData> search(IQuery query);
    List<SearchHit<NotesData>> getAllEntries(IQuery query);

    List<NotesData> delete(IQuery query);

    NotesData delete(String keyGuid);
}
