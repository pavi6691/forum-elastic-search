package com.acme.poc.notes.elasticsearch.queries;
import com.acme.poc.notes.elasticsearch.queries.generics.AbstractQuery;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SearchAll extends AbstractQuery {

    private static final String QUERY = "{\n" +
            "    \"match_all\": {}\n" +
            "  }";
    
    @Override
    public String buildQuery() {
        return QUERY;
    }
}
