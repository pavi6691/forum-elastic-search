package com.acme.poc.notes.data;

public class ElasticSearchData {
    
    public static final String ENTRIES = "[\n" +
            "  {\n" +
            "    \"guid\": \"bff804e1-a503-4980-9115-0ed358b31088\",\n" +
            "    \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "    \"threadGuid\": \"16a45a51-647f-4dbf-a057-ec6c9681d46b\",\n" +
            "    \"entryGuid\": \"ba7a0762-935d-43f3-acb0-c33d86e7f350\",\n" +
            "    \"content\": \"Content-update-1\",\n" +
            "    \"created\": \"2023-10-19T11:33:39.000000+05:30\",\n" +
            "    \"threads\": [\n" +
            "      {\n" +
            "        \"guid\": \"5afecb9d-71a6-4fa6-bcaa-55d4e8695361\",\n" +
            "        \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "        \"threadGuid\": \"16a45a51-647f-4dbf-a057-ec6c9681d46b\",\n" +
            "        \"entryGuid\": \"06a418c3-7475-473e-9e9d-3e952d672d4c\",\n" +
            "        \"entryGuidParent\": \"ba7a0762-935d-43f3-acb0-c33d86e7f350\",\n" +
            "        \"content\": \"Content-thread-1\",\n" +
            "        \"created\": \"2023-10-19T08:15:50.000000+05:30\",\n" +
            "        \"threads\": [\n" +
            "          {\n" +
            "            \"guid\": \"2c4fbd54-3681-4bdd-ae26-c5e228e96a68\",\n" +
            "            \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "            \"threadGuid\": \"16a45a51-647f-4dbf-a057-ec6c9681d46b\",\n" +
            "            \"entryGuid\": \"16b8d331-92ab-424b-b69a-3181f6d80f5a\",\n" +
            "            \"entryGuidParent\": \"06a418c3-7475-473e-9e9d-3e952d672d4c\",\n" +
            "            \"content\": \"Content-thread-2\",\n" +
            "            \"created\": \"2023-10-19T08:16:15.000000+05:30\",\n" +
            "            \"threads\": [\n" +
            "              {\n" +
            "                \"guid\": \"babec42a-49e9-409f-b635-17fe1232b509\",\n" +
            "                \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "                \"threadGuid\": \"16a45a51-647f-4dbf-a057-ec6c9681d46b\",\n" +
            "                \"entryGuid\": \"7f20d0eb-3907-4647-9584-3d7814cd3a55\",\n" +
            "                \"entryGuidParent\": \"16b8d331-92ab-424b-b69a-3181f6d80f5a\",\n" +
            "                \"content\": \"Content-thread-3-update-1\",\n" +
            "                \"created\": \"2023-10-19T08:28:43.000000+05:30\",\n" +
            "                \"archived\": \"2023-10-20T10:20:54.000000+05:30\",\n" +
            "                \"threads\": [\n" +
            "                  {\n" +
            "                    \"guid\": \"350e4c01-4445-4304-b816-25ad62806390\",\n" +
            "                    \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "                    \"threadGuid\": \"16a45a51-647f-4dbf-a057-ec6c9681d46b\",\n" +
            "                    \"entryGuid\": \"68b787e4-f75e-4775-ba76-fc53928f72df\",\n" +
            "                    \"entryGuidParent\": \"7f20d0eb-3907-4647-9584-3d7814cd3a55\",\n" +
            "                    \"content\": \"Content-thread-4\",\n" +
            "                    \"created\": \"2023-10-19T08:27:41.000000+05:30\",\n" +
            "                    \"archived\": \"2023-10-20T10:20:54.000000+05:30\"\n" +
            "                  },\n" +
            "                  {\n" +
            "                    \"guid\": \"b2189a45-fddc-4b15-adc1-5182491439ce\",\n" +
            "                    \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "                    \"threadGuid\": \"16a45a51-647f-4dbf-a057-ec6c9681d46b\",\n" +
            "                    \"entryGuid\": \"dcd0df45-8fe7-4b1c-ad30-f64c5a2b6e74\",\n" +
            "                    \"entryGuidParent\": \"7f20d0eb-3907-4647-9584-3d7814cd3a55\",\n" +
            "                    \"content\": \"Content-thread-5\",\n" +
            "                    \"created\": \"2023-10-20T05:44:55.000000+05:30\",\n" +
            "                    \"archived\": \"2023-10-20T10:20:52.000000+05:30\"\n" +
            "                  }\n" +
            "                ],\n" +
            "                \"history\": [\n" +
            "                  {\n" +
            "                    \"guid\": \"1c516c5c-4989-4371-b7b0-920a8c672c71\",\n" +
            "                    \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "                    \"threadGuid\": \"16a45a51-647f-4dbf-a057-ec6c9681d46b\",\n" +
            "                    \"entryGuid\": \"7f20d0eb-3907-4647-9584-3d7814cd3a55\",\n" +
            "                    \"entryGuidParent\": \"16b8d331-92ab-424b-b69a-3181f6d80f5a\",\n" +
            "                    \"content\": \"Content-thread-3\",\n" +
            "                    \"created\": \"2023-10-19T08:16:37.000000+05:30\",\n" +
            "                    \"archived\": \"2023-10-20T10:20:54.000000+05:30\"\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ],\n" +
            "    \"history\": [\n" +
            "      {\n" +
            "        \"guid\": \"eedee224-e35e-4135-aabd-e89c9e5b7eb3\",\n" +
            "        \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "        \"threadGuid\": \"16a45a51-647f-4dbf-a057-ec6c9681d46b\",\n" +
            "        \"entryGuid\": \"ba7a0762-935d-43f3-acb0-c33d86e7f350\",\n" +
            "        \"content\": \"Content\",\n" +
            "        \"created\": \"2023-10-19T08:15:25.000000+05:30\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"guid\": \"01dbfc43-63d6-4b6b-b608-ae8958c9b3a2\",\n" +
            "    \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "    \"threadGuid\": \"a7f285c5-b99e-4200-a042-87c1afd84922\",\n" +
            "    \"entryGuid\": \"72a2ae64-034d-4979-9a47-8abcf8d0317b\",\n" +
            "    \"content\": \"Content-1\",\n" +
            "    \"created\": \"2023-10-20T10:34:12.000000+05:30\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"guid\": \"7f3fc0e1-154f-4ca4-b58c-6c96391553ba\",\n" +
            "    \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "    \"threadGuid\": \"9db017b7-0e1b-4194-a79e-96239232e2be\",\n" +
            "    \"entryGuid\": \"260624b3-0beb-46cb-a6cf-90a83162df71\",\n" +
            "    \"content\": \"Content-2\",\n" +
            "    \"created\": \"2023-10-20T10:34:18.000000+05:30\",\n" +
            "    \"threads\": [\n" +
            "      {\n" +
            "        \"guid\": \"4033954e-54b9-49f4-bbe4-357345d1e650\",\n" +
            "        \"externalGuid\": \"10a14259-ca84-4c7d-8d46-7ad398000002\",\n" +
            "        \"threadGuid\": \"9db017b7-0e1b-4194-a79e-96239232e2be\",\n" +
            "        \"entryGuid\": \"a854b74c-305b-4797-9d80-322990ee38f7\",\n" +
            "        \"entryGuidParent\": \"260624b3-0beb-46cb-a6cf-90a83162df71\",\n" +
            "        \"content\": \"Content-2-thread-1\",\n" +
            "        \"created\": \"2023-10-20T10:34:29.000000+05:30\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";
}
