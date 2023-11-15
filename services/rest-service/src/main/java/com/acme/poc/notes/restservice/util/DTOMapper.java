package com.acme.poc.notes.restservice.util;

import com.acme.poc.notes.restservice.persistence.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.models.NoteEntry;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);


    PGNoteEntity toEntity(NoteEntry noteEntry);
    NoteEntry toDTO(PGNoteEntity pgNoteEntity);

    NotesData toESEntity(NoteEntry noteEntry);
    NoteEntry toDTO(NotesData notesData);

}
