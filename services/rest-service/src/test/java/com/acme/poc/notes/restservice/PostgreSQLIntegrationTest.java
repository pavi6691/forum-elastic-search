package com.acme.poc.notes.restservice;

import com.acme.poc.notes.restservice.base.BaseTest;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.SearchByExternalGuid;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import com.acme.poc.notes.restservice.service.ESNotesClientService;
import com.acme.poc.notes.restservice.service.PSQLNotesClientService;
import com.acme.poc.notes.restservice.util.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static com.acme.poc.notes.restservice.data.PostgreSQLData.*;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgreSQLIntegrationTest extends BaseTest {

    @Autowired
    private PSQLNotesClientService psqlNotesClientService;

    @Autowired
    private ESNotesClientService esNotesClientService;

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

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            psqlNotesClientService.update(entity);
        });

        log.info("Exception: {}", responseStatusException.getMessage());
        assertTrue(responseStatusException.getMessage().contains("ids for this class must be manually assigned before calling save()"));
    }

    @Test
    @Order(2)
    @DisplayName("Save with correct values")
    void savingCorrect() {
        PGNoteEntity entity = DTOMapper.INSTANCE.toEntity(TEST_NOTE);

        PGNoteEntity savedEntity = psqlNotesClientService.create(entity);

        assertEquals(TEST_NOTE.guid(), savedEntity.getGuid());
    }

    @Test
    @Order(3)
    @DisplayName("Find by guid")
    void findByGuid() {
        PGNoteEntity byGuid = psqlNotesClientService.getByGuid(TEST_GUID);
        assertEquals(TEST_NOTE.guid(), byGuid.getGuid());
    }

    @Test
    @Order(4)
    @DisplayName("Find by guid")
    void delete() {
        NotesData savedEntity = esNotesClientService.create(DTOMapper.INSTANCE.toESEntity(TEST_NOTE));
        IQuery query = SearchByExternalGuid.builder().searchGuid(savedEntity.getExternalGuid().toString()).build();
        assertEquals(savedEntity.getGuid(),psqlNotesClientService.getByGuid(savedEntity.getGuid()).getGuid());
        psqlNotesClientService.delete(query);
        assertEquals(null,psqlNotesClientService.getByGuid(savedEntity.getGuid()));
    }

}
