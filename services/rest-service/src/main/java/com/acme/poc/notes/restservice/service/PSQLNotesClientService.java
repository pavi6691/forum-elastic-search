package com.acme.poc.notes.restservice.service;

import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.service.generics.abstracts.AbstractNotesCrudOperations;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PSQLNotesClientService extends AbstractNotesCrudOperations<PGNoteEntity> {


    public PSQLNotesClientService(PGNotesRepository pgNotesRepository) {
        super(pgNotesRepository);
    }


    @Override
    protected List<PGNoteEntity> execSearchQuery(IQuery query) {
        return null;
    }

}
