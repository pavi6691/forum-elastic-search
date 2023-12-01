package com.acme.poc.notes.restservice.util;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.models.NoteEntry;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);


    // PostgreSQL
    PGNoteEntity toEntity(NoteEntry noteEntry);
    NoteEntry toDTO(PGNoteEntity pgNoteEntity);

    // Elasticsearch
    NotesData toESEntity(NoteEntry noteEntry);
    NotesData from(NotesData notesData);
    NoteEntry toDTO(NotesData notesData);

    // ES to PG, for search on elasticsearch for PGSQL crud operations
    PGNoteEntity toPG(NotesData pgNoteEntity);
    PGNoteEntity from(PGNoteEntity pgNoteEntity);

}
