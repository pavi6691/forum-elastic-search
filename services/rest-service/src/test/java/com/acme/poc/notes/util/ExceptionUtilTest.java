package com.acme.poc.notes.util;

import com.acme.poc.notes.core.enums.NotesAPIError;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test cases for {@link ExceptionUtil} methods.
 */
@Slf4j
public class ExceptionUtilTest {


    @Test
    @DisplayName("Test method throwRestError(...)")
    void testThrowRestError() {
        UUID TEST_EXTERNAL_GUID = UUID.randomUUID();
        UUID TEST_ENTRY_GUID = UUID.randomUUID();
        NotesAPIError TEST_NOTESAPIERROR = NotesAPIError.ERROR_ENTRY_ARCHIVED_CANNOT_ADD_THREAD;
        String TEST_EXPECTED = String.format(TEST_NOTESAPIERROR.errorMessage(), TEST_EXTERNAL_GUID, TEST_ENTRY_GUID);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> {
            ExceptionUtil.throwRestError(TEST_NOTESAPIERROR, TEST_EXTERNAL_GUID.toString(), TEST_ENTRY_GUID.toString());
        });

        log.debug("Expected: {}", TEST_EXPECTED);
        log.debug("Actual:   {}", responseStatusException.getMessage());

        assertTrue(responseStatusException.getMessage().contains(TEST_EXPECTED));
    }

}
