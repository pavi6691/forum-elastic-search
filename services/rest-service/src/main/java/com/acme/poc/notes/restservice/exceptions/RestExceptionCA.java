package com.acme.poc.notes.restservice.exceptions;

import com.acme.poc.notes.restservice.util.LogUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.RestStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.SocketTimeoutException;

@Slf4j
@RestControllerAdvice
public class RestExceptionCA {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RestStatusException.class)
    public BaseRestException handleRestException(RestStatusException exception) {
        log.error("{} {}", LogUtil.method(), exception.getMessage());
        return new BaseRestException(HttpStatus.resolve(exception.getStatus()).name(),exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseRestException handleArgTypeMisMatchException(MethodArgumentTypeMismatchException exception) {
        FieldValidationException ex = new FieldValidationException(HttpStatus.BAD_REQUEST.name(), 
                "Invalid request param value");
        ex.getFields().put(exception.getName(),exception.getValue().toString());
        log.error("{} {}", LogUtil.method(), ex.getMessage());
        return ex;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseRestException handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        FieldValidationException ex = new FieldValidationException(HttpStatus.BAD_REQUEST.name(),
                "Validation error on field in payload");
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();
            ex.getFields().put(fieldName,errorMessage);
        }
        log.error("{} {}", LogUtil.method(), ex.getMessage());
        return ex;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SocketTimeoutException.class)
    public BaseRestException EsTimeOutException(SocketTimeoutException exception) {
        log.error("{} {}", LogUtil.method(), exception.getMessage());
        return new BaseRestException(HttpStatus.BAD_REQUEST.name(), "Time out from elastic search. " + exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseRestException handlePayloadFieldMisMatchException(HttpMessageNotReadableException exception) {
        if (exception.getCause() instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) exception.getCause();
            FieldValidationException fieldEx = new FieldValidationException(HttpStatus.BAD_REQUEST.name(), "Invalid value in the payload");
            fieldEx.getFields().put(invalidFormatException.getPath().size() > 0 ?  invalidFormatException.getPath().get(0).getFieldName() : "", invalidFormatException.getValue().toString());
            log.error("{} {}", LogUtil.method(), fieldEx.getMessage());
            return fieldEx;
        }
        return null;
    }
}
