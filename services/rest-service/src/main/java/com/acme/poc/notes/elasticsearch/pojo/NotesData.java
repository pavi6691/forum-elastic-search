package com.acme.poc.notes.elasticsearch.pojo;

import com.acme.poc.notes.serialzation.CustomDateDeserializer;
import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.serialzation.CustomDateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "#{@indexName}", createIndex = false, writeTypeHint = WriteTypeHint.FALSE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotesData {
    
    @Id
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "guid")
    @Getter @Setter
    private UUID guid; // Unique for the document and also the Elasticsearch key/id
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "externalGuid")
    @NotNull
    @Getter @Setter private UUID externalGuid; // An external Guid

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "threadGuid")
    @Getter @Setter private UUID threadGuid; // A thread Guid

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "entryGuid")
    @Getter @Setter private UUID entryGuid; // A Guid for this entry

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "threadGuidParent")
    @Getter @Setter private UUID threadGuidParent; // A Guid for this entry's parent
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Field(type = FieldType.Text, name = "content")
    @Getter @Setter private String content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Field(type = FieldType.Date, name = "created", format = DateFormat.custom, pattern = NotesConstants.TIMESTAMP_ISO8601)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter @Setter private Date created;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Field(type = FieldType.Date, name = "archived", format = DateFormat.custom, pattern = NotesConstants.TIMESTAMP_ISO8601)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter @Setter private Date archived;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter @Setter private List<NotesData> threads = null; // answers/responses to this answer

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter @Setter private List<NotesData> history = null; // Previous versions of this entryGuid, sorted by

    public void addThreads(NotesData threads, int index) {
        if( this.threads == null) {
            this.threads  = new LinkedList<>();;
        }
        this.threads.add(index,threads);
    }
    public void addHistory(NotesData history, int index) {
        if( this.history == null) {
            this.history  = new LinkedList<>();;
        }
        this.history.add(index,history);
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
