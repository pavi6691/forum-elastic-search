package com.freelance.forum.elasticsearch.queries;

import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.enums.EsNotesFields;

/**
 * Search query to fetch entries by external guid
 */
public class SearchByExternalGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, EsNotesFields.EXTERNAL.getEsFieldName(),guid,createdDateTime);
    }
}
