package com.acme.poc.notes.restservice.configuration;

import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.postgresql.models.PGNoteEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.acme.poc.notes.restservice")
public class AppConfig {
    @Bean
    public NotesData notesData() {
        return new NotesData();
    }
    @Bean
    public PGNoteEntity pgNoteEntity() {
        return new PGNoteEntity();
    }
}
