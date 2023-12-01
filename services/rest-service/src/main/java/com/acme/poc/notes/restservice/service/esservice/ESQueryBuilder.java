package com.acme.poc.notes.restservice.service.esservice;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Match;
public class ESQueryBuilder {

    private static final String GET_ALL = """
            {
                "match_all": {}
            }
            """;

    protected static final String GET_BY_OTHER = """
            {
                  "bool": {
                        "must": [
                            {
                                "match_phrase": {
                                    "%s": "%s"
                                }
                            },
                            {
                                "range": {
                                    "{FIELDNAME}": {
                                        "gte": "%s"
                                    }
                                }
                            }
                        ]
                  }
            }
            """
            .replace("{FIELDNAME}", Match.CREATED.getMatch());

    private static final String GET_ARCHIVED_BY_ENTRY_GUID = """
            {
                "bool": {
                    "must": [
                        {
                            "match_phrase": {
                                "{FIELDNAME}": "%s"
                            }
                        }
                    ]
                }
            }
            """
            .replace("{FIELDNAME}", Match.ENTRY.getMatch());

    private static final String GET_ARCHIVED_BY_EXTERNAL_GUID = """
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
            .replace("{FIELDNAME}", Match.EXTERNAL.getMatch());

    private static final String SEARCH_CONTENT = """
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
            .replace("{FIELDNAME}", Match.CONTENT.getMatch());
    
    public static String build(IQueryRequest queryRequest) {
        switch (queryRequest.getSearchField()) {
            case ALL:
                return GET_ALL;
            case ENTRY:
                if(queryRequest.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED)) {
                    return String.format(GET_ARCHIVED_BY_ENTRY_GUID, queryRequest.getSearchData());
                } else {
                    return String.format(GET_BY_OTHER, queryRequest.getSearchField().getMatch(), queryRequest.getSearchData(), 
                            queryRequest.getCreatedDateTime());
                }
            case EXTERNAL:
                if(queryRequest.getFilters().contains(Filter.INCLUDE_ONLY_ARCHIVED)) {
                    return String.format(GET_ARCHIVED_BY_EXTERNAL_GUID, queryRequest.getSearchData());
                } else {
                    return String.format(GET_BY_OTHER, queryRequest.getSearchField().getMatch(), queryRequest.getSearchData(), 
                            queryRequest.getCreatedDateTime());
                }
            case CONTENT:
                return String.format(SEARCH_CONTENT, queryRequest.getSearchData());
            default:
                return String.format(GET_BY_OTHER, queryRequest.getSearchField().getMatch(), queryRequest.getSearchData(), 
                        queryRequest.getCreatedDateTime());
        }
    }
}
