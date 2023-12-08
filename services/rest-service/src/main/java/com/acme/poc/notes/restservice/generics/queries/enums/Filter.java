package com.acme.poc.notes.restservice.generics.queries.enums;

public enum Filter {
    INCLUDE_ARCHIVED("archived"),
    INCLUDE_ONLY_ARCHIVED("archived"),
    EXCLUDE_ARCHIVED("archived"),
    INCLUDE_VERSIONS("versions"),
    EXCLUDE_VERSIONS("versions"),
    GET_ONLY_RECENT("recent");
    private final String value;
    Filter(String value) {
        this.value = value;
    }
}