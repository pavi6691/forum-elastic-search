package com.freelance.forum;

import com.freelance.forum.base.BaseTest;
import com.freelance.forum.data.ElasticSearchData;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.*;
import com.freelance.forum.elasticsearch.queries.generics.enums.Entries;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntegrationTest extends BaseTest {

// TODO	
//		@Value("${index.name}")
//		private String indexName;
//		private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:6.8.12";
//		@Container
//		public static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(ELASTICSEARCH_IMAGE);
//		@BeforeAll
//		void setup() {
//			elasticsearchContainer.setWaitStrategy((new LogMessageWaitStrategy())
//					.withRegEx(".*(\"message\":\\s?\"started[\\s?|\"].*|] started\n$)")
//					.withStartupTimeout(Duration.ofSeconds(180L)));
//			elasticsearchContainer.start();
//			assertEquals(notesService.createIndex(indexName),indexName);
//		}

    @Test
    void crud() {
        NotesData newEntryCreated = createNewEntry(new NotesData.Builder().setExternalGuid(UUID.randomUUID()).setContent("New External Entry - 1").build());

        IQuery querySearchByExternalGuid = new SearchByExternalGuid().setSearchBy(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true);
        List<NotesData> searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 1, 0, 0);

        // create Thread 1
        NotesData thread1 = createThread(newEntryCreated,"New External Entry-Thread-1");
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult, 1,2, 1, 0);

        // create Thread 2
        createThread(newEntryCreated,"New External Entry-Thread-2");
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 3, 2, 0);

        // create Thread 3
        createThread(newEntryCreated,"New External Entry-Thread-3");
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 4, 3, 0);

        // create Thread 4
        createThread(newEntryCreated,"New External Entry-Thread-4");
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 5, 4, 0);

        // create Thread 1-1
        NotesData thread1_1 = createThread(thread1,"New External Entry-Thread-1-1");
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 6, 5, 0);

        // update guid 1
        updateGuid(thread1,"New External Entry-Thread-1-Updated");
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 7, 5, 1);

        // create Thread 1-2
        NotesData thread1_2 = createThread(thread1,"New External Entry-Thread-1-2");
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 8, 6, 1);

        // update by entryGuid thread 1-2
        thread1.setGuid(null); // if guid is null, entry will be updated by entryGuid
        updateGuid(thread1_2,"New External Entry-Thread-1-2-Updated");
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 9, 6, 2);


        // Search by EntryId
        IQuery querySearchByEntryGuid = new SearchByEntryGuid().setSearchBy(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true);

        searchResult = notesService.search(querySearchByEntryGuid);
        validateAll(searchResult,1, 5, 2, 2);


        IQuery searchBy_Thread_1_Entry = new SearchByEntryGuid().setSearchBy(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(false).setGetArchived(true);

        searchResult = notesService.search(searchBy_Thread_1_Entry);
        validateAll(searchResult,1, 3, 2, 0);

        searchBy_Thread_1_Entry = new SearchByEntryGuid().setSearchBy(thread1_2.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(false);
        notesService.archive(searchBy_Thread_1_Entry);

        // search archived by external entry test 1
        IQuery querySearchArchived = new SearchArchivedByExternalGuid().setExternalGuid(thread1_2.getExternalGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,1,2,0,1);

        // search archived by external entry test 2
        querySearchArchived = new SearchArchivedByExternalGuid().setExternalGuid(thread1_2.getExternalGuid().toString())
                .setGetUpdateHistory(false);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,1,1,0,0);

        // search archived by entry test 1
        querySearchArchived = new SearchArchivedByEntryGuid().setEntryGuid(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,1,2,0,1);

        // search archived by entry test 2
        querySearchArchived = new SearchArchivedByEntryGuid().setEntryGuid(thread1_2.getEntryGuid().toString())
                .setGetUpdateHistory(false);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,1,1,0,0);


        // create Thread 1-1-1
        createThread(thread1_1,"New External Entry-Thread-1-1-1");
        searchResult = notesService.search(new SearchByExternalGuid().setSearchBy(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true));
        validateAll(searchResult,1, 10, 7, 2);

        // Archive another entry (total two different threads archived), expected multiple results
        IQuery archive_1_1 = new SearchByEntryGuid().setSearchBy(thread1_1.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(false);
        notesService.archive(archive_1_1);

        // search archived. by external guid
        querySearchArchived = new SearchArchivedByExternalGuid().setExternalGuid(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,2,4,1,1);

        // search archived. By entry guid
        querySearchArchived = new SearchArchivedByEntryGuid().setEntryGuid(newEntryCreated.getEntryGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,2,4,1,1);

        // search multiple threads archived. both may have further threads. by entry guid
        querySearchArchived = new SearchArchivedByEntryGuid().setEntryGuid(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,2,4,1,1);

        List<NotesData> resultDelete = notesService.delete(new SearchByEntryGuid().setSearchBy(thread1_1.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true), Entries.ARCHIVED);
        validateAll(resultDelete,1, 2, 1, 0);
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,1, 8, 5, 2);

        // Archive another entry (total two different threads archived), expected multiple results
        IQuery archive_thread1 = new SearchByEntryGuid().setSearchBy(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(false);
        notesService.archive(archive_thread1);

        // search multiple threads archived. both may have further threads. by External Guid
        querySearchArchived = new SearchArchivedByExternalGuid().setExternalGuid(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,1,4,1,2);

        // search multiple threads archived. both may have further threads. by External Guid
        querySearchArchived = new SearchArchivedByEntryGuid().setEntryGuid(newEntryCreated.getEntryGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(querySearchArchived);
        validateAll(searchResult,1,4,1,2);


        // crate Another ExternalEntry with same externalId
        createNewEntry(new NotesData.Builder().setExternalGuid(newEntryCreated.getExternalGuid())
                .setContent("New External Entry - 2").build());
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,2, 9, 5, 2);

        resultDelete = notesService.delete(new SearchArchivedByExternalGuid().setExternalGuid(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true), Entries.ARCHIVED);
        validateAll(resultDelete,1, 4, 1, 2);
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,2, 5, 3, 0);
        
        // delete
        resultDelete = notesService.delete(querySearchByExternalGuid, Entries.ALL);
        validateAll(resultDelete,2, 5, 3, 0);
        searchResult = notesService.search(querySearchByExternalGuid);
        validateAll(searchResult,0, 0, 0, 0);
    }


    @BeforeAll
    void createEntries() throws JSONException {
        JSONArray jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
        for(int i=0; i< jsonArray.length(); i++) {
            List<NotesData> entries = new ArrayList<>();
            String jsonStringToStore = jsonArray.getString(i);
            flatten(NotesData.fromJson(jsonStringToStore),entries);
            entries.forEach(e -> {
                if(e.getThreads() != null)
                    e.getThreads().clear();
                if(e.getHistory() != null)
                    e.getHistory().clear();
                NotesData notesData = repository.save(e);
                assertEquals(notesData.getGuid(),e.getGuid());
                assertEquals(notesData.getExternalGuid(),e.getExternalGuid());
                assertEquals(notesData.getEntryGuid(),e.getEntryGuid());
                assertEquals(notesData.getThreadGuid(),e.getThreadGuid());
                assertEquals(notesData.getThreadGuidParent(),e.getThreadGuidParent());
                assertEquals(notesData.getContent(),e.getContent());
                assertEquals(notesData.getCreated(),e.getCreated());
                assertEquals(notesData.getArchived(),e.getArchived());
                assertEquals(notesData.getThreads(),e.getThreads());
                assertEquals(notesData.getHistory(),e.getHistory());
            });
        }
    }

    @AfterAll
    void deleteEntries() throws JSONException {
        JSONArray jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
        for(int i=0; i< jsonArray.length(); i++) {
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
        List<NotesData> result = notesService.search(new SearchByExternalGuid().setSearchBy("10a14259-ca84-4c7d-8d46-7ad398000002")
                .setGetUpdateHistory(true).setGetArchived(true));
        validateAll(result,3,11,6,2);
    }

    @Test
    void getByExternalGuid_noHistories() {
        List<NotesData> result = notesService.search(new SearchByExternalGuid().setSearchBy("10a14259-ca84-4c7d-8d46-7ad398000002")
                .setGetUpdateHistory(false).setGetArchived(true));
        validateAll(result,3,9,6,0);
    }

    @Test
    void getByExternalGuid_noArchive() {
        List<NotesData> result = notesService.search(new SearchByExternalGuid().setSearchBy("10a14259-ca84-4c7d-8d46-7ad398000002")
                .setGetUpdateHistory(true).setGetArchived(false));
        validateAll(result,3,7,3,1);
    }

    @Test
    void getByExternalGuid_noHistoryAndArchives() {
        List<NotesData> result = notesService.search(new SearchByExternalGuid().setSearchBy("10a14259-ca84-4c7d-8d46-7ad398000002")
                .setGetUpdateHistory(false).setGetArchived(false));
        validateAll(result,3,6,3,0);
    }

    @Test
    void searchContent() {
        List<NotesData> result = notesService.search(new SearchByContent().setContentToSearch("content")
                .setGetUpdateHistory(true).setGetArchived(true));
        checkDuplicates(result);
        validateAll(result,3,11,6,2);
    }

    @Test
    void getByEntryGuid_all() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .setGetUpdateHistory(true).setGetArchived(true));
        validateAll(result,1,4,2,1);
    }

    @Test
    void getByEntryGuid_all_test_1() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .setGetUpdateHistory(true).setGetArchived(true));
        validateAll(result,1,8,5,2);
    }

    @Test
    void getByEntryGuid_all_test_2() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("06a418c3-7475-473e-9e9d-3e952d672d4c")
                .setGetUpdateHistory(true).setGetArchived(true));
        validateAll(result,1,6,4,1);
    }

    @Test
    void getByEntryGuid_noHistories() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .setGetUpdateHistory(false).setGetArchived(true));

        validateAll(result,1,3,2,0);
    }

    @Test
    void getByEntryGuid_noHistories_test_1() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .setGetUpdateHistory(false).setGetArchived(true));

        validateAll(result,1,6,5,0);
    }

    @Test
    void getByEntryGuid_noArchived_test_1() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .setGetUpdateHistory(true).setGetArchived(false));
        assertEquals(0,result.size());
    }

    @Test
    void getByEntryGuid_noArchived_test_2() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("16b8d331-92ab-424b-b69a-3181f6d80f5a")
                .setGetUpdateHistory(true).setGetArchived(false));
        validateAll(result,1,1,0,0);
    }

    @Test
    void getByEntryGuid_noArchived_test_3() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .setGetUpdateHistory(true).setGetArchived(false));
        validateAll(result,1,4,2,1);
    }

    @Test
    void getByEntryGuid_NoHistoriesAndArchives_test_1() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("7f20d0eb-3907-4647-9584-3d7814cd3a55")
                .setGetUpdateHistory(false).setGetArchived(false));
        validateAll(result,0,0,0,0);
    }

    @Test
    void getByEntryGuid_NoHistoriesAndArchives_test_2() {
        List<NotesData> result = notesService.search(new SearchByEntryGuid().setSearchBy("ba7a0762-935d-43f3-acb0-c33d86e7f350")
                .setGetUpdateHistory(false).setGetArchived(false));
        validateAll(result,1,3,2,0);
    }
}
