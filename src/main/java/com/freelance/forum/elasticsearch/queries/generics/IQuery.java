package com.freelance.forum.elasticsearch.queries.generics;

public interface IQuery {
    String buildQuery();
    boolean getUpdateHistory();
    default boolean getArchived() {
        return true;
    }
}
