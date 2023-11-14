package com.acme.poc.notes;

import com.acme.poc.notes.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.persistence.postgresql.repositories.PGNotesRepository;
import com.acme.poc.notes.util.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static com.acme.poc.notes.data.PostgreSQLData.*;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgreSQLIntegrationTest {

    @Autowired
    private PGNotesRepository pgNotesRepository;

    private static final String POSTGRESQL_IMAGE = "postgres:15.5-alpine";

    @Container
    public static final PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer(POSTGRESQL_IMAGE)
            .withDatabaseName("acme")
            .withUsername("postgresql-username")
            .withPassword("postgresql-password");


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
//        Network network = Network.SHARED;

        postgresqlContainer
                .withNetworkAliases("postgresql");
        postgresqlContainer.start();

        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "postgresql-username");
        registry.add("spring.datasource.password", () -> "postgresql-password");
    }


    @Test
    @Order(1)
    @DisplayName("Save with all null values should throw exception")
    void savingAllNulls() {
        PGNoteEntity entity = DTOMapper.INSTANCE.toEntity(TEST_NOTE_ALL_NULLS);

        JpaSystemException jpaSystemException = assertThrows(JpaSystemException.class, () -> {
            pgNotesRepository.save(entity);
        });

        log.info("Exception: {}", jpaSystemException.getMessage());
        assertTrue(jpaSystemException.getMessage().startsWith("ids for this class must be manually assigned before calling save()"));
    }

    @Test
    @Order(2)
    @DisplayName("Save with correct values")
    void savingCorrect() {
        PGNoteEntity entity = DTOMapper.INSTANCE.toEntity(TEST_NOTE);

        PGNoteEntity savedEntity = pgNotesRepository.save(entity);

        assertEquals(TEST_NOTE.guid(), savedEntity.getGuid());
    }

    @Test
    @Order(3)
    @DisplayName("Find by guid")
    void findByGuid() {
        Optional<PGNoteEntity> byGuid = pgNotesRepository.findByGuid(TEST_GUID);
        assertEquals(TEST_NOTE.guid(), byGuid.get().getGuid());
    }

}
