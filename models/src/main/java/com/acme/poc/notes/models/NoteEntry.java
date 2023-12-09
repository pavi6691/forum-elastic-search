package com.acme.poc.notes.models;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.models.validation.CreateValidationGroup;
import com.acme.poc.notes.models.validation.UpdateValidationGroup;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//TODO assign validation group when enabled fields that are currently not used
@Builder
public record NoteEntry(
    @NotNull(groups = UpdateValidationGroup.class)
    UUID guid,
    @NotNull(groups = CreateValidationGroup.class)
    String externalDataSource,         // Required, not null. Not used in this phase
    @NotNull(groups = CreateValidationGroup.class)
    UUID externalGuid,
    @NotNull(groups = CreateValidationGroup.class)
    UUID externalItemGuid,             // TODO We need to talk about this compared to externalGuid in a meeting
    String externalItemId,             // TODO Not used in this phase, but should be persisted as null
    UUID threadGuid,
    UUID entryGuid,
    UUID entryGuidParent,
    String userId,                     // TODO Required, not null. Not used in this phase
    @NotNull(groups = CreateValidationGroup.class)
    NoteType type,
    @NotNull(groups = {CreateValidationGroup.class,UpdateValidationGroup.class})
    String content,
    Object customJson,
    @JsonFormat(pattern = NotesConstants.TIMESTAMP_ISO8601)
    Date createdInitially,             // Required, not null. Will be the initial created timestamp and should be the same across all versions of same note
    @JsonFormat(pattern = NotesConstants.TIMESTAMP_ISO8601)
    @NotNull(groups = UpdateValidationGroup.class)
    Date created,
    @JsonFormat(pattern = NotesConstants.TIMESTAMP_ISO8601)
    Date archived,
    List<NoteEntry> threads,
    List<NoteEntry> history

) {
}
