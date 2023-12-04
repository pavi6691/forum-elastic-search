package com.acme.poc.notes.restservice.cron;
import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.util.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Process dirty entries stored in postgresql. these entries are stored when there are any errors in elasticsearch.
 * Entries are stored with flag isDirty true. Once stored them back them in elasticsearch and isDirty flag is changed to false.
 * So that same entry is not picked for storing in elasticsearch
 */
@Slf4j
@Component
public class DirtyNotesProcessorJob {
    PGNotesRepository pgNotesRepository;
    
    ESNotesRepository esNotesRepository;
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final String UPDATE_DIRTY_TO_FALSE = "UPDATE note e SET e.isDirty = :newValue WHERE e.guid IN :ids";
    
    @Autowired
    public DirtyNotesProcessorJob(PGNotesRepository pgNotesRepository, ESNotesRepository esNotesRepository) {
        this.pgNotesRepository = pgNotesRepository;
        this.esNotesRepository = esNotesRepository;
    }
    @Scheduled(fixedRate = NotesConstants.DIRTY_NOTES_PROCESSOR_JOB_SCHEDULE)
    @Transactional
    public void run() {
        // Get dirty entries from postgresql
        List<PGNoteEntity> resultsFromPg = pgNotesRepository.findByIsDirty(true);
        
        if(!resultsFromPg.isEmpty()) {
            // convert them to es note entities
            List<ESNoteEntity> esNoteEntities = resultsFromPg.stream()
                    .map(pgNoteEntity -> DTOMapper.INSTANCE.toESEntity(pgNoteEntity))
                    .collect(Collectors.toList());

            // store them in elasticsearch and make a list of guid(primary key) of stored entries in elasticsearch 
            List<UUID> idsToUpdate = new ArrayList<>();
            try {
                Iterable<ESNoteEntity> savedInEs = esNotesRepository.saveAll(esNoteEntities);
                if (savedInEs != null) {
                    savedInEs.forEach(e -> {
                        idsToUpdate.add(e.getGuid());
                    });
                }
                // update entries in postgresql with dirty flag to false
                entityManager.createQuery(UPDATE_DIRTY_TO_FALSE)
                        .setParameter("newValue", false)
                        .setParameter("ids", idsToUpdate)
                        .executeUpdate();
            } catch (Exception e) {
                log.error("Exception in processing dirty entries", e);
            }
        }
    }
}
