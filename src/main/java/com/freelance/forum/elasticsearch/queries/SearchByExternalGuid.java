package com.freelance.forum.elasticsearch.queries;

import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.ESIndexNotesFields;

/**
 * Search query to fetch entries by external guid
 */
public class SearchByExternalGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, ESIndexNotesFields.EXTERNAL.getEsFieldName(),guid,createdDateTime);
    }
}
