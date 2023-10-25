package com.freelance.forum.elasticsearch.queries;

import org.elasticsearch.search.sort.SortOrder;

public interface IQuery {
    String buildQuery();
    boolean getUpdateHistory();
    boolean getArchived();
    RequestType getRequestType();
    SortOrder getSortOrder();
}
