package com.freelance.forum.elasticsearch.queries;

public interface IQuery {
    String buildQuery();
    boolean getUpdateHistory();
    default boolean getArchived() {
        return true;
    }
}
