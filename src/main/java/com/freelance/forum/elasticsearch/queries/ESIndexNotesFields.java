package com.freelance.forum.elasticsearch.queries;

public enum ESIndexNotesFields {
    GUID("guid"),
    EXTERNAL("externalGuid"),
    ENTRY("entryGuid"),
    THREAD("threadGuid"),
    PARENT_THREAD("threadGuidParent"),
    ARCHIVED("archived"),
    CREATED("created");

    private String indexFieldName ="";
    ESIndexNotesFields(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    public String getEsFieldName() {
        return indexFieldName;
    }
}