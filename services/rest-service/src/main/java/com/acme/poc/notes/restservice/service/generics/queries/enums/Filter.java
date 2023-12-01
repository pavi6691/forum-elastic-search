package com.acme.poc.notes.restservice.service.generics.queries.enums;

public enum Filter {
    INCLUDE_ARCHIVED("archived"),
    INCLUDE_ONLY_ARCHIVED("archived"),
    EXCLUDE_ARCHIVED("archived"),
    INCLUDE_VERSIONS("versions"),
    EXCLUDE_VERSIONS("versions");
    private final String value;
    Filter(String value) {
        this.value = value;
    }
}
