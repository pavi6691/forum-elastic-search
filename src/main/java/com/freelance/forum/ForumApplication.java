package com.freelance.forum;

import com.freelance.forum.service.ESNotesService;
import com.freelance.forum.service.INotesService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ForumApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ForumApplication.class, args);
        INotesService esNotesService = context.getBean(ESNotesService.class);
        esNotesService.createIndex(context.getEnvironment().getProperty("index.name"));
    }
}
