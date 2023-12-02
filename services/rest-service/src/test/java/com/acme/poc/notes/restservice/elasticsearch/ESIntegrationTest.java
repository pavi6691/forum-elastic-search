package com.acme.poc.notes.restservice.elasticsearch;
import com.acme.poc.notes.restservice.base.AbstractIntegrationTest;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.ESNoteEntity;
import com.acme.poc.notes.restservice.service.esservice.ESNotesService;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public class ESIntegrationTest extends AbstractIntegrationTest<ESNoteEntity> {

    @Value("${index.name}")
    private String indexName;
    
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
        registry.add("spring.datasource.username", () -> "postgresql-username");
        registry.add("spring.datasource.password", () -> "postgresql-password");
        registry.add("elasticsearch.host", () -> elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200));
        registry.add("elasticsearch.clustername", () -> "");
        registry.add("index.name", () -> "note-v1");
        registry.add("default.number.of.entries.to.return", () -> 20);
        registry.add("service.thread.pool.size", () -> 8);
    }

    @Autowired
    public ESIntegrationTest(ESNotesService esNotesService, ESNoteEntity eSNoteEntity) {
        super(esNotesService, eSNoteEntity);
    }
}
