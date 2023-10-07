package com.freelance.forum.elasticsearch.exceptions;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RestStatusException.class)
    public NoFoundException handleRestException(RestStatusException exception) {
        return new NoFoundException(exception.getStatus(),exception.getMessage());
    } 
}
