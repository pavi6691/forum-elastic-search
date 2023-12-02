package com.acme.poc.notes.restservice.persistence.postgresql.models;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.models.NoteType;
import com.acme.poc.notes.restservice.util.DTOMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Transient;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Table(name = "notes")
@Entity(name = "note")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PGNoteEntity implements INoteEntity<PGNoteEntity> {

    @Id
    @Column(name = "guid", nullable = false)
    private UUID guid;
    private String externalDataSource;
    private UUID externalGuid;
    private UUID externalItemGuid;
    private String externalItemId;
    private UUID threadGuid;
    private UUID entryGuid;
    private UUID entryGuidParent;
    private String userId;
    private NoteType type;
    private String content;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Object customJson;
    private Date createdInitially;
    private Date created;
    private Date archived;
    private Boolean isDirty;
    @Transient
    @OneToMany
    private List<PGNoteEntity> threads = null; // Answers/responses to this note
    @Transient
    @OneToMany
    private List<PGNoteEntity> history = null; // Previous versions of this entryGuid, sorted by ???
    @Override
    public PGNoteEntity copyThis() {
        return DTOMapper.INSTANCE.from(this);
    }
    @Override
    public PGNoteEntity newInstance() {
        return new PGNoteEntity();
    }
}
