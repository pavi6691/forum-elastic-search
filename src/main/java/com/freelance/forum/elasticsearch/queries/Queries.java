package com.freelance.forum.elasticsearch.queries;

public class Queries {
    
    public static class SearchByExternalGuid implements IQuery {
        private static final String QUERY_ROOT_EXTERNAL_ENTRIES = "{\n" +
                "  \"bool\": {\n" +
                "    \"must\": [\n" +
                "      {\n" +
                "        \"bool\": {\n" +
                "          \"must_not\": [\n" +
                "            {\n" +
                "              \"exists\": {\n" +
                "                \"field\": \""+ESIndexNotesFields.PARENT_THREAD.getEsFieldName()+"\"\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"match_phrase\": {\n" +
                "          \""+ESIndexNotesFields.EXTERNAL.getEsFieldName()+"\": \"%s\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        
        private String externalGuid;
        private boolean getUpdateHistory;
        private boolean getArchived;

        public SearchByExternalGuid setGetUpdateHistory(boolean getUpdateHistory) {
            this.getUpdateHistory = getUpdateHistory;
            return this;
        }

        public SearchByExternalGuid setGetArchived(boolean getArchived) {
            this.getArchived = getArchived;
            return this;
        }

        @Override
        public boolean getUpdateHistory() {
            return getUpdateHistory;
        }

        @Override
        public boolean getArchived() {
            return getArchived;
        }

        public SearchByExternalGuid setExternalGuid(String externalGuid) {
            this.externalGuid = externalGuid;
            return this;
        }

        @Override
        public String buildQuery() {
            return String.format(QUERY_ROOT_EXTERNAL_ENTRIES,externalGuid);
        }
    }

    public static class SearchByContent implements IQuery {
        private static String QUERY_CONTENT_ENTRIES = "{\n" +
                "    \"wildcard\": {\n" +
                "      \""+ESIndexNotesFields.CONTENT.getEsFieldName()+"\": {\n" +
                "        \"value\": \"*%s*\",\n" +
                "        \"boost\": 1.0,\n" +
                "        \"rewrite\": \"constant_score\"\n" +
                "      }\n" +
                "    }\n" +
                "  }";
        private String contentToSearch;
        private boolean getUpdateHistory;
        private boolean getArchived;

        public SearchByContent setGetUpdateHistory(boolean getUpdateHistory) {
            this.getUpdateHistory = getUpdateHistory;
            return this;
        }
        public SearchByContent setGetArchived(boolean getArchived) {
            this.getArchived = getArchived;
            return this;
        }

        public SearchByContent setContentToSearch(String contentToSearch) {
            this.contentToSearch = contentToSearch;
            return this;
        }
        
        @Override
        public boolean getUpdateHistory() {
            return getUpdateHistory;
        }

        @Override
        public boolean getArchived() {
            return getArchived;
        }

        @Override
        public String buildQuery() {
            return String.format(QUERY_CONTENT_ENTRIES,contentToSearch);
        }
    }

    public static class SearchByEntryGuid implements IQuery {
        private static String QUERY = "{\n" +
                "  \"bool\": {\n" +
                "    \"must\": [\n" +
                "      {\n" +
                "        \"match_phrase\": {\n" +
                "          \"%s\": \"%s\"\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"range\": {\n" +
                "          \""+ESIndexNotesFields.CONTENT.getEsFieldName()+"\": {\n" +
                "            \"gte\": \"%s\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        private ESIndexNotesFields searchField;
        private String entryGuid;
        private long createdDateTime;
        private boolean getUpdateHistory;
        private boolean getArchived;

        public SearchByEntryGuid setGetUpdateHistory(boolean getUpdateHistory) {
            this.getUpdateHistory = getUpdateHistory;
            return this;
        }

        public SearchByEntryGuid setGetArchived(boolean getArchived) {
            this.getArchived = getArchived;
            return this;
        }

        @Override
        public boolean getUpdateHistory() {
            return getUpdateHistory;
        }

        @Override
        public boolean getArchived() {
            return getArchived;
        }
        
        public SearchByEntryGuid setSearchField(ESIndexNotesFields searchField) {
            this.searchField = searchField;
            return this;
        }

        public SearchByEntryGuid setEntryGuid(String entryGuid) {
            this.entryGuid = entryGuid;
            return this;
        }

        public SearchByEntryGuid setCreatedDateTime(long createdDateTime) {
            this.createdDateTime = createdDateTime;
            return this;
        }

        @Override
        public String buildQuery() {
            return String.format(QUERY,searchField.getEsFieldName(),entryGuid,createdDateTime);
        }
    }

    public static class SearchArchived implements IQuery {
        
        private static String FILTER = ",\n" +
                "    \"filter\": [\n" +
                "      {\n" +
                "        \"exists\": {\n" +
                "          \"field\": \""+ESIndexNotesFields.ARCHIVED.getEsFieldName()+"\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]";
        private static String QUERY = "{\n" +
                "  \"bool\": {\n" +
                "    \"must\": [\n" +
                "      {\n" +
                "        \"match_phrase\": {\n" +
                "          \"%s\": \"%s\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]%s\n" +
                "  }\n" +
                "}";

        private String guid;
        private ESIndexNotesFields searchField;
        private boolean getUpdateHistory;

        public SearchArchived setGuid(String guid) {
            this.guid = guid;
            return this;
        }

        public SearchArchived setSearchField(ESIndexNotesFields searchField) {
            this.searchField = searchField;
            return this;
        }

        public SearchArchived setGetUpdateHistory(boolean getUpdateHistory) {
            this.getUpdateHistory = getUpdateHistory;
            return this;
        }
        
        @Override
        public String buildQuery() {
            if (searchField  == ESIndexNotesFields.ENTRY) {
                FILTER=""; // should be able to get archived results by entry which is not archived but their threads does
            }
            return String.format(QUERY,searchField.getEsFieldName(),guid,FILTER);
        }

        @Override
        public boolean getUpdateHistory() {
            return getUpdateHistory;
        }

        @Override
        public boolean getArchived() {
            return true;
        }
    }
}
