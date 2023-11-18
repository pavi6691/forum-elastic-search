package com.acme.poc.notes.restservice;

import com.acme.poc.notes.restservice.base.BaseTest;
import com.acme.poc.notes.restservice.data.ElasticSearchData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.models.NotesData;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.*;
import com.acme.poc.notes.restservice.persistence.elasticsearch.queries.generics.IQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest extends BaseTest {

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
////        assertEquals(notesAdminService.createIndex(indexName), indexName);   // TODO This does not validate correctly
//    }


    @Test
    void crud() {
        NotesData newEntryCreated = createNewEntry(NotesData.builder().externalGuid(UUID.randomUUID()).content("New External Entry - 1").build());

        SearchByExternalGuid querySearchByExternalGuid = SearchByExternalGuid.builder()
                .searchGuid(newEntryCreated.getExternalGuid().toString())
                .includeVersions(true)
                .includeArchived(true)
                .build();
        List<NotesData> searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 1, 0, 0);

        // Create Thread 1
        NotesData thread1 = createThread(newEntryCreated, "New External Entry-Thread-1");
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1,2, 1, 0);

        // Create Thread 2
        NotesData thread2 = createThread(newEntryCreated, "New External Entry-Thread-2");
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 3, 2, 0);

        // Create Thread 3
        NotesData thread3 = createThread(newEntryCreated, "New External Entry-Thread-3");
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 4, 3, 0);

        // Create Thread 4,5,6 and archive 5
        NotesData thread4 = createThread(newEntryCreated, "New External Entry-Thread-4");
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 5, 4, 0);
        NotesData thread5 = createThread(thread4, "New External Entry-Thread-5");
        NotesData thread6 = createThread(thread5, "New External Entry-Thread-6");
        notesService.archive(SearchByEntryGuid.builder()
                .searchGuid(thread5.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(false)
                .build());
        
        // Create Thread 1-1
        NotesData thread1_1 = createThread(thread1, "New External Entry-Thread-1-1");
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 8, 7, 0);

        // Update guid 1
        updateGuid(thread1,"New External Entry-Thread-1-Updated");
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 9, 7, 1);

        // Create Thread 1-2
        NotesData thread1_2 = createThread(thread1, "New External Entry-Thread-1-2");
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 10, 8, 1);

        // Update by entryGuid thread 1-2
        thread1.setGuid(null); // if guid is null, entry will be updated by entryGuid
        updateGuid(thread1_2, "New External Entry-Thread-1-2-Updated");
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 11, 8, 2);


        // Search by EntryId for a thread
        SearchByEntryGuid querySearchByEntryGuid = SearchByEntryGuid.builder()
                .searchGuid(thread1.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(true)
                .build();
        searchResult = notesService.searchByEntryGuid(querySearchByEntryGuid);
        validateAll(searchResult, 1, 5, 2, 2);

        // Search by EntryId for a thread with no histories
        SearchByEntryGuid searchBy_Thread_1_Entry = SearchByEntryGuid.builder()
                .searchGuid(thread1.getEntryGuid().toString())
                .includeVersions(false)
                .includeArchived(true)
                .build();
        searchResult = notesService.searchByEntryGuid(searchBy_Thread_1_Entry);
        validateAll(searchResult, 1, 3, 2, 0);

        searchBy_Thread_1_Entry = SearchByEntryGuid.builder()
                .searchGuid(thread1_2.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(false)
                .build();
        notesService.archive(searchBy_Thread_1_Entry);

        // Search archived by external entry
        SearchArchivedByExternalGuid querySearchArchived_thread1_2 = SearchArchivedByExternalGuid.builder()
                .searchGuid(thread1_2.getExternalGuid().toString())
                .includeVersions(true)
                .build();
        searchResult = notesService.searchArchivedByExternalGuid(querySearchArchived_thread1_2);
        validateAll(searchResult, 2, 4, 1, 1);

        // Search archived by external entry with no histories
        SearchArchivedByExternalGuid querySearchArchived_thread1_2_no_histories = SearchArchivedByExternalGuid.builder()
                .searchGuid(thread1_2.getExternalGuid().toString())
                .includeVersions(false)
                .build();
        searchResult = notesService.searchArchivedByExternalGuid(querySearchArchived_thread1_2_no_histories);
        validateAll(searchResult, 2, 3, 1, 0);

        // Search archived by entry test 1
        SearchArchivedByEntryGuid thread1_query = SearchArchivedByEntryGuid.builder()
                .searchGuid(thread1.getEntryGuid().toString())
                .includeVersions(true)
                .build();
        searchResult = notesService.searchArchivedByEntryGuid(thread1_query);
        validateAll(searchResult, 1, 2, 0, 1);

        // Search archived by entry test 2
        SearchArchivedByEntryGuid thread1_2_query = SearchArchivedByEntryGuid.builder()
                .searchGuid(thread1_2.getEntryGuid().toString())
                .includeVersions(false)
                .build();
        searchResult = notesService.searchArchivedByEntryGuid(thread1_2_query);
        validateAll(searchResult, 1, 1, 0, 0);

        // Create Thread 1-1-1
        createThread(thread1_1,"New External Entry-Thread-1-1-1");
        searchResult = notesAdminService.searchByExternalGuid(SearchByExternalGuid.builder()
                .searchGuid(newEntryCreated.getExternalGuid().toString())
                .includeVersions(true)
                .includeArchived(true)
                .build());
        validateAll(searchResult, 1, 12, 9, 2);

        // Archive another entry (total two different threads archived), expected multiple results
        IQuery archive_1_1 = SearchByEntryGuid.builder()
                .searchGuid(thread1_1.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(false)
                .build();
        notesService.archive(archive_1_1);

        // Search archived. by external guid
        SearchArchivedByExternalGuid querySearchArchivedByExternal = SearchArchivedByExternalGuid.builder()
                .searchGuid(newEntryCreated.getExternalGuid().toString())
                .includeVersions(true)
                .build();
        searchResult = notesService.searchArchivedByExternalGuid(querySearchArchivedByExternal);
        validateAll(searchResult, 3, 6, 2, 1);

        // Search archived. By entry guid
        SearchArchivedByEntryGuid querySearchArchivedByEntry = SearchArchivedByEntryGuid.builder()
                .searchGuid(newEntryCreated.getEntryGuid().toString())
                .includeVersions(true)
                .build();
        searchResult = notesService.searchArchivedByEntryGuid(querySearchArchivedByEntry);
        validateAll(searchResult, 3, 6, 2, 1);

        // Search multiple threads archived. both may have further threads. by entry guid
        SearchArchivedByEntryGuid querySearchArchived_thread1 = SearchArchivedByEntryGuid.builder()
                .searchGuid(thread1.getEntryGuid().toString())
                .includeVersions(true)
                .build();
        searchResult = notesService.searchArchivedByEntryGuid(querySearchArchived_thread1);
        validateAll(searchResult, 2, 4, 1, 1);

        List<NotesData> resultDelete = notesService.deleteArchivedByEntryGuid(SearchArchivedByEntryGuid.builder()
                .searchGuid(thread1_1.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(true)
                .build());
        validateAll(resultDelete, 2, 2, 0, 0);
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 1, 10, 7, 2);

        // Archive another entry (total two different threads archived), expected multiple results
        SearchByEntryGuid query_thread1 = SearchByEntryGuid.builder()
                .searchGuid(thread1.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(true)
                .build();
        searchResult = notesService.searchByEntryGuid(query_thread1);
        validateAll(searchResult, 1, 4, 1, 2);
        notesService.archive(query_thread1);

        // Select one archived entry of many threads on the same layer. and some other is also archived
        IQuery archive_thread3 = SearchByEntryGuid.builder()
                .searchGuid(thread3.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(false)
                .build();
        notesService.archive(archive_thread3);
        SearchArchivedByEntryGuid search_archived_thread1 = SearchArchivedByEntryGuid.builder()
                .searchGuid(thread1.getEntryGuid().toString())
                .includeVersions(true)
                .build();
        searchResult = notesService.searchArchivedByEntryGuid(search_archived_thread1);
        validateAll(searchResult, 1, 4, 1, 2);

        // Search multiple threads archived. both may have further threads. by externalGuid
        searchResult = notesService.searchArchivedByExternalGuid(querySearchArchivedByExternal);
        validateAll(searchResult, 3, 7, 2, 2);

        // Search multiple threads archived. both may have further threads. by externalGuid
        searchResult = notesService.searchArchivedByEntryGuid(querySearchArchivedByEntry);
        validateAll(searchResult, 3, 7, 2, 2);

        // Create another external entry with same externalGuid
        createNewEntry(NotesData.builder()
                .externalGuid(newEntryCreated.getExternalGuid())
                .content("New External Entry - 2")
                .build());
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 2, 11, 7, 2);

        SearchArchivedByEntryGuid queryArchivedRootEntry = SearchArchivedByEntryGuid.builder()
                .searchGuid(newEntryCreated.getEntryGuid().toString())
                .includeVersions(true)
                .build();
        resultDelete = notesService.deleteArchivedByEntryGuid(queryArchivedRootEntry);
        checkDuplicates(resultDelete,new HashSet<>());
        assertEquals(7,resultDelete.size());

        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 2, 4, 2, 0);
        NotesData thread_4_1 = createThread(thread4, "New External Entry-Thread-4-1");
        searchResult = notesService.archive(SearchByEntryGuid.builder()
                .searchGuid(thread_4_1.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(false)
                .build());
        validateAll(searchResult, 1, 1, 0, 0);
        searchResult = notesService.archive(SearchByEntryGuid.builder()
                .searchGuid(thread2.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(false)
                .build());
        validateAll(searchResult, 1, 1, 0, 0);
        searchResult = notesService.searchArchivedByEntryGuid(queryArchivedRootEntry);
        validateAll(searchResult, 2, 2, 0, 0);

        // This make sure it archives entire entry. where some thread entry have already been archived.
        // So when searched results should contain one entry with all threads
        searchResult = notesService.archive(SearchByEntryGuid.builder()
                .searchGuid(newEntryCreated.getEntryGuid().toString())
                .includeVersions(true)
                .includeArchived(false)
                .build());
        validateAll(searchResult, 4, 4, 0, 0);
        searchResult = notesService.searchArchivedByEntryGuid(queryArchivedRootEntry);
        validateAll(searchResult, 4, 4, 0, 0);
        resultDelete = notesService.deleteArchivedByEntryGuid(queryArchivedRootEntry);
        validateAll(resultDelete, 4, 4, 0, 0);
        
        // Delete
        resultDelete = notesAdminService.deleteByExternalGuid(UUID.fromString(querySearchByExternalGuid.getSearchGuid()));
        validateAll(resultDelete, 1, 1, 0, 0);
        searchResult = notesAdminService.searchByExternalGuid(querySearchByExternalGuid);
        validateAll(searchResult, 0, 0, 0, 0);
    }


    @BeforeAll
    void createEntries() throws JSONException {
        JSONArray jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
        for (int i = 0; i < jsonArray.length(); i++) {
            List<NotesData> entries = new ArrayList<>();
            String jsonStringToStore = jsonArray.getString(i);
            flatten(NotesData.fromJson(jsonStringToStore), entries);
            entries.forEach(e -> {
                if (e.getThreads() != null)
                    e.getThreads().clear();
                if (e.getHistory() != null)
                    e.getHistory().clear();
                NotesData notesData = repository.save(e);
                assertEquals(notesData.getGuid(), e.getGuid());
                assertEquals(notesData.getExternalGuid(), e.getExternalGuid());
                assertEquals(notesData.getEntryGuid(), e.getEntryGuid());
                assertEquals(notesData.getThreadGuid(), e.getThreadGuid());
                assertEquals(notesData.getEntryGuidParent(), e.getEntryGuidParent());
                assertEquals(notesData.getContent(), e.getContent());
                assertEquals(notesData.getCreated(), e.getCreated());
                assertEquals(notesData.getArchived(), e.getArchived());
                assertEquals(notesData.getThreads(), e.getThreads());
                assertEquals(notesData.getHistory(), e.getHistory());
            });
        }
    }

    @AfterAll
    void deleteEntries() throws JSONException {
        JSONArray jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
        for (int i = 0; i < jsonArray.length(); i++) {
            List<NotesData> entries = new ArrayList<>();
            String jsonStringToStore = jsonArray.getString(i);
            flatten(NotesData.fromJson(jsonStringToStore),entries);
            entries.forEach(e -> {
                if (e.getThreads() != null)
                    e.getThreads().clear();
                if (e.getHistory() != null)
                    e.getHistory().clear();
                repository.delete(e);
            });
        }
    }


    @Test
    void getByExternalGuid_all() {
        List<NotesData> result = notesAdminService.searchByExternalGuid(SearchByExternalGuid.builder()
                .searchGuid("10a14259-ca84-4c7d-8d46-7ad398000002")
                .includeVersions(true)
                .includeArchived(true)
                .build());
        validateAll(result, 3, 11, 6, 2);
    }

    @Test
    void getByExternalGuid_noHistories() {
        List<NotesData> result = notesAdminService.searchByExternalGuid(SearchByExternalGuid.builder()
                .searchGuid("10a14259-ca84-4c7d-8d46-7ad398000002")
                .includeVersions(false)
                .includeArchived(true)
                .build());
        validateAll(result, 3, 9, 6, 0);
    }

    @Test
    void getByExternalGuid_noArchive() {
        List<NotesData> result = notesAdminService.searchByExternalGuid(SearchByExternalGuid.builder()
                .searchGuid("10a14259-ca84-4c7d-8d46-7ad398000002")
                .includeVersions(true)
                .includeArchived(false)
                .build());
        validateAll(result, 3, 7, 3, 1);
    }

    @Test
    void getByExternalGuid_noHistoryAndArchives() {
        List<NotesData> result = notesAdminService.searchByExternalGuid(SearchByExternalGuid.builder()
                .searchGuid("10a14259-ca84-4c7d-8d46-7ad398000002")
                .includeVersions(false)
                .includeArchived(false)
                .build());
        validateAll(result, 3, 6, 3, 0);
    }

    @Test
    void searchContent() {
        List<NotesData> result = notesService.searchByContent(SearchByContent.builder()
                .contentToSearch("content")
                .includeVersions(true)
                .includeArchived(true)
                .build());
        checkDuplicates(result,new HashSet<>());
        assertEquals(11, result.size());
    }

    @Test
    void getByEntryGuid_all() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .includeVersions(true)
                .includeArchived(true)
                .build());
        validateAll(result, 1, 4, 2, 1);
    }

    @Test
    void getByEntryGuid_all_test_1() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .includeVersions(true)
                .includeArchived(true)
                .build());
        validateAll(result, 1, 8, 5, 2);
    }

    @Test
    void getByEntryGuid_all_test_2() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .includeVersions(true)
                .includeArchived(true)
                .build());
        validateAll(result, 1, 6, 4, 1);
    }

    @Test
    void getByEntryGuid_noHistories() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .includeVersions(false)
                .includeArchived(true)
                .build());
        validateAll(result, 1, 3, 2, 0);
    }

    @Test
    void getByEntryGuid_noHistories_test_1() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .includeVersions(false)
                .includeArchived(true)
                .build());
        validateAll(result, 1, 6, 5, 0);
    }

    @Test
    void getByEntryGuid_noArchived_test_1() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .includeVersions(true)
                .includeArchived(false)
                .build());
        assertEquals(0, result.size());
    }

    @Test
    void getByEntryGuid_noArchived_test_2() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("16b8d331-92ab-424b-b69a-3181f6d80f5a")
                .includeVersions(true)
                .includeArchived(false)
                .build());
        validateAll(result, 1, 1, 0, 0);
    }

    @Test
    void getByEntryGuid_noArchived_test_3() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .includeVersions(true)
                .includeArchived(false)
                .build());
        validateAll(result, 1, 4, 2, 1);
    }

    @Test
    void getByEntryGuid_NoHistoriesAndArchives_test_1() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .includeVersions(false)
                .includeArchived(false)
                .build());
        validateAll(result, 0, 0, 0, 0);
    }

    @Test
    void getByEntryGuid_NoHistoriesAndArchives_test_2() {
        List<NotesData> result = notesService.searchByEntryGuid(SearchByEntryGuid.builder()
                .searchGuid("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .includeVersions(false)
                .includeArchived(false)
                .build());
        validateAll(result, 1, 3, 2, 0);
    }

}
