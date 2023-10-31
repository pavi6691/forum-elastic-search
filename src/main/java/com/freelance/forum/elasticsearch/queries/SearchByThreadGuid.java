package com.freelance.forum.elasticsearch.queries;

import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.enums.EsNotesFields;

/**
 * Search query to fetch root entries by EsNotesFields.THREAD.
 */
public class SearchByThreadGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, EsNotesFields.THREAD.getEsFieldName(),guid,createdDateTime);
    }
}
