package com.acme.poc.notes.restservice.persistence.elasticsearch.models;

import com.acme.poc.notes.core.NotesConstants;
import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.models.NoteType;
import com.acme.poc.notes.restservice.serialzation.CustomDateDeserializer;
import com.acme.poc.notes.restservice.serialzation.CustomDateSerializer;
import com.acme.poc.notes.restservice.util.DTOMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import javax.validation.constraints.NotNull;
import java.util.*;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "#{@indexName}", writeTypeHint = WriteTypeHint.FALSE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESNoteEntity implements INoteEntity<ESNoteEntity> {
    
    @Id
    @Field(type = FieldType.Keyword, name = "guid")
    private UUID guid;

    @Field(type = FieldType.Text, name = "externalDataSource")
    @NotNull
    private String externalDataSource;
    
    @Field(type = FieldType.Keyword, name = "externalGuid")
    @NotNull
    private UUID externalGuid;

    @Field(type = FieldType.Keyword, name = "externalItemGuid")
    @NotNull
    private UUID externalItemGuid;
    @Field(type = FieldType.Keyword, name = "externalItemId")
    private String externalItemId;

    @Field(type = FieldType.Keyword, name = "threadGuid")
    private UUID threadGuid;

    @Field(type = FieldType.Keyword, name = "entryGuid")
    private UUID entryGuid;

    @Field(type = FieldType.Keyword, name = "entryGuidParent")
    private UUID entryGuidParent;

    @Field(type = FieldType.Keyword, name = "userId")
    private String userId;

    @Field(type = FieldType.Keyword, name = "type")
    private NoteType type = NoteType.NOTE;
    
    @Field(type = FieldType.Text, name = "content")
    private String content;

    @Field(type = FieldType.Object, name = "customJson")
    private Object customJson;

    @Field(type = FieldType.Date, name = "createdInitially", pattern = NotesConstants.TIMESTAMP_ISO8601)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date createdInitially;

    @Field(type = FieldType.Date, name = "created", pattern = NotesConstants.TIMESTAMP_ISO8601)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date created;

    @Field(type = FieldType.Date, name = "archived", pattern = NotesConstants.TIMESTAMP_ISO8601)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date archived;
    private List<ESNoteEntity> threads = null; // Answers/responses to this note
    private List<ESNoteEntity> history = null; // Previous versions of this entryGuid, sorted by ???
    @Override
    public ESNoteEntity copyThis() {
        return DTOMapper.INSTANCE.from(this);
    }
    @Override
    public ESNoteEntity newInstance() {
        return new ESNoteEntity();
    }
}
