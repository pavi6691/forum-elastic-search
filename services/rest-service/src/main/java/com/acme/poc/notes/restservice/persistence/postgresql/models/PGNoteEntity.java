package com.acme.poc.notes.restservice.persistence.postgresql.models;

import com.acme.poc.notes.models.NoteType;
import lombok.Getter;
import lombok.Setter;

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
public class PGNoteEntity {

    @Id
    @Column(name = "guid", nullable = false)
    private UUID guid;
    private UUID externalGuid;
    private UUID threadGuid;
    private UUID entryGuid;
    private UUID entryGuidParent;
    private NoteType type;
    private String content;
    private Object customJson;
    private Date created;
    private Date archived;

}
