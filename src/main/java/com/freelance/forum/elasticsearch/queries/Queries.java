package com.freelance.forum.elasticsearch.queries;

public class Queries {

    /**
     * Search query to fetch root entries by external guid. also returns historical records
     */
    public static class SearchByExternalGuid implements IQuery {
        private static final String QUERY = "{\n" +
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
            return String.format(QUERY,externalGuid);
        }
    }

    /**
     * Search for content
     */
    public static class SearchByContent implements IQuery {
        private static String QUERY = "{\n" +
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
            return String.format(QUERY,contentToSearch);
        }
    }

    /**
     * Search by any fields. returns entries created after passed time im millis
     */
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
                "          \""+ESIndexNotesFields.CREATED.getEsFieldName()+"\": {\n" +
                "            \"gte\": \"%s\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        private ESIndexNotesFields searchField;
        private String guid;
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

        public SearchByEntryGuid setSearchBy(String guid) {
            this.guid = guid;
            return this;
        }

        public SearchByEntryGuid setCreatedDateTime(long createdDateTime) {
            this.createdDateTime = createdDateTime;
            return this;
        }

        @Override
        public String buildQuery() {
            return String.format(QUERY,searchField.getEsFieldName(),guid,createdDateTime);
        }
    }

    /**
     * Query to search archived entries. Returns only archived entries.
     */
    public static class SearchArchivedByExternalGuid implements IQuery {
        private static String QUERY = "{\n" +
                "  \"bool\": {\n" +
                "    \"must\": [\n" +
                "      {\n" +
                "        \"match_phrase\": {\n" +
                "          \""+ESIndexNotesFields.EXTERNAL.getEsFieldName()+"\": \"%s\"\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"filter\": [\n" +
                "      {\n" +
                "        \"exists\": {\n" +
                "          \"field\": \"archived\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        private String externalGuid;
        private boolean getUpdateHistory;

        public SearchArchivedByExternalGuid setExternalGuid(String externalGuid) {
            this.externalGuid = externalGuid;
            return this;
        }

        public SearchArchivedByExternalGuid setGetUpdateHistory(boolean getUpdateHistory) {
            this.getUpdateHistory = getUpdateHistory;
            return this;
        }
        
        @Override
        public String buildQuery() {
            return String.format(QUERY,externalGuid);
        }

        @Override
        public boolean getUpdateHistory() {
            return getUpdateHistory;
        }
    }

    /**
     * Query to search archived entries by entryGuid. Returns all entries even if they are not archived. filter is done in the code. 
     * For any entryGuid(not archived), if their threads are archived, then return them
     */
    public static class SearchArchivedByEntryGuid implements IQuery {
        private static String QUERY = "{\n" +
                "  \"bool\": {\n" +
                "    \"must\": [\n" +
                "      {\n" +
                "        \"match_phrase\": {\n" +
                "          \""+ESIndexNotesFields.ENTRY.getEsFieldName()+"\": \"%s\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        private String entryGuid;
        private boolean getUpdateHistory;

        public SearchArchivedByEntryGuid setEntryGuid(String entryGuid) {
            this.entryGuid = entryGuid;
            return this;
        }

        public SearchArchivedByEntryGuid setGetUpdateHistory(boolean getUpdateHistory) {
            this.getUpdateHistory = getUpdateHistory;
            return this;
        }

        @Override
        public String buildQuery() {
            return String.format(QUERY,entryGuid);
        }

        @Override
        public boolean getUpdateHistory() {
            return getUpdateHistory;
        }
    }
}
