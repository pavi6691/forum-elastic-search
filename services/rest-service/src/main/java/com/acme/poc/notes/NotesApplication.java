package com.acme.poc.notes;

import com.acme.poc.notes.service.ESNotesService;
import com.acme.poc.notes.service.INotesService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@OpenAPIDefinition(info = @Info(title = "Notes REST APIs", version = "0.0.0-SNAPSHOT", description = "Notes can be used by other microservices to represent notes, comments, responses etc to existing entities."))
@SpringBootApplication
public class NotesApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(NotesApplication.class, args);

//        INotesService esNotesService = context.getBean(ESNotesService.class);
//        esNotesService.createIndex(context.getEnvironment().getProperty("index.name"));
    }

}
