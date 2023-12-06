package com.acme.poc.notes.restservice.persistence.postgresql.repositories;

import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PGNotesRepository extends JpaRepository<PGNoteEntity, UUID> {
    
    List<PGNoteEntity> findAll();
    List<PGNoteEntity> findByIsDirty(boolean isDirty);
    List<PGNoteEntity> findByEntryGuidOrderByCreatedAsc(UUID entryGuid);
    List<PGNoteEntity> findByEntryGuidAndCreatedGreaterThanEqualOrderByCreatedAsc(UUID entryGuid, Date created);
    List<PGNoteEntity> findByExternalGuidAndArchivedIsNotNullOrderByCreatedAsc(UUID externalGuid);
    List<PGNoteEntity> findByExternalGuidAndCreatedGreaterThanEqualOrderByCreatedAsc(UUID externalGuid, Date created);
    List<PGNoteEntity> findByContentContainingOrderByCreatedAsc(String content);

}
