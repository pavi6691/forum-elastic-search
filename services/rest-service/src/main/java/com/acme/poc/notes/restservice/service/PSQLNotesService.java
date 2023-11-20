package com.acme.poc.notes.restservice.service;

import com.acme.poc.notes.restservice.persistence.elasticsearch.generics.INotesProcessor;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.service.generics.AbstractService;
import org.springframework.stereotype.Service;
@Service
public class PSQLNotesService extends AbstractService<PGNoteEntity> {
    public PSQLNotesService(INotesProcessor iNotesProcessor,
                            PGNotesRepository pgNotesRepository) {
        super(iNotesProcessor, pgNotesRepository);
    }
}
