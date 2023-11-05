package com.acme.poc.notes.util;

import com.acme.poc.notes.elasticsearch.pojo.NotesData;

import java.util.Date;
import java.util.Set;

public class ESUtil {

    public static void flatten(NotesData root, Set<NotesData> entries) {
        entries.add(root);
        if(root.getThreads() != null)
            root.getThreads().forEach(e -> flatten(e,entries));
        if(root.getHistory() != null)
            root.getHistory().forEach(e -> flatten(e,entries));
    }

    public static Date getCurrentDate() {
        return new Date();
    }

    public static void clearHistoryAndThreads(NotesData entry) {
        if(entry.getThreads() != null) {
            entry.getThreads().clear();
        }
        if(entry.getHistory() != null) {
            entry.getHistory().clear();
        }
    }
}
