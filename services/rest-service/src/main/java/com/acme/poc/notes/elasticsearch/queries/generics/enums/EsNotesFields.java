package com.acme.poc.notes.elasticsearch.queries.generics.enums;

public enum EsNotesFields {
    GUID("guid"),
    EXTERNAL("externalGuid"),
    ENTRY("entryGuid"),
    THREAD("threadGuid"),
    PARENT_THREAD("threadGuidParent"),
    ARCHIVED("archived"),
    CREATED("created"),
    CONTENT("content");

    private String indexFieldName ="";
    EsNotesFields(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    public String getEsFieldName() {
        return indexFieldName;
    }
}