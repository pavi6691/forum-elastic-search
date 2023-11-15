package com.acme.poc.notes.restservice.persistence.elasticsearch.pojo;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.models.NoteType;
import com.acme.poc.notes.restservice.serialzation.CustomDateDeserializer;
import com.acme.poc.notes.restservice.serialzation.CustomDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "#{@indexName}", createIndex = false, writeTypeHint = WriteTypeHint.FALSE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotesData {
    
    @Id
    @Field(type = FieldType.Keyword, name = "guid")
    private UUID guid;
    
    @Field(type = FieldType.Keyword, name = "externalGuid")
    @NotNull
    private UUID externalGuid; // An external Guid

    @Field(type = FieldType.Keyword, name = "threadGuid")
    private UUID threadGuid; // A thread Guid

    @Field(type = FieldType.Keyword, name = "entryGuid")
    private UUID entryGuid; // A Guid for this entry

    @Field(type = FieldType.Keyword, name = "entryGuidParent")
    private UUID entryGuidParent; // A Guid for this entry's parent

    @Field(type = FieldType.Keyword, name = "type")
    private NoteType type = NoteType.NOTE;
    
    @Field(type = FieldType.Text, name = "content")
    private String content;

    @Field(type = FieldType.Object, name = "customJson")
    private JsonNode customJson;

    @Field(type = FieldType.Date, name = "created", format = DateFormat.custom, pattern = NotesConstants.TIMESTAMP_ISO8601)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date created;

    @Field(type = FieldType.Date, name = "archived", format = DateFormat.custom, pattern = NotesConstants.TIMESTAMP_ISO8601)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date archived;

    private List<NotesData> threads = null; // Answers/responses to this note

    private List<NotesData> history = null; // Previous versions of this entryGuid, sorted by ???


    public void addThreads(NotesData threads, int index) {
        if (this.threads == null) {
            this.threads = new LinkedList<>();;
        }
        this.threads.add(index, threads);
    }

    public void addHistory(NotesData history, int index) {
        if (this.history == null) {
            this.history = new LinkedList<>();;
        }
        this.history.add(index, history);
    }
    
    public static NotesData fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, NotesData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
