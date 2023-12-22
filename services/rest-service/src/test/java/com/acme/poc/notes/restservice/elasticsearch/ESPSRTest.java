package com.acme.poc.notes.restservice.elasticsearch;

import com.acme.poc.notes.restservice.generics.models.INoteEntity;
import com.acme.poc.notes.restservice.base.AbstractBaseTest;
import com.acme.poc.notes.restservice.generics.queries.enums.OperationStatus;
import com.acme.poc.notes.restservice.generics.queries.enums.ResultFormat;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.generics.queries.enums.Field;
import com.acme.poc.notes.restservice.service.esservice.ESNotesService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;


@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Configuration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ESPSRTest extends AbstractBaseTest<ESNoteEntity> {

    @Autowired
    public ESPSRTest(ESNotesService esNotesService) {
        super(esNotesService);
    }

    //for 10K, its stack overflow for V2 as JVM stack size exceeding. V3 doesn't use recursion, so we are good there
    @Value("${com.acme.poc.notes.test.number-of-entries:10}")
    private int NUMBER_OF_ENTRIES;
    static final UUID EXTERNAL_GUID = UUID.fromString("164c1633-44f0-4eee-8491-d5e6a5391300");


    @Test
    @Order(1)
    void createEntries() {
        INoteEntity entry = createNewEntry(ESNoteEntity.builder()
                .externalGuid(EXTERNAL_GUID)
                .content("New External Entry-1")
                .build());
        IntStream.range(0, NUMBER_OF_ENTRIES)
                .forEach(i -> createThread(entry, "New External Entry-Thread-" + i));
        log.info("Created {} entries", NUMBER_OF_ENTRIES + 1);
    }

    @Test
    @Order(2)
    void searchEntries() {
        IQueryRequest query = QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .allEntries(true)
                .searchData(EXTERNAL_GUID.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build();
        List<ESNoteEntity> searchResult = notesService.get(query);
        validateAll(searchResult, 1, NUMBER_OF_ENTRIES + 1, NUMBER_OF_ENTRIES, 0);
        log.info("Found {} entries", NUMBER_OF_ENTRIES + 1);
    }

    @Test
    @Order(3)
    void deleteEntries() {
        List<ESNoteEntity> searchResult = notesService.delete(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .allEntries(true)
                .searchData(EXTERNAL_GUID.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .resultFormat(ResultFormat.FLATTEN)
                .build(),OperationStatus.DELETE);
        log.info("Deleted {} entries", searchResult.size());
        validateAll(searchResult, 1, NUMBER_OF_ENTRIES + 1, NUMBER_OF_ENTRIES, 0);
        searchResult = notesService.get(QueryRequest.builder()
                .searchField(Field.EXTERNAL)
                .searchData(EXTERNAL_GUID.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 0, 0, 0, 0);

    }

}
