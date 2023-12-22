package com.acme.poc.notes.restservice.persistence.postgresql.repositories;

import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Repository
public interface PGNotesRepository extends JpaRepository<PGNoteEntity, UUID> {
    
    List<PGNoteEntity> findAll();
    List<PGNoteEntity> findByOperationStatus(OperationStatus operationStatus);
    void deleteByOperationStatus(OperationStatus operationStatus);
    List<PGNoteEntity> findByEntryGuidAndOperationStatusInOrderByCreatedAsc(UUID entryGuid, List<OperationStatus> operationStatusList);
    List<PGNoteEntity> findByEntryGuidAndCreatedGreaterThanEqualAndOperationStatusInOrderByCreatedAsc(UUID entryGuid, Date created, List<OperationStatus> operationStatusList);
    List<PGNoteEntity> findByExternalGuidAndArchivedIsNotNullAndOperationStatusInOrderByCreatedAsc(UUID externalGuid, List<OperationStatus> operationStatusList);
    List<PGNoteEntity> findByExternalGuidAndCreatedGreaterThanEqualAndOperationStatusInOrderByCreatedAsc(UUID externalGuid, Date created, List<OperationStatus> operationStatusList);
    List<PGNoteEntity> findByContentContainingIgnoreCaseAndOperationStatusInOrderByCreatedAsc(String content, List<OperationStatus> operationStatusList);

}
