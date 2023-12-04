package com.acme.poc.notes.models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    default List<E> getHistory() {return Collections.emptyList();}
    default List<E> getThreads() {return Collections.emptyList();}
    @JsonIgnore
    default void setIsDirty(Boolean isDirty) {}
    @JsonIgnore
    default Boolean getIsDirty() {return false;}
    default void setThreads(List<E> threads){}
    default void setHistory(List<E> history) {}
    @JsonIgnore
    default void addThreads(INoteEntity threads, int index) {
        if (getThreads() == null) {
            setThreads(new LinkedList<>());
        }
        getThreads().add(index, (E) threads);
    }
    @JsonIgnore
    default void addHistory(INoteEntity history, int index) {
        if (getHistory() == null) {
            setHistory(new LinkedList<>());
        }
        getHistory().add(index, (E) history);
    }
    
    @JsonIgnore
    static <T> T fromJson(String json, Class<T> eClass) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructType(eClass));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @JsonIgnore
    E copyThis();
    @JsonIgnore
    E newInstance();

}
