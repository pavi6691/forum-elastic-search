package com.acme.poc.notes.restservice.cron;
import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.persistence.elasticsearch.repositories.ESNotesRepository;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.util.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    private static final String UPDATE_DIRTY_TO_FALSE = "UPDATE note e SET e.operationStatus = :newValue WHERE e.guid IN :ids";
    
    @Autowired
    public DirtyNotesProcessorJob(PGNotesRepository pgNotesRepository, ESNotesRepository esNotesRepository) {
        this.pgNotesRepository = pgNotesRepository;
        this.esNotesRepository = esNotesRepository;
    }
    @Scheduled(fixedRate = NotesConstants.DIRTY_NOTES_PROCESSOR_JOB_SCHEDULE)
    @Transactional
    public void run() {
        processDirtyEntries(OperationStatus.UPSERT);
        processDirtyEntries(OperationStatus.DELETE);
    }
    private void processDirtyEntries(OperationStatus operation) {
        // Get dirty entries from postgresql
        List<PGNoteEntity> resultsFromPg = pgNotesRepository.findByOperationStatus(operation);
        if (!resultsFromPg.isEmpty()) {
            if (OperationStatus.DELETE == operation) {
                pgNotesRepository.deleteByOperationStatus(operation);
            }
            List<UUID> idsToUpdate = new ArrayList<>();
            try {
                // convert them to es note entities
                List<ESNoteEntity> esNoteEntities = resultsFromPg.stream()
                        .map(pgNoteEntity -> DTOMapper.INSTANCE.toESEntity(pgNoteEntity))
                        .collect(Collectors.toList());

                // TODO to To_Upgrade_7.x when upgraded to 7.x and above, uncomment below line, and no need to esNotesRepository.save(e); just iterate savedInEs instead of esNoteEntities
//                Iterable<ESNoteEntity> savedInEs = esNotesRepository.saveAll(esNoteEntities);
                if (esNoteEntities != null) {
                    esNoteEntities.forEach(e -> {
                        if(OperationStatus.UPSERT == operation) {
                            esNotesRepository.save(e);
                        } else if(OperationStatus.DELETE == operation) {
                            esNotesRepository.delete(e);
                        }
                        idsToUpdate.add(e.getGuid());
                    });
                }
                // update postgresql
                if (OperationStatus.UPSERT == operation) {
                    // Update entries in PostgreSQL with dirty flag to false
                    entityManager.createQuery(UPDATE_DIRTY_TO_FALSE)
                            .setParameter("newValue", OperationStatus.NONE)
                            .setParameter("ids", idsToUpdate)
                            .executeUpdate();
                }
            } catch (Exception e) {
                log.error("Exception in processing dirty entries", e);
            }
        }
    }
}
