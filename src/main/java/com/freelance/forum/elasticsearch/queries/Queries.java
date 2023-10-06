package com.freelance.forum.elasticsearch.queries;

public class Queries {
    
    public static String QUERY_BY_EXTERNAL_GUID = "{\n" +
            "  \"match_phrase\": {\n" +
            "    \"externalGuid\": \"%s\"\n" +
            "  }\n" +
            "}";

    public static String QUERY_BY_ENTRY_GUID = "{\n" +
            "  \"match_phrase\": {\n" +
            "    \"entryGuid\": \"%s\"\n" +
            "  }\n" +
            "}";
    
    public static String QUERY_RESPONSES_BY_THREAD_GUID = "{\n" +
            "  \"match_phrase\": {\n" +
            "    \"threadGuidParent\": \"%s\"\n" +
            "  }\n" +
            "}";
    public static String QUERY_TO_SEARCH_CONTENT = "{ \"match\": { \"content\": \"%s\" } }";
}
