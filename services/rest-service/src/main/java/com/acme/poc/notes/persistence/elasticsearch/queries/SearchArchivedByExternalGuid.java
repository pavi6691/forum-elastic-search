package com.acme.poc.notes.persistence.elasticsearch.queries;

import com.acme.poc.notes.persistence.elasticsearch.generics.AbstractNotesProcessor;
import com.acme.poc.notes.persistence.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.persistence.elasticsearch.queries.generics.IQuery;
import com.acme.poc.notes.persistence.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.experimental.SuperBuilder;

import java.util.Iterator;


/**
 * Query to search archived entries by External Guid.
 * Filter is done in {@link AbstractNotesProcessor#process(IQuery, Iterator)}
 * For any entryGuid (not archived), if their threads are archived, then return them too
 */
@SuperBuilder
public class SearchArchivedByExternalGuid extends AbstractQuery {

    private static final String QUERY = """
            {
                "bool": {
                    "must": [
                        {
                            "match_phrase": {
                                "{FIELDNAME}": "%s"
                            }
                        }
                    ],
                    "filter": [
                        {
                            "exists": {
                                "field": "archived"
                            }
                        }
                    ]
                }
            }
            """
            .replace("{FIELDNAME}", EsNotesFields.EXTERNAL.getEsFieldName());


    @Override
    public boolean includeArchived() {
        return true;
    }
    
    @Override
    public String buildQuery() {
        return String.format(QUERY, searchGuid);
    }

}
