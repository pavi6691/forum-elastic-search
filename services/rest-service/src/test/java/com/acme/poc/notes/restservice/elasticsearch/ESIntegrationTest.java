package com.acme.poc.notes.restservice.elasticsearch;
import com.acme.poc.notes.restservice.base.AbstractIntegrationTest;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.service.esservice.ESNotesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void searchContent() {
        List<ESNoteEntity> result = notesService.get(QueryRequest.builder()
                .searchField(Field.CONTENT)
                .searchData("content")
                .resultFormat(ResultFormat.FLATTEN)
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        checkDuplicates(result,new HashSet<>());
        assertEquals(11, result.size());
    }
}
