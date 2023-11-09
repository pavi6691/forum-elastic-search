package com.acme.poc.notes.elasticsearch.queries;

import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import com.acme.poc.notes.elasticsearch.queries.generics.enums.EsNotesFields;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Search for content
 */
@SuperBuilder
public class SearchByContent extends AbstractQuery {
    private static final String QUERY = "{\n" +
            "    \"wildcard\": {\n" +
            "      \""+ EsNotesFields.CONTENT.getEsFieldName()+"\": {\n" +
            "        \"value\": \"*%s*\",\n" +
            "        \"boost\": 1.0,\n" +
            "        \"rewrite\": \"constant_score\"\n" +
            "      }\n" +
            "    }\n" +
            "  }";

    @Getter
    protected String contentToSearch;

    @Override
    public String buildQuery() {
        return String.format(QUERY,contentToSearch);
    }
}
