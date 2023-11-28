package com.acme.poc.notes.restservice.service.generics.queries;

import com.acme.poc.notes.restservice.service.generics.queries.generics.AbstractQuery;
import lombok.experimental.SuperBuilder;


@SuperBuilder
public class SearchAll extends AbstractQuery {

    private static final String QUERY = """
            {
                "match_all": {}
            }
            """;


    @Override
    public String buildQuery() {
        return QUERY;
    }

}
