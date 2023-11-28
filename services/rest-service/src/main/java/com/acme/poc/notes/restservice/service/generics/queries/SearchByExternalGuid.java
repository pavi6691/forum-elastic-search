package com.acme.poc.notes.restservice.service.generics.queries;

import com.acme.poc.notes.restservice.service.generics.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.NotesFields;
import lombok.experimental.SuperBuilder;


/**
 * Search query to fetch entries by external guid
 */
@SuperBuilder
public class SearchByExternalGuid extends AbstractQuery {


    @Override
    public String buildQuery() {
        return String.format(QUERY, NotesFields.EXTERNAL.getEsFieldName(), searchGuid, createdDateTime);
    }

}
