package com.acme.poc.notes.models;

import java.util.List;


public enum NoteSortOrder {

    ASCENDING (List.of("asc", "ascending")) { @Override boolean isAscending() { return true; }},
    DESCENDING (List.of("desc", "descending")) { @Override boolean isDescending() { return true; }},
    UNSORTED (List.of("unsorted", "nosort", "nosorting", "notsorted")) { @Override boolean isUnsorted() { return true; }};


    private List<String> values;


    NoteSortOrder(List<String> values) {
        this.values = values;
    }


    boolean isAscending() { return false; }
    boolean isDescending() { return false; }
    boolean isUnsorted() { return false; }

}
