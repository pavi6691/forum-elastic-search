package com.freelance.forum.elasticsearch.queries;

import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.ESIndexNotesFields;

/**
 * Search query to fetch root entries by ESIndexNotesFields.THREAD.
 */
public class SearchByThreadGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, ESIndexNotesFields.THREAD.getEsFieldName(),guid,createdDateTime);
    }
}
