package com.freelance.forum.elasticsearch.queries;

public enum ESIndexFields {
    GUID("guid"),
    EXTERNAL("externalGuid"),
    ENTRY("entryGuid"),
    THREAD("threadGuid"),
    PARENT_THREAD("threadGuidParent"),
    ARCHIVED("archived"),
    CREATED("created");

    private String indexFieldName ="";
    ESIndexFields(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    public String getEsFieldName() {
        return indexFieldName;
    }
}