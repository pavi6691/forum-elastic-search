package com.acme.poc.notes.restservice;

import com.acme.poc.notes.restservice.service.esservice.ESNotesService;
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
        ESNotesService esNotesService = context.getBean(ESNotesService.class);
        esNotesService.createDataStore(context.getEnvironment().getProperty("index.name"));
    }

}
