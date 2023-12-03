package com.acme.poc.notes.restservice.persistence.postgresql.repositories;

import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PGNotesRepository extends JpaRepository<PGNoteEntity, UUID> {

//    Optional<Page<PGNoteEntity>> findAll(Pageable pageable);
    List<PGNoteEntity> findAll();
    Optional<List<PGNoteEntity>> findByExternalGuid(UUID externalGuid);
    Optional<PGNoteEntity> findByThreadGuid(UUID externalGuid/*, Pageable pageable*/);
    Optional<PGNoteEntity> findByGuid(UUID guid);
    List<PGNoteEntity> findByIsDirty(boolean isDirty);

}
