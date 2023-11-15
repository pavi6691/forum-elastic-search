package com.acme.poc.notes.util;

import com.acme.poc.notes.persistence.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.models.NoteEntry;
import com.acme.poc.notes.persistence.postgresql.models.PGNoteEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.acme.poc.notes.data.PostgreSQLData.TEST_NOTE;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test cases for {@link DTOMapper}
 */
@Slf4j
public class DTOMapperTest {




    @Test
    @DisplayName("Map from DTO to PostgreSQL entity")
    void mapDTOtoPostgreSQLEntity() {
        PGNoteEntity entity = DTOMapper.INSTANCE.toEntity(TEST_NOTE);

        assertEquals(TEST_NOTE.guid(), entity.getGuid());
        assertEquals(TEST_NOTE.externalGuid(), entity.getExternalGuid());
        assertEquals(TEST_NOTE.threadGuid(), entity.getThreadGuid());
        assertEquals(TEST_NOTE.entryGuid(), entity.getEntryGuid());
        assertEquals(TEST_NOTE.entryGuidParent(), entity.getEntryGuidParent());
        assertEquals(TEST_NOTE.type(), entity.getType());
        assertEquals(TEST_NOTE.content(), entity.getContent());
        // TODO Add assert for testing customJson
        assertEquals(TEST_NOTE.created(), entity.getCreated());
        assertEquals(TEST_NOTE.archived(), entity.getArchived());
    }

    @Test
    @DisplayName("Map from PostgreSQL entity to DTO")
    void mapPostgreSQLEntityToDTO() {
        PGNoteEntity entity = DTOMapper.INSTANCE.toEntity(TEST_NOTE);
        NoteEntry dto = DTOMapper.INSTANCE.toDTO(entity);

        assertEquals(entity.getGuid(), dto.guid());
        assertEquals(entity.getExternalGuid(), dto.externalGuid());
        assertEquals(entity.getThreadGuid(), dto.threadGuid());
        assertEquals(entity.getEntryGuid(), dto.entryGuid());
        assertEquals(entity.getEntryGuidParent(), dto.entryGuidParent());
        assertEquals(entity.getType(), dto.type());
        assertEquals(entity.getContent(), dto.content());
        // TODO Add assert for testing customJson
        assertEquals(entity.getCreated(), dto.created());
        assertEquals(entity.getArchived(), dto.archived());
    }

    @Test
    @DisplayName("Map from DTO to Elasticsearch entity")
    void mapDTOtoElasticsearchEntity() {
        NotesData entity = DTOMapper.INSTANCE.toESEntity(TEST_NOTE);

        assertEquals(TEST_NOTE.guid(), entity.getGuid());
        assertEquals(TEST_NOTE.externalGuid(), entity.getExternalGuid());
        assertEquals(TEST_NOTE.threadGuid(), entity.getThreadGuid());
        assertEquals(TEST_NOTE.entryGuid(), entity.getEntryGuid());
        assertEquals(TEST_NOTE.entryGuidParent(), entity.getEntryGuidParent());
        assertEquals(TEST_NOTE.type(), entity.getType());
        assertEquals(TEST_NOTE.content(), entity.getContent());
        // TODO Add assert for testing customJson
        assertEquals(TEST_NOTE.created(), entity.getCreated());
        assertEquals(TEST_NOTE.archived(), entity.getArchived());
    }

    @Test
    @DisplayName("Map from Elasticsearch entity to DTO")
    void mapElasticsearchEntityToDTO() {
        NotesData entity = DTOMapper.INSTANCE.toESEntity(TEST_NOTE);
        NoteEntry dto = DTOMapper.INSTANCE.toDTO(entity);

        assertEquals(entity.getGuid(), dto.guid());
        assertEquals(entity.getExternalGuid(), dto.externalGuid());
        assertEquals(entity.getThreadGuid(), dto.threadGuid());
        assertEquals(entity.getEntryGuid(), dto.entryGuid());
        assertEquals(entity.getEntryGuidParent(), dto.entryGuidParent());
        assertEquals(entity.getType(), dto.type());
        assertEquals(entity.getContent(), dto.content());
        // TODO Add assert for testing customJson
        assertEquals(entity.getCreated(), dto.created());
        assertEquals(entity.getArchived(), dto.archived());
    }

}
