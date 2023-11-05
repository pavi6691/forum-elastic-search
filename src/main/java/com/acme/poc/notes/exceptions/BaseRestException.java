package com.acme.poc.notes.exceptions;

public class BaseRestException {
    
    private String status;
    private String message;
    public BaseRestException(String status, String msg) {
        this.message = msg;
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
