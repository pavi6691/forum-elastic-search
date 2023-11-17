package com.acme.poc.notes.restservice.util;

import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import java.util.Date;


public class ESUtil {

    public static Date getCurrentDate() {
        return new Date();
    }

    public static void clearHistoryAndThreads(NotesData entry) {
        if (entry.getThreads() != null) {
            entry.getThreads().clear();
        }
        if (entry.getHistory() != null) {
            entry.getHistory().clear();
        }
    }
}
