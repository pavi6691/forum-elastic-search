package com.acme.poc.notes.util;

import com.acme.poc.notes.core.enums.NotesAPIError;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


public class ExceptionUtil {


    public static void throwRestError(NotesAPIError notesAPIError, Object ...args) {
        throw new ResponseStatusException(HttpStatus.valueOf(notesAPIError.httpStatusCode()), String.format(notesAPIError.errorMessage(), args));
    }

}
