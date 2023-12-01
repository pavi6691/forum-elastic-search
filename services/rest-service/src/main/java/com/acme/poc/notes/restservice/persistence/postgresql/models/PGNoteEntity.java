package com.acme.poc.notes.restservice.persistence.postgresql.models;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.models.NoteType;
import com.acme.poc.notes.restservice.util.DTOMapperImpl;
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
    @Override
    public PGNoteEntity getInstance(PGNoteEntity pgNoteEntity) {
        DTOMapperImpl mapper = new DTOMapperImpl(); // TODO DTOMapper.INSTANCE.from(notesData) classpath error
        return mapper.from(pgNoteEntity);
    }
}
