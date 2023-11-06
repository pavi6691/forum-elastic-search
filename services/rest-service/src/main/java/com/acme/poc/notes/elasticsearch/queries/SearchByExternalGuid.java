package com.acme.poc.notes.elasticsearch.queries;

import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

/**
 * Search query to fetch entries by external guid
 */
@SuperBuilder
public class SearchByExternalGuid extends AbstractQuery {
    @Override
    public String buildQuery() {
        return String.format(QUERY, EsNotesFields.EXTERNAL.getEsFieldName(),searchGuid,createdDateTime);
    }
}
