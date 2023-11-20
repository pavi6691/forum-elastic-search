package com.acme.poc.notes.models;

import java.util.*;
public interface INoteEntity {
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
}
