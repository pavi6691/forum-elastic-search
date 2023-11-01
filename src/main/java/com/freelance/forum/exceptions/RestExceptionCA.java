package com.freelance.forum.exceptions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
        return new NoteRestException(HttpStatus.BAD_REQUEST.name(),"Invalid request param value. fieldName = " + exception.getName());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public NoteRestException handlePayloadFieldMisMatchException(HttpMessageNotReadableException exception) {
        return new NoteRestException(HttpStatus.BAD_REQUEST.name(),"Invalid value in the payload. fieldName = " + 
                StringUtils.substringBetween(exception.getMessage(), "[\"", "\"]"));
    }
}
