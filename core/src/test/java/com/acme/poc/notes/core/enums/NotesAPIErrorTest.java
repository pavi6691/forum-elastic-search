package com.acme.poc.notes.core.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Tests for {@link NotesAPIError} enums
 */
public class NotesAPIErrorTest {

    private final static int[] VALID_HTTP_STATUS_CODES = { 400, 404, 408, 409, 500 };


    @Test
    @DisplayName("Test all enums")
    void testAllEnums() {
        Arrays.stream(NotesAPIError.values()).forEach(notesAPIError -> {
            String result = switch (notesAPIError) {
                case ERROR_CLIENT_REQUEST,
                     ERROR_MISSING_GUID,
                     ERROR_MISSING_ENTRY_GUID,
                     ERROR_MISSING_THREAD_GUID,
                     ERROR_MISSING_THREAD_PARENT_GUID,
                     ERROR_MISSING_CREATED,
                     ERROR_PARSING_TIMESTAMP,
                     ERROR_INCORRECT_SEARCH_AFTER,
                     ERROR_NOT_EXISTS_GUID,
                     ERROR_NOT_EXISTS_ENTRY_GUID,
                     ERROR_NEW_RESPONSE_NO_THREAD_GUID,
                     ERROR_ENTRY_ARCHIVED_NO_UPDATE,
                     ERROR_ENTRY_ARCHIVED_CANNOT_ADD_THREAD,
                     ERROR_ENTRY_HAS_BEEN_MODIFIED,
                     ERROR_TIMEOUT_DELETE,
                     ERROR_NOT_FOUND,
                     ERROR_SERVER,
                        ERROR_ON_DB_OPERATION -> {
                         String s = String.format("Checking: %s (%s, %s, \"%s\")", notesAPIError.name(), notesAPIError.httpStatusCode(), notesAPIError.errorCode(), notesAPIError.errorMessage());
                         System.out.println(s);
                         assertTrue(Arrays.stream(VALID_HTTP_STATUS_CODES).anyMatch(i -> i == notesAPIError.httpStatusCode()), () -> String.format("httpStatusCode: %s is not between valid values", notesAPIError.httpStatusCode()));
                         assertTrue(notesAPIError.errorCode() >= 4000 && notesAPIError.errorCode() < 6000, () -> String.format("errorCode: %s is not between valid values", notesAPIError.errorCode()));
                         assertNotNull(notesAPIError.errorMessage(), () -> "Error message can not be null");
                         yield s;
                     }
            };
        });
    }

    @Test
    @DisplayName("Check that no two errors has the same errorCode")
    void testUniqueErrorCodes() {
        List<Integer> list = new ArrayList<>();
        Arrays.stream(NotesAPIError.values()).forEach(notesAPIError -> {
            int errorCode = notesAPIError.errorCode();
            assertFalse(list.contains(errorCode), () -> String.format("Seems that errorCodes are not unique: %s", errorCode));
            list.add(errorCode);
        });
        System.out.println("Found the following errorCodes (all unique): " + list.stream().sorted().map(Object::toString).collect(Collectors.joining(", ")));
    }

}
