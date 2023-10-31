package com.freelance.forum;

import com.freelance.forum.base.BaseTest;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.SearchArchivedByEntryGuid;
import com.freelance.forum.elasticsearch.queries.SearchArchivedByExternalGuid;
import com.freelance.forum.elasticsearch.queries.SearchByEntryGuid;
import com.freelance.forum.elasticsearch.queries.SearchByExternalGuid;
import com.freelance.forum.elasticsearch.queries.generics.IQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

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
    void test() {
        NotesData newEntryCreated = createNewEntry(new NotesData.Builder().setExternalGuid(UUID.randomUUID()).setContent("New External Entry - 1").build());

        IQuery query = new SearchByExternalGuid().setSearchBy(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true);
        List<NotesData> searchResult = notesService.search(query);
        validateAll(searchResult,1, 1, 0, 0);

        // create Thread 1
        NotesData thread1 = createThread(newEntryCreated,"New External Entry-Thread-1");
        searchResult = notesService.search(query);
        validateAll(searchResult, 1,2, 1, 0);

        // create Thread 2
        createThread(newEntryCreated,"New External Entry-Thread-2");
        searchResult = notesService.search(query);
        validateAll(searchResult,1, 3, 2, 0);

        // create Thread 3
        createThread(newEntryCreated,"New External Entry-Thread-3");
        searchResult = notesService.search(query);
        validateAll(searchResult,1, 4, 3, 0);

        // create Thread 4
        createThread(newEntryCreated,"New External Entry-Thread-4");
        searchResult = notesService.search(query);
        validateAll(searchResult,1, 5, 4, 0);

        // create Thread 1-1
        NotesData thread1_1 = createThread(thread1,"New External Entry-Thread-1-1");
        searchResult = notesService.search(query);
        validateAll(searchResult,1, 6, 5, 0);

        // update guid 1
        updateGuid(thread1,"New External Entry-Thread-1-Updated");
        searchResult = notesService.search(query);
        validateAll(searchResult,1, 7, 5, 1);

        // create Thread 1-2
        NotesData thread1_2 = createThread(thread1,"New External Entry-Thread-1-2");
        searchResult = notesService.search(query);
        validateAll(searchResult,1, 8, 6, 1);

        // update by entryGuid thread 1-2
        thread1.setGuid(null); // if guid is null, entry will be updated by entryGuid
        updateGuid(thread1_2,"New External Entry-Thread-1-2-Updated");
        searchResult = notesService.search(query);
        validateAll(searchResult,1, 9, 6, 2);


        // Search by EntryId
        IQuery entryQuery = new SearchByEntryGuid().setSearchBy(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true);

        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,1, 5, 2, 2);


        entryQuery = new SearchByEntryGuid().setSearchBy(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(false).setGetArchived(true);

        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,1, 3, 2, 0);

        entryQuery = new SearchByEntryGuid().setSearchBy(thread1_2.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true);
        notesService.archive(entryQuery);

        // search archived by external entry test 1
        entryQuery = new SearchArchivedByExternalGuid().setExternalGuid(thread1_2.getExternalGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,1,2,0,1);

        // search archived by external entry test 2
        entryQuery = new SearchArchivedByExternalGuid().setExternalGuid(thread1_2.getExternalGuid().toString())
                .setGetUpdateHistory(false);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,1,1,0,0);

        // search archived by entry test 1
        entryQuery = new SearchArchivedByEntryGuid().setEntryGuid(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,1,2,0,1);

        // search archived by entry test 2
        entryQuery = new SearchArchivedByEntryGuid().setEntryGuid(thread1_2.getEntryGuid().toString())
                .setGetUpdateHistory(false);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,1,1,0,0);


        // create Thread 1-1-1
        createThread(thread1_1,"New External Entry-Thread-1-1-1");
        searchResult = notesService.search(new SearchByExternalGuid().setSearchBy(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true));
        validateAll(searchResult,1, 10, 7, 2);

        // Archive another entry (total two different threads archived), expected multiple results
        IQuery archive = new SearchByEntryGuid().setSearchBy(thread1_1.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true);
        notesService.archive(archive);

        // search archived. by external guid
        entryQuery = new SearchArchivedByExternalGuid().setExternalGuid(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,2,4,1,1);

        // search archived. By entry guid
        entryQuery = new SearchArchivedByEntryGuid().setEntryGuid(newEntryCreated.getEntryGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,2,4,1,1);

        // search multiple threads archived. both may have further threads. by entry guid
        entryQuery = new SearchArchivedByEntryGuid().setEntryGuid(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,2,4,1,1);

        // Archive another entry (total two different threads archived), expected multiple results
        archive = new SearchByEntryGuid().setSearchBy(thread1.getEntryGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true);
        notesService.archive(archive);

        // search multiple threads archived. both may have further threads. by External Guid
        entryQuery = new SearchArchivedByExternalGuid().setExternalGuid(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,1,6,3,2);

        // search multiple threads archived. both may have further threads. by External Guid
        entryQuery = new SearchArchivedByEntryGuid().setEntryGuid(newEntryCreated.getEntryGuid().toString())
                .setGetUpdateHistory(true);
        searchResult = notesService.search(entryQuery);
        validateAll(searchResult,1,6,3,2);


        // crate Another ExternalEntry with same externalId
        createNewEntry(new NotesData.Builder().setExternalGuid(newEntryCreated.getExternalGuid())
                .setContent("New External Entry - 2").build());

        query = new SearchByExternalGuid().setSearchBy(newEntryCreated.getExternalGuid().toString())
                .setGetUpdateHistory(true).setGetArchived(true);
        searchResult = notesService.search(query);
        validateAll(searchResult,2, 11, 7, 2);

        // delete
        notesService.delete(query,"all");
        searchResult = notesService.search(query);
    }
}
