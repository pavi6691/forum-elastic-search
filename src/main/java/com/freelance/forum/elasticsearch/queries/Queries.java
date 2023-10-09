package com.freelance.forum.elasticsearch.queries;

public class Queries {
    
    public static String QUERY_ALL_ENTRIES = "{\n" +
            "  \"match_phrase\": {\n" +
            "    \"%s\": \"%s\"\n" +
            "  }\n" +
            "}";
}
