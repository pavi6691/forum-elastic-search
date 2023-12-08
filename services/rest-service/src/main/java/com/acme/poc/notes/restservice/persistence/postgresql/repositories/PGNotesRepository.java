package com.acme.poc.notes.restservice.persistence.postgresql.repositories;

import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Repository
public interface PGNotesRepository extends JpaRepository<PGNoteEntity, UUID> {
    
    List<PGNoteEntity> findAll();
    List<PGNoteEntity> findByOperationStatus(OperationStatus operationStatus);
    void deleteByOperationStatus(OperationStatus operationStatus);
    List<PGNoteEntity> findByEntryGuidOrderByCreatedAsc(UUID entryGuid);
    List<PGNoteEntity> findByEntryGuidAndCreatedGreaterThanEqualOrderByCreatedAsc(UUID entryGuid, Date created);
    List<PGNoteEntity> findByExternalGuidAndArchivedIsNotNullOrderByCreatedAsc(UUID externalGuid);
    List<PGNoteEntity> findByExternalGuidAndCreatedGreaterThanEqualOrderByCreatedAsc(UUID externalGuid, Date created);
    List<PGNoteEntity> findByContentContainingIgnoreCaseOrderByCreatedAsc(String content);

}
