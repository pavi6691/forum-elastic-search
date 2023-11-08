package com.acme.poc.notes.elasticsearch.queries.generics;

import org.elasticsearch.search.sort.SortOrder;

public interface IQuery {
    String buildQuery();
    boolean getUpdateHistory();
    boolean getArchived();
    void searchAfter(Object sortValues);
    Object searchAfter();
    int getSize();
    SortOrder getSortOrder();
}
