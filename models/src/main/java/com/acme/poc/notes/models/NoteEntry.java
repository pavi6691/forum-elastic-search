package com.acme.poc.notes.models;

import lombok.Builder;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Builder
public record NoteEntry(

    UUID guid,
    String externalDataSource,         // Required, not null. Not used in this phase
    UUID externalGuid,
    UUID externalItemGuid,             // We need to talk about this compared to externalGuid in a meeting
    String externalItemId,             // Not used in this phase, but should be persisted as null
    UUID threadGuid,
    UUID entryGuid,
    UUID entryGuidParent,
    String userId,                     // Required, not null. Not used in this phase
    NoteType type,
    String content,
    Object customJson,
    Date createdInitially,             // Required, not null. Will be the initial created timestamp and should be the same across all versions of same note
    Date created,
    Date archived,
    Boolean isDirty,                   // Initial true. Should be false when data is ACID persisted to PostgreSQL AND Elasticsearch. Only used in PostgreSQL entity, not Elasticsearch and should also be removed from this class. Only here to not break code in entities.
    List<NoteEntry> threads,
    List<NoteEntry> history

) {
}
