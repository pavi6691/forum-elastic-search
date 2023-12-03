package com.acme.poc.notes.restservice.util;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
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
    ESNoteEntity toESEntity(NoteEntry noteEntry);
    ESNoteEntity toESEntity(PGNoteEntity pgNoteEntity);
    ESNoteEntity from(ESNoteEntity eSNoteEntity);
    NoteEntry toDTO(ESNoteEntity eSNoteEntity);

    // ES to PG, for search on elasticsearch for PGSQL crud operations
    PGNoteEntity toPG(ESNoteEntity pgNoteEntity);
    PGNoteEntity from(PGNoteEntity pgNoteEntity);

}
