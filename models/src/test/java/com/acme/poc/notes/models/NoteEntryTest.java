package com.acme.poc.notes.models;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Test cases for {@link NoteEntry}
 */
@Slf4j
public class NoteEntryTest {

    private static final UUID TEST_GUID = UUID.randomUUID();


    @Test
    @DisplayName("Test NotesEntry builder")
    void testBuilder() {
        NoteEntry noteEntry = NoteEntry.builder()
                .guid(TEST_GUID)
                .build();

        assertEquals(TEST_GUID, noteEntry.guid());

        log.info("notesEntry:\n{}", noteEntry);
    }

}
