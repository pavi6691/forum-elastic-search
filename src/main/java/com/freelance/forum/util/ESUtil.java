package com.freelance.forum.util;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import org.elasticsearch.search.sort.SortOrder;

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

    public static SortOrder getSortOrder(ESIndexNotesFields esIndexNotesFields){
        if(ESIndexNotesFields.EXTERNAL == esIndexNotesFields) {
            return SortOrder.DESC; // DESC to get latest updated entry with threadParentGuid null and that entry will be root
        } else {
            return SortOrder.ASC;
        }
    }
}
