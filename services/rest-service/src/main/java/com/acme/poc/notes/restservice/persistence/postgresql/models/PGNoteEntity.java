package com.acme.poc.notes.restservice.persistence.postgresql.models;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.models.NoteType;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;


@Getter
@Setter
@Table(name = "notes")
@Entity(name = "note")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PGNoteEntity implements INoteEntity {
    @Id
    @Column(name = "guid", nullable = false)
    private UUID guid;
    private UUID externalGuid;
    private UUID threadGuid;
    private UUID entryGuid;
    private UUID entryGuidParent;
    private NoteType type;
    private String content;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Object customJson;
    private Date created;
    private Date archived;
}
