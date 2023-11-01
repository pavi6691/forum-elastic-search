package com.freelance.forum.exceptions;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class RestExceptionCA {
    
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RestStatusException.class)
    public NoteRestException handleRestException(RestStatusException exception) {
        return new NoteRestException(HttpStatus.resolve(exception.getStatus()).name(),exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public NoteRestException handleArgTypeMisMatchException(MethodArgumentTypeMismatchException exception) {
        return new NoteRestException(HttpStatus.BAD_REQUEST.name(),"Invalid field value, fieldName = " + exception.getName());
    }
}
