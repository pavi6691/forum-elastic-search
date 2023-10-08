package com.freelance.forum.elasticsearch.queries;

public class Queries {
    
    public static String QUERY_BY_GUID = "{\n" +
            "  \"match_phrase\": {\n" +
            "    \"%s\": \"%s\"\n" +
            "  }\n" +
            "}";
    public static String QUERY_ARCHIVED_ENTRIES = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"exists\": {\n" +
            "          \"field\": \"archived\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"match_phrase\": {\n" +
            "          \"%s\": \"%s\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
}
