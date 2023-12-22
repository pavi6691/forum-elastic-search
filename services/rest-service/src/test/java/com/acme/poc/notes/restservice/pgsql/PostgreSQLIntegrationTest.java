package com.acme.poc.notes.restservice.pgsql;

import com.acme.poc.notes.restservice.base.AbstractIntegrationTest;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.restservice.service.pgsqlservice.PGSQLNotesService;
import com.acme.poc.notes.restservice.util.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.acme.poc.notes.restservice.base.data.PostgreSQLData.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgreSQLIntegrationTest extends AbstractIntegrationTest<PGNoteEntity> {

    @Autowired
    private PGNotesRepository pgNotesRepository;

    @Autowired
    public PostgreSQLIntegrationTest(PGSQLNotesService pgsqlNotesService, PGNoteEntity pgNoteEntity) {
        super(pgsqlNotesService, pgNoteEntity);
    }


    @Test
    @Order(1)
    @DisplayName("Save with all null values should throw exception")
    void savingAllNulls() {
        PGNoteEntity entity = DTOMapper.INSTANCE.toEntity(TEST_NOTE_ALL_NULLS);

        JpaSystemException jpaSystemException = assertThrows(JpaSystemException.class, () -> pgNotesRepository.save(entity));

        log.info("Exception: {}", jpaSystemException.getMessage());
        assertTrue(jpaSystemException.getMessage().startsWith("ids for this class must be manually assigned before calling save()"));
    }

    @Test
    @Order(2)
    @DisplayName("Save with correct values")
    void savingCorrect() {
        PGNoteEntity entity = DTOMapper.INSTANCE.toEntity(TEST_NOTE);

        PGNoteEntity savedEntity = notesService.create(entity);

        assertEquals(TEST_NOTE.guid(), savedEntity.getGuid());
    }

    @Test
    @Order(3)
    @DisplayName("Find by guid")
    void findByGuid() {
        PGNoteEntity byGuid = notesService.get(TEST_GUID);
        assertEquals(TEST_NOTE.guid(), byGuid.getGuid());
    }

    @Test
    @Order(4)
    @DisplayName("delete by guid")
    void delete() {
        assertEquals(TEST_NOTE.guid(), notesService.delete(TEST_NOTE.guid(), OperationStatus.DELETE).getGuid());
        assertEquals(null, notesService.get(TEST_NOTE.guid()));
    }

}
