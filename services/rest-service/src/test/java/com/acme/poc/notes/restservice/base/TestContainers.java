package com.acme.poc.notes.restservice.base;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import java.time.Duration;
public abstract class TestContainers {
    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:6.8.12";
    private static final String POSTGRESQL_IMAGE = "postgres:15.5-alpine";
    @Container
    public static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(ELASTICSEARCH_IMAGE);
    @Container
    public static final PostgreSQLContainer postgresqlContainer = new PostgreSQLContainer(POSTGRESQL_IMAGE)
            .withDatabaseName("acme")
            .withUsername("postgresql-username")
            .withPassword("postgresql-password");
    
    static {
        // elasticsearch
        elasticsearchContainer
                .withNetworkAliases("elasticsearch")
                .withReuse(true)
                .setWaitStrategy((new LogMessageWaitStrategy())
                        .withRegEx(".*(\"message\":\\s?\"started[\\s?|\"].*|] started\n$)")
                        .withStartupTimeout(Duration.ofSeconds(180L)));
        elasticsearchContainer.start();

        // postgresql
        postgresqlContainer
                .withReuse(true)
                .withNetworkAliases("postgresql");
        postgresqlContainer.start();
    }
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.username", () -> "postgresql-username");
        registry.add("spring.datasource.password", () -> "postgresql-password");
        registry.add("elasticsearch.host", () -> elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200));
        registry.add("elasticsearch.clustername", () -> "");
        registry.add("index.name", () -> "note-v1");
        registry.add("default.db.response.size", () -> 20);
        registry.add("service.thread.pool.size", () -> 8);
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
    }
}
