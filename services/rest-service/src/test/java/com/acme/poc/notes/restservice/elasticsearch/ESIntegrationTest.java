package com.acme.poc.notes.restservice.elasticsearch;
import com.acme.poc.notes.restservice.base.AbstractIntegrationTest;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.service.esservice.ESNotesService;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ESIntegrationTest extends AbstractIntegrationTest<ESNoteEntity> {

    @Value("${index.name}")
    private String indexName;

    @Autowired
    public ESIntegrationTest(ESNotesService esNotesService, ESNoteEntity eSNoteEntity) {
        super(esNotesService, eSNoteEntity);
    }
}
