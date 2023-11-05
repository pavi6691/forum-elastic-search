package com.acme.poc.notes.elasticsearch.queries.generics;

import org.elasticsearch.search.sort.SortOrder;

public interface IQuery {
    
    String getSearchId();
    String buildQuery();
    boolean getUpdateHistory();
    boolean getArchived();
    Object searchAfter();
    int getSize();
    SortOrder getSortOrder();
}
