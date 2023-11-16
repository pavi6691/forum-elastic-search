package com.acme.poc.notes.restservice.service.generics;

import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.util.List;
import java.util.UUID;


public interface IESCommonOperations {

    List<NotesData> search(IQuery query);
    List<SearchHit<NotesData>> getAllEntries(IQuery query);

    List<NotesData> delete(IQuery query);

    NotesData delete(UUID keyGuid);

}
