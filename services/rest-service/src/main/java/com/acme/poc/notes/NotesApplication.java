package com.acme.poc.notes;

import com.acme.poc.notes.service.ESNotesService;
import com.acme.poc.notes.service.INotesService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class NotesApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(NotesApplication.class, args);
        INotesService esNotesService = context.getBean(ESNotesService.class);
        esNotesService.createIndex(context.getEnvironment().getProperty("index.name"));
    }
}
