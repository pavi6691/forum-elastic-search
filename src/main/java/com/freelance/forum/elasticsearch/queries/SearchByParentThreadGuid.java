package com.freelance.forum.elasticsearch.queries;

import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.ESIndexNotesFields;

/**
 * Search query to fetch root entries by ESIndexNotesFields.PARENT_THREAD guid
 */
public class SearchByParentThreadGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, ESIndexNotesFields.PARENT_THREAD.getEsFieldName(),guid,createdDateTime);
    }
}
