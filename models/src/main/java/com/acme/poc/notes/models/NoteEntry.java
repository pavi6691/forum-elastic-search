package com.acme.poc.notes.models;

import com.fasterxml.jackson.databind.JsonNode;
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
    UUID threadGuidParent,
    NoteType type,
    String content,
    JsonNode customJson,
    Date created,
    Date archived,
    List<NoteEntry> threads,
    List<NoteEntry> history
) {
}
