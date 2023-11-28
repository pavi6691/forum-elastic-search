package com.acme.poc.notes.restservice.service.generics.queries;

import com.acme.poc.notes.restservice.service.generics.queries.generics.AbstractQuery;
import com.acme.poc.notes.restservice.service.generics.queries.generics.enums.NotesFields;
import lombok.experimental.SuperBuilder;


/**
 * Search query to entries by entry guid.
 */
@SuperBuilder
public class SearchByEntryGuid extends AbstractQuery {


    @Override
    public String buildQuery() {
        return String.format(QUERY, NotesFields.ENTRY.getEsFieldName(), searchGuid, createdDateTime);
    }

}
