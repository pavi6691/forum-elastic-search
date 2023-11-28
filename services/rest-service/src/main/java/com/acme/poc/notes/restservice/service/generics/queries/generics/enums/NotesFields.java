package com.acme.poc.notes.restservice.service.generics.queries.generics.enums;


public enum NotesFields {

    GUID ("guid"),
    EXTERNAL ("externalGuid"),
    ENTRY ("entryGuid"),
    THREAD ("threadGuid"),
    PARENT_ENTRY ("entryGuidParent"),
    ARCHIVED ("archived"),
    CREATED ("created"),
    TYPE ("type"),
    CONTENT ("content");


    private String indexFieldName ="";


    NotesFields(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    public String getEsFieldName() {
        return indexFieldName;
    }

}
