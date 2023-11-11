package com.acme.poc.notes.elasticsearch.queries;

import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.experimental.SuperBuilder;

/**
 * Search query to fetch root entries by EsNotesFields.PARENT_ENTRY guid
 */
@SuperBuilder
public class SearchByParentEntryGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, EsNotesFields.PARENT_ENTRY.getEsFieldName(),searchGuid,createdDateTime);
    }
}
