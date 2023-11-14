package com.acme.poc.notes;

import com.acme.poc.notes.base.BaseTest;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.SearchByExternalGuid;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;


@Slf4j
@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Configuration
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PSRTest extends BaseTest {

    //for 10K, its stack overflow for V2 as JVM stack size exceeding. V3 doesn't use recursion, so we are good there
    @Value("${com.acme.poc.notes.test.number-of-entries:10}")
    private int NUMBER_OF_ENTRIES;
    static final UUID EXTERNAL_GUID = UUID.fromString("164c1633-44f0-4eee-8491-d5e6a5391300");


    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:6.8.12";

    @Container
    public static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(ELASTICSEARCH_IMAGE);


    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        elasticsearchContainer
                .withNetworkAliases("elasticsearch")
                .setWaitStrategy((new LogMessageWaitStrategy())
                        .withRegEx(".*(\"message\":\\s?\"started[\\s?|\"].*|] started\n$)")
                        .withStartupTimeout(Duration.ofSeconds(180L)));
        elasticsearchContainer.start();

        registry.add("elasticsearch.host", () -> elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200));
        registry.add("elasticsearch.clustername", () -> "");
        registry.add("index.name", () -> "note-v1");
        registry.add("default.number.of.entries.to.return", () -> 20);
        registry.add("service.thread.pool.size", () -> 8);
    }

//    @BeforeAll
//    void setup() {
//        elasticsearchContainer
//                .withNetworkAliases("elasticsearch")
//                .setWaitStrategy((new LogMessageWaitStrategy())
//                        .withRegEx(".*(\"message\":\\s?\"started[\\s?|\"].*|] started\n$)")
//                        .withStartupTimeout(Duration.ofSeconds(180L)));
//        elasticsearchContainer.start();
//
////        assertEquals(notesAdminService.createIndex(indexName), indexName);   // TODO This does not validate correctly//    }


    @Test
    @Order(1)
    void createEntries() {
        NotesData entry = createNewEntry(NotesData.builder()
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
        SearchByExternalGuid query = SearchByExternalGuid.builder()
                .searchGuid(EXTERNAL_GUID.toString())
                .includeVersions(true)
                .includeArchived(true)
                .build();
        List<NotesData> searchResult = notesAdminService.searchByExternalGuid(query);
        validateAll(searchResult, 1, NUMBER_OF_ENTRIES + 1, NUMBER_OF_ENTRIES, 0);
        log.info("Found {} entries", NUMBER_OF_ENTRIES + 1);
    }

    @Test
    @Order(3)
    void deleteEntries() {
        List<NotesData> searchResult = notesAdminService.deleteByExternalGuid(EXTERNAL_GUID);
        log.info("Deleted {} entries", searchResult.size());  // TODO This returns '1'. Shouldn't it return NUMBER_OF_ENTRIES + 1 ?
        validateAll(searchResult, 1, NUMBER_OF_ENTRIES + 1, NUMBER_OF_ENTRIES, 0);
        searchResult = notesAdminService.searchByExternalGuid(SearchByExternalGuid.builder()
                .searchGuid(EXTERNAL_GUID.toString())
                .includeVersions(true)
                .includeArchived(true)
                .build());
        validateAll(searchResult, 0, 0, 0, 0);

    }

}
