package com.acme.poc.notes.restservice.service.generics.queries.enums;

import lombok.Getter;

@Getter
public enum Match {

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

    Match(String match) {
        this.match = match;
    }
}
