package com.acme.poc.notes.exceptions;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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

@RestControllerAdvice
public class RestExceptionCA {
    
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RestStatusException.class)
    public BaseRestException handleRestException(RestStatusException exception) {
        return new BaseRestException(HttpStatus.resolve(exception.getStatus()).name(),exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public BaseRestException handleArgTypeMisMatchException(MethodArgumentTypeMismatchException exception) {
        FieldValidationException ex = new FieldValidationException(HttpStatus.BAD_REQUEST.name(), 
                "Invalid request param value");
        ex.getFields().put(exception.getName(),exception.getValue().toString());
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
        return ex;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SocketTimeoutException.class)
    public BaseRestException EsTimeOutException(SocketTimeoutException exception) {
        return new BaseRestException(HttpStatus.BAD_REQUEST.name(), "Time out from elastic search. " + exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BaseRestException handlePayloadFieldMisMatchException(HttpMessageNotReadableException exception) {
        if(exception.getCause() instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) exception.getCause();
            FieldValidationException fieldEx = new FieldValidationException(HttpStatus.BAD_REQUEST.name(),
                    "Invalid value in the payload");
            fieldEx.getFields().put(invalidFormatException.getPath().size() > 0 ?  invalidFormatException.getPath().get(0).getFieldName() : "",
                    invalidFormatException.getValue().toString());
            return fieldEx;
        }
        return null;
    }
}
