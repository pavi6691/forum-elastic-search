package com.freelance.forum.elasticsearch.queries;

import com.freelance.forum.elasticsearch.queries.generics.AbstractQuery;
import com.freelance.forum.elasticsearch.queries.generics.enums.EsNotesFields;

/**
 * Search for content
 */
public class SearchByContent extends AbstractQuery {
    private static String QUERY = "{\n" +
            "    \"wildcard\": {\n" +
            "      \""+ EsNotesFields.CONTENT.getEsFieldName()+"\": {\n" +
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
