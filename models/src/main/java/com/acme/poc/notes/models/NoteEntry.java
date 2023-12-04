package com.acme.poc.notes.models;

import com.acme.poc.notes.core.NotesConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Builder
public record NoteEntry(

    UUID guid,
    @NotNull
    String externalDataSource,         // Required, not null. Not used in this phase
    @NotNull
    UUID externalGuid,
    @NotNull
    UUID externalItemGuid,             // We need to talk about this compared to externalGuid in a meeting
    @NotNull
    String externalItemId,             // Not used in this phase, but should be persisted as null
    UUID threadGuid,
    UUID entryGuid,
    UUID entryGuidParent,
    String userId,                     // Required, not null. Not used in this phase
    @NotNull
    NoteType type,
    @NotNull
    String content,
    Object customJson,
    @JsonFormat(pattern = NotesConstants.TIMESTAMP_ISO8601)
    Date createdInitially,             // Required, not null. Will be the initial created timestamp and should be the same across all versions of same note
    @JsonFormat(pattern = NotesConstants.TIMESTAMP_ISO8601)
    Date created,
    @JsonFormat(pattern = NotesConstants.TIMESTAMP_ISO8601)
    Date archived,
    Boolean isDirty,                   // Initial true. Should be false when data is ACID persisted to PostgreSQL AND Elasticsearch. Only used in PostgreSQL entity, not Elasticsearch and should also be removed from this class. Only here to not break code in entities.
    List<NoteEntry> threads,
    List<NoteEntry> history

) {
}
