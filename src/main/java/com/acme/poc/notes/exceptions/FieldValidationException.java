package com.acme.poc.notes.exceptions;

import java.util.HashMap;
import java.util.Map;

public class FieldValidationException extends BaseRestException {
    Map<String,String> fields = new HashMap<>();
    public FieldValidationException(String status, String message, String fieldName, String value) {
        super(status, message);
        fields.put(fieldName,value);
    }

    public Map<String, String> getFields() {
        return fields;
    }
}
