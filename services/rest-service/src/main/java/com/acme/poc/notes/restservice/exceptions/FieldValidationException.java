package com.acme.poc.notes.restservice.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
public class FieldValidationException extends BaseRestException {

    @Getter private Map<String, String> fields = new HashMap<>();


    public FieldValidationException(String status, String message) {
        super(status, message);
    }

}
