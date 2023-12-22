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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** JOB that covers below uses cases for ACID compliant
 * 1. Copy UPSERT entries from postgresql to elasticsearch. entries created/updated are marked as UPSERT
 * 2. Delete entries with operation status MARK_FOR_DELETE. entries are deleted from both postgresql and elasticsearch
 * 3. Delete entries with operation status SOFT_DELETE from only elasticsearch. So that soft deleted entries are not included in content search.
 *    And SOFT deleted entries are still present in postgresql and can be reverted by changing setting operation status to UPSERT
 */
@Slf4j
@Component
public class NotesProcessorJob {
    PGNotesRepository pgNotesRepository;
    
    ESNotesRepository esNotesRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Value("${notes.processor.job.enabled:true}")
    private boolean notesProcessorJobEnabled;
    
    private static final String UPDATE_POSTGRESQL = "UPDATE note e SET e.operationStatus = :newValue WHERE e.guid IN :ids";
    
    @Autowired
    public NotesProcessorJob(PGNotesRepository pgNotesRepository, ESNotesRepository esNotesRepository) {
        this.pgNotesRepository = pgNotesRepository;
        this.esNotesRepository = esNotesRepository;
    }
    @Scheduled(fixedRate = NotesConstants.NOTES_PROCESSOR_JOB_SCHEDULE)
    public void run() {
        if(notesProcessorJobEnabled) {
            execute();
        }
    }

    /**
     * To execute it manually when required. Ex- in integration test cases
     */
    @Transactional
    public void execute() {
        processEntries(OperationStatus.UPSERT);
        processEntries(OperationStatus.MARK_FOR_SOFT_DELETE);
        processEntries(OperationStatus.MARK_FOR_DELETE);
    }
    
    private void processEntries(OperationStatus operation) {
        // Get entries from postgresql
        List<PGNoteEntity> resultsFromPg = pgNotesRepository.findByOperationStatus(operation);
        if (!resultsFromPg.isEmpty()) {
            if (OperationStatus.MARK_FOR_DELETE == operation) {
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
                        } else if(OperationStatus.MARK_FOR_DELETE == operation || OperationStatus.MARK_FOR_SOFT_DELETE == operation) {
                            esNotesRepository.delete(e);
                        }
                        idsToUpdate.add(e.getGuid());
                    });
                }
                // update postgresql
                if (OperationStatus.UPSERT == operation) {
                    // Update upsert entries in PostgreSQL
                    entityManager.createQuery(UPDATE_POSTGRESQL)
                            .setParameter("newValue", OperationStatus.ACTIVE)
                            .setParameter("ids", idsToUpdate)
                            .executeUpdate();
                } else  if (OperationStatus.MARK_FOR_SOFT_DELETE == operation) {
                    entityManager.createQuery(UPDATE_POSTGRESQL)
                            .setParameter("newValue", OperationStatus.SOFT_DELETED)
                            .setParameter("ids", idsToUpdate)
                            .executeUpdate();
                }
            } catch (Exception e) {
                log.error("Exception in processing notes entries", e);
            }
        }
    }
}
