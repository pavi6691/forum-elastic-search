package com.acme.poc.notes.persistence.elasticsearch.queries.generics.enums;

public enum EsNotesFields {
    GUID("guid"),
    EXTERNAL("externalGuid"),
    ENTRY("entryGuid"),
    THREAD("threadGuid"),
    PARENT_ENTRY("entryGuidParent"),
    ARCHIVED("archived"),
    CREATED("created"),
    TYPE("type"),
    CONTENT("content");

    private String indexFieldName ="";
    EsNotesFields(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    public String getEsFieldName() {
        return indexFieldName;
    }
}