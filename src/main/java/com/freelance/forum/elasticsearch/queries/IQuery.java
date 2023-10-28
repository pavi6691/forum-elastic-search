package com.freelance.forum.elasticsearch.queries;

import org.elasticsearch.search.sort.SortOrder;

public interface IQuery {
    String buildQuery();
    boolean getUpdateHistory();
    default boolean getArchived() {
        return true;
    }
    default SortOrder getSortOrder() {return SortOrder.DESC;}
}
