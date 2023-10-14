package com.freelance.forum.elasticsearch.pojo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import com.freelance.forum.serialzation.CustomDateSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "#{@indexName}", createIndex = false, writeTypeHint = WriteTypeHint.FALSE)
public class NotesData {
    
    @Id
    @JsonDeserialize(using = UUIDDeserializer.class)
    private UUID guid = UUID.randomUUID(); // Unique for the document and also the Elasticsearch key/id
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "externalGuid")
    private UUID externalGuid; // An external Guid

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "threadGuid")
    private UUID threadGuid; // A thread Guid

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "entryGuid")
    private UUID entryGuid; // A Guid for this entry

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = UUIDDeserializer.class)
    @Field(type = FieldType.Keyword, name = "threadGuidParent")
    private UUID threadGuidParent; // A Guid for this entry's parent
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Field(type = FieldType.Text, name = "content")
    private String content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Field(type = FieldType.Date, name = "created", format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date created;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Field(type = FieldType.Date, name = "archived", format = DateFormat.custom, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date archived;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<NotesData> threads = null; // answers/responses to this answer

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<NotesData> history = null; // Previous versions of this entryGuid, sorted by

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    public UUID getExternalGuid() {
        return externalGuid;
    }

    public void setExternalGuid(UUID externalGuid) {
        this.externalGuid = externalGuid;
    }

    public UUID getThreadGuid() {
        return threadGuid;
    }

    public void setThreadGuid(UUID threadGuid) {
        this.threadGuid = threadGuid;
    }

    public UUID getEntryGuid() {
        return entryGuid;
    }

    public void setEntryGuid(UUID entryGuid) {
        this.entryGuid = entryGuid;
    }

    public UUID getThreadGuidParent() {
        return threadGuidParent;
    }

    public void setThreadGuidParent(UUID threadGuidParent) {
        this.threadGuidParent = threadGuidParent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setThreads(List<NotesData> threads) {
        this.threads = threads;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getArchived() {
        return archived;
    }

    public void setArchived(Date archived) {
        this.archived = archived;
    }

    public List<NotesData> getThreads() {
        return threads;
    }

    public void setAnswers(List<NotesData> answers) {
        this.threads = answers;
    }

    public List<NotesData> getHistory() {
        return history;
    }

    public void setHistory(List<NotesData> history) {
        this.history = history;
    }

    public void addThreads(NotesData threads) {
        if( this.threads == null) {
            this.threads  = new ArrayList<>();;
        }
        this.threads.add(threads);
    }
    public void addHistory(NotesData history) {
        if( this.history == null) {
            this.history  = new ArrayList<>();;
        }
        this.history.add(history);
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
