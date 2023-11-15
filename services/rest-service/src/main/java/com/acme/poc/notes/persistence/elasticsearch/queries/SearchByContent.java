package com.acme.poc.notes.persistence.elasticsearch.queries;

import com.acme.poc.notes.persistence.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.persistence.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.Getter;
import lombok.experimental.SuperBuilder;


/**
 * Search for content
 */
@SuperBuilder
public class SearchByContent extends AbstractQuery {

    private static final String QUERY = """
            {
                "wildcard": {
                    "{FIELDNAME}": {
                        "value": "*%s*",
                        "boost": 1.0,
                        "rewrite": "constant_score"
                    }
                }
            }
            """
            .replace("{FIELDNAME}", EsNotesFields.CONTENT.getEsFieldName());

    @Getter
    protected String contentToSearch;


    @Override
    public String buildQuery() {
        return String.format(QUERY, contentToSearch);
    }

}
