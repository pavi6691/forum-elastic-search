package com.freelance.forum.elasticsearch.queries;

public interface IQuery {
    String buildQuery();
    boolean getUpdateHistory();
    boolean getArchived();
    RequestType getRequestType();
}
