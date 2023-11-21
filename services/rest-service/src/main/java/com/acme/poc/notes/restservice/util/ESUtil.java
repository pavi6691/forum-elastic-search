package com.acme.poc.notes.restservice.util;

import com.acme.poc.notes.models.INoteEntity;

import java.util.Date;


public class ESUtil {


    public static Date getCurrentDate() {
        return new Date();
    }

    public static void clearHistoryAndThreads(INoteEntity entry) {
        if (entry.getThreads() != null) {
            entry.getThreads().clear();
        }
        if (entry.getHistory() != null) {
            entry.getHistory().clear();
        }
    }

}
