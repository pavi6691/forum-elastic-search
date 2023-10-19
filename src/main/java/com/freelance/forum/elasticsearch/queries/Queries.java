package com.freelance.forum.elasticsearch.queries;

public class Queries {
    
    public static String QUERY_ALL_ENTRIES = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"match_phrase\": {\n" +
            "          \"%s\": \"%s\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"range\": {\n" +
            "          \"created\": {\n" +
            "            \"gte\": \"%s\"\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    // TODO let me check if filtered would be good here 
    public static String QUERY_ROOT_EXTERNAL_ENTRIES = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"bool\": {\n" +
            "          \"must_not\": [\n" +
            "            {\n" +
            "              \"exists\": {\n" +
            "                \"field\": \"threadGuidParent\"\n" +
            "              }\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"match_phrase\": {\n" +
            "          \"externalGuid\": \"%s\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    public static String QUERY_CONTENT_ROOT_EXTERNAL_ENTRIES = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"bool\": {\n" +
            "          \"must_not\": [\n" +
            "            {\n" +
            "              \"exists\": {\n" +
            "                \"field\": \"threadGuidParent\"\n" +
            "              }\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"match\": {\n" +
            "          \"content\": \"%s\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    public static String QUERY_CONTENT_ENTRIES = "{\n" +
            "  \"bool\": {\n" +
            "    \"must\": [\n" +
            "      {\n" +
            "        \"match_phrase\": {\n" +
            "          \"%s\": \"%s\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"match\": {\n" +
            "          \"content\": \"%s\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
}
