package com.acme.poc.notes.models;

import lombok.Builder;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Builder
public record NoteEntry(

    UUID guid,
    UUID externalGuid,
    UUID threadGuid,
    UUID entryGuid,
    UUID entryGuidParent,
    NoteType type,
    String content,
    Object customJson,
    Date created,
    Date archived,
    List<NoteEntry> threads,
    List<NoteEntry> history

) implements INoteEntity {
    @Override
    public UUID getGuid() {
        return null;
    }

    @Override
    public void setGuid(UUID guid) {
        
    }

    @Override
    public UUID getExternalGuid() {
        return null;
    }

    @Override
    public void setExternalGuid(UUID externalGuid) {

    }

    @Override
    public UUID getThreadGuid() {
        return null;
    }

    @Override
    public void setThreadGuid(UUID threadGuid) {

    }

    @Override
    public UUID getEntryGuid() {
        return null;
    }

    @Override
    public void setEntryGuid(UUID entryGuid) {

    }

    @Override
    public UUID getEntryGuidParent() {
        return null;
    }

    @Override
    public void setEntryGuidParent(UUID entryGuidParent) {

    }

    @Override
    public NoteType getType() {
        return null;
    }

    @Override
    public void setType(NoteType type) {

    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public void setContent(String content) {

    }

    @Override
    public Object getCustomJson() {
        return null;
    }

    @Override
    public void setCustomJson(Object customJson) {

    }

    @Override
    public Date getCreated() {
        return null;
    }

    @Override
    public void setCreated(Date created) {

    }

    @Override
    public Date getArchived() {
        return null;
    }

    @Override
    public void setArchived(Date archived) {

    }
}
