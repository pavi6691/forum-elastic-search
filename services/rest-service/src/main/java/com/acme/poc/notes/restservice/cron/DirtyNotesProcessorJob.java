package com.acme.poc.notes.restservice.cron;
import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.service.esservice.ESNotesService;
import com.acme.poc.notes.restservice.util.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Process dirty entries stored in postgresql. these entries are stored when there are any errors in elasticsearch.
 * Entries are stored with flag isDirty true. Once stored them back them in elasticsearch and isDirty flag is changed to false.
 * So that same entry is not picked for storing in elasticsearch
 */
@Slf4j
@Component
public class DirtyNotesProcessorJob {
    PGNotesRepository pgNotesRepository;
    ESNotesService esNotesService;
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    public DirtyNotesProcessorJob(PGNotesRepository pgNotesRepository, ESNotesService esNotesService) {
        this.pgNotesRepository = pgNotesRepository;
        this.esNotesService = esNotesService;
    }
    @Scheduled(fixedRate = NotesConstants.DIRTY_NOTES_PROCESSOR_JOB_SCHEDULE)
    @Transactional
    public void run() {
        List<PGNoteEntity> results = pgNotesRepository.findByIsDirty(true);
        results.stream().forEach(pgNoteEntity -> {
            ESNoteEntity esNoteEntity  = DTOMapper.INSTANCE.toESEntity(pgNoteEntity);
            ESNoteEntity esNoteEntityCreated = esNotesService.create(esNoteEntity);
            if(esNoteEntityCreated != null) {
                try {
                    pgNoteEntity.setIsDirty(false);
                    entityManager.merge(pgNoteEntity);
                } catch (Exception e) {
                    log.error("Error processing dirty entries from postgresql, guid = {}", pgNoteEntity.getGuid());
                }
            }
        });
    }
}
