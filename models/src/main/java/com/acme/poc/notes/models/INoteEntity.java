package com.acme.poc.notes.models;

import java.util.*;
public interface INoteEntity<E> {
    UUID getGuid();
    void setGuid(UUID guid);
    UUID getExternalGuid();
    void setExternalGuid(UUID externalGuid);
    UUID getThreadGuid();
    void setThreadGuid(UUID threadGuid);
    UUID getEntryGuid();
    void setEntryGuid(UUID entryGuid);
    UUID getEntryGuidParent();
    void setEntryGuidParent(UUID entryGuidParent);
    NoteType getType();
    void setType(NoteType type);
    String getContent();
    void setContent(String content);
    Object getCustomJson();
    void setCustomJson(Object customJson);
    Date getCreated();
    void setCreated(Date created);
    Date getArchived();
    void setArchived(Date archived);
    default Collection<E> getHistory() {return new ArrayList<>();}
    default Collection<E> getThreads() {return new ArrayList<>();}
    default void addThreads(INoteEntity threads, int index) {}
    default void addHistory(INoteEntity history, int index) {}
    E getInstance();
}
