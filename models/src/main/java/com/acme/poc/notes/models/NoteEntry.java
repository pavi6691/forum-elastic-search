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
) {
}
