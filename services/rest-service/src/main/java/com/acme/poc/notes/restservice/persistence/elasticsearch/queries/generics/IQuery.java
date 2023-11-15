package com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics;

import org.elasticsearch.search.sort.SortOrder;

public interface IQuery {
    String buildQuery();
    boolean includeVersions();
    boolean includeArchived();
    void searchAfter(Object sortValues);
    Object searchAfter();
    int getSize();
    SortOrder getSortOrder();
}
