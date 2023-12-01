package com.acme.poc.notes.restservice;

import com.acme.poc.notes.models.INoteEntity;
import com.acme.poc.notes.restservice.base.BaseTest;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.service.generics.queries.IQueryRequest;
import com.acme.poc.notes.restservice.service.generics.queries.QueryRequest;
import com.acme.poc.notes.restservice.service.generics.queries.enums.Filter;
import com.acme.poc.notes.restservice.service.generics.queries.enums.Match;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;


@Slf4j
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Configuration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PSRTest extends BaseTest {

    //for 10K, its stack overflow for V2 as JVM stack size exceeding. V3 doesn't use recursion, so we are good there
    @Value("${com.acme.poc.notes.test.number-of-entries:10}")
    private int NUMBER_OF_ENTRIES;
    static final UUID EXTERNAL_GUID = UUID.fromString("164c1633-44f0-4eee-8491-d5e6a5391300");

    @Test
    @Order(1)
    void createEntries() {
        INoteEntity entry = createNewEntry(NotesData.builder()
                .externalGuid(EXTERNAL_GUID)
                .content("New External Entry-1")
                .build(),esNotesService);
        IntStream.range(0, NUMBER_OF_ENTRIES)
                .forEach(i -> createThread(entry, "New External Entry-Thread-" + i));
        log.info("Created {} entries", NUMBER_OF_ENTRIES + 1);
    }

    @Test
    @Order(2)
    void searchEntries() {
        IQueryRequest query = QueryRequest.builder()
                .searchField(Match.EXTERNAL)
                .searchData(EXTERNAL_GUID.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build();
        List<NotesData> searchResult = notesAdminService.searchByExternalGuid(query);
        validateAll(searchResult, 1, NUMBER_OF_ENTRIES + 1, NUMBER_OF_ENTRIES, 0);
        log.info("Found {} entries", NUMBER_OF_ENTRIES + 1);
    }

    @Test
    @Order(3)
    void deleteEntries() {
        List<NotesData> searchResult = notesAdminService.deleteByExternalGuid(EXTERNAL_GUID);
        log.info("Deleted {} entries", searchResult.size());
        validateAll(searchResult, NUMBER_OF_ENTRIES + 1, NUMBER_OF_ENTRIES + 1, 0, 0);
        searchResult = notesAdminService.searchByExternalGuid(QueryRequest.builder()
                .searchField(Match.EXTERNAL)
                .searchData(EXTERNAL_GUID.toString())
                .filters(Set.of(Filter.INCLUDE_VERSIONS, Filter.INCLUDE_ARCHIVED))
                .build());
        validateAll(searchResult, 0, 0, 0, 0);

    }

}
