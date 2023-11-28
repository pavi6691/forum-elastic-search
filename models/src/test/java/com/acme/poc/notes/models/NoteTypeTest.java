package com.acme.poc.notes.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.acme.poc.notes.models.NoteType.NOTE;
import static com.acme.poc.notes.models.NoteType.REMARK;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Test cases for {@link NoteType}
 */
public class NoteTypeTest {


    @Test
    @DisplayName("Test all enums")
    void testAllEnums() {
        Arrays.stream(NoteType.values()).forEach(noteType -> {
            String s = String.format("Checking: %s", noteType.name());
            assertEquals(noteType.name().toLowerCase(), noteType.getTypeValue());
            String result = switch (noteType) {
                case NOTE -> {
                    assertTrue(noteType.isNote());
                    assertFalse(noteType.isRemark());
                    assertTrue(noteType.isAllowAsRoot());
                    assertTrue(noteType.isAllowCustomJson());
                    assertEquals(NOTE.getAllowedChildTypes(), noteType.getAllowedChildTypes());
                    yield s;
                }
                case REMARK -> {
                    assertFalse(noteType.isNote());
                    assertTrue(noteType.isRemark());
                    assertFalse(noteType.isAllowAsRoot());
                    assertFalse(noteType.isAllowCustomJson());
                    assertEquals(REMARK.getAllowedChildTypes(), noteType.getAllowedChildTypes());
                    yield s;
                }
            };
        });
    }

}
