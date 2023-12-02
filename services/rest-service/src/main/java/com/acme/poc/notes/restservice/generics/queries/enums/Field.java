package com.acme.poc.notes.restservice.generics.queries.enums;

import lombok.Getter;

@Getter
public enum Field {

    GUID ("guid"),
    EXTERNAL ("externalGuid"),
    ENTRY ("entryGuid"),
    THREAD ("threadGuid"),
    PARENT_ENTRY ("entryGuidParent"),
    CREATED ("created"),
    TYPE ("type"),
    CONTENT ("content"),
    ALL ("all");


    private String match ="";

    Field(String match) {
        this.match = match;
    }
}
