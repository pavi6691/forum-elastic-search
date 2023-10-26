package com.freelance.forum;

import com.freelance.forum.data.ElasticSearchData;
import com.freelance.forum.elasticsearch.configuration.EsConfig;
import com.freelance.forum.elasticsearch.esrepo.ESNotesRepository;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.queries.IQuery;
import com.freelance.forum.elasticsearch.queries.Queries;
import com.freelance.forum.elasticsearch.queries.RequestType;
import com.freelance.forum.service.INotesService;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EsNotesServiceTest {


	@Autowired
	INotesService notesService;

	@Autowired
	ESNotesRepository repository;

	@Value("${index.name}")
	private String indexName;

	@Value("${elasticsearch.host}")
	private String esHost;

	private static final String ELASTICSEARCH_IMAGE =
			"docker.elastic.co/elasticsearch/elasticsearch:6.8.12";
//	@Container
//	private static final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
//			.withEnv("discovery.type", "single-node")
//			.withExposedPorts(9200);

	EsConfig esConfig;

	@BeforeAll
	void setup() throws JSONException, IOException {
//		elasticsearchContainer.setWaitStrategy((new LogMessageWaitStrategy())
//				.withRegEx(".*(\"message\":\\s?\"started[\\s?|\"].*|] started\n$)")
//				.withStartupTimeout(Duration.ofSeconds(180L)));
//		elasticsearchContainer.start();
//
//		String hostAndPort = elasticsearchContainer.getHttpHostAddress();
//		final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//				.connectedTo(hostAndPort)
//				.build();
//		RestHighLevelClient restHighLevelClient = RestClients.create(clientConfiguration).rest();
//
//		esConfig = Mockito.mock(EsConfig.class);
//		Mockito.when(esConfig.elasticsearchClient()).thenReturn(restHighLevelClient);
//		assertEquals(service.createIndex(indexName),indexName);

	}



	@Test
	void testEverything() {
		NotesData newEntryCreated = createNewEntry(new NotesData.Builder().setExternalGuid(UUID.randomUUID()).setContent("New External Entry").build());

		IQuery query = new Queries.SearchByExternalGuid().setExternalGuid(newEntryCreated.getExternalGuid().toString())
				.setGetUpdateHistory(true).setGetArchived(true);
		
		List<NotesData> searchResult = notesService.search(query);
		validateAll(searchResult, 1, 0, 0);

		// create Thread 1
		NotesData thread1 = createThread(newEntryCreated,"New External Entry-Thread-1");
		searchResult = notesService.search(query);
		validateAll(searchResult, 2, 1, 0);

		// create Thread 2
		createThread(newEntryCreated,"New External Entry-Thread-2");
		searchResult = notesService.search(query);
		validateAll(searchResult, 3, 2, 0);

		// create Thread 3
		createThread(newEntryCreated,"New External Entry-Thread-3");
		searchResult = notesService.search(query);
		validateAll(searchResult, 4, 3, 0);

		// create Thread 4
		createThread(newEntryCreated,"New External Entry-Thread-4");
		searchResult = notesService.search(query);
		validateAll(searchResult, 5, 4, 0);

		// create Thread 1-1
		createThread(thread1,"New External Entry-Thread-1-1");
		searchResult = notesService.search(query);
		validateAll(searchResult, 6, 5, 0);

		// update guid 1
		updateGuid(thread1,"New External Entry-Thread-1-Updated");
		searchResult = notesService.search(query);
		validateAll(searchResult, 7, 5, 1);

		// create Thread 1-2
		NotesData thread1_2 = createThread(thread1,"New External Entry-Thread-1-2");
		searchResult = notesService.search(query);
		validateAll(searchResult, 8, 6, 1);

		// update by entryGuid thread 1-2
		thread1.setGuid(null); // if guid is null, entry will be updated by entryGuid
		updateGuid(thread1_2,"New External Entry-Thread-1-2-Updated");
		searchResult = notesService.search(query);
		validateAll(searchResult, 9, 6, 2);


		// Search by EntryId
		IQuery entryQuery = new Queries.SearchByEntryGuid().setEntryGuid(thread1.getEntryGuid().toString())
				.setSearchField(ESIndexNotesFields.ENTRY).setGetUpdateHistory(true).setGetArchived(true);
		
		searchResult = notesService.search(entryQuery);
		validateAll(searchResult, 5, 2, 2);


		entryQuery = new Queries.SearchByEntryGuid().setEntryGuid(thread1.getEntryGuid().toString())
				.setSearchField(ESIndexNotesFields.ENTRY).setGetUpdateHistory(false).setGetArchived(true);
		
		searchResult = notesService.search(entryQuery);
		validateAll(searchResult, 3, 2, 0);

		entryQuery = new Queries.SearchByEntryGuid().setEntryGuid(thread1_2.getEntryGuid().toString())
				.setSearchField(ESIndexNotesFields.ENTRY).setGetUpdateHistory(false).setGetArchived(false);
		notesService.archive(entryQuery);
		
		// search archived by external entry test 1
		entryQuery = new Queries.SearchByExternalGuid().setExternalGuid(thread1_2.getExternalGuid().toString())
				.setGetUpdateHistory(true).setGetArchived(true).setRequestType(RequestType.ARCHIVE);
		searchResult = notesService.search(entryQuery);
		validateAll(searchResult,2,0,1);

		// search archived by external entry test 2
		entryQuery = new Queries.SearchByExternalGuid().setExternalGuid(thread1_2.getExternalGuid().toString())
				.setGetUpdateHistory(false).setGetArchived(true).setRequestType(RequestType.ARCHIVE);
		searchResult = notesService.search(entryQuery);
		validateAll(searchResult,1,0,0);

		// search archived by external entry test 3
		entryQuery = new Queries.SearchByExternalGuid().setExternalGuid(thread1_2.getExternalGuid().toString())
				.setGetUpdateHistory(false).setGetArchived(false).setRequestType(RequestType.ARCHIVE);
		searchResult = notesService.search(entryQuery);
		validateAll(searchResult,0,0,0);

		// search archived by entry test 1
		entryQuery = new Queries.SearchByEntryGuid().setEntryGuid(thread1.getEntryGuid().toString())
				.setRequestType(RequestType.ARCHIVE).setSearchField(ESIndexNotesFields.ENTRY).setGetUpdateHistory(true).setGetArchived(true);
		searchResult = notesService.search(entryQuery);
		validateAll(searchResult,2,0,1);

		// search archived by entry test 2
		entryQuery = new Queries.SearchByEntryGuid().setEntryGuid(thread1_2.getEntryGuid().toString())
				.setRequestType(RequestType.ARCHIVE).setSearchField(ESIndexNotesFields.ENTRY).setGetUpdateHistory(false).setGetArchived(true);
		searchResult = notesService.search(entryQuery);
		searchResult = notesService.search(entryQuery);
		validateAll(searchResult,1,0,0);

		// search archived by entry test 3
		entryQuery = new Queries.SearchByEntryGuid().setEntryGuid(thread1_2.getEntryGuid().toString())
				.setRequestType(RequestType.ARCHIVE).setSearchField(ESIndexNotesFields.ENTRY).setGetUpdateHistory(false).setGetArchived(false);
		searchResult = notesService.search(entryQuery);
		searchResult = notesService.search(entryQuery);
		validateAll(searchResult,0,0,0);

		// delete
		notesService.delete(query,"all");
		searchResult = notesService.search(query);
		validateAll(searchResult,0,0,0);
	}
	
	private NotesData createNewEntry(NotesData newExternalEntry) {
		NotesData entryCreated = notesService.saveNew(newExternalEntry);
		assertEquals(newExternalEntry.getExternalGuid(), entryCreated.getExternalGuid());
		assertEquals(newExternalEntry.getContent(), entryCreated.getContent());
		assertEquals(null, entryCreated.getHistory());
		assertEquals(null, entryCreated.getThreads());
		assertEquals(null, entryCreated.getThreadGuidParent());
		assertEquals(null, entryCreated.getArchived());
		assertNotEquals(null, entryCreated.getEntryGuid());
		assertNotEquals(null, entryCreated.getCreated());
		assertNotEquals(null, entryCreated.getThreadGuid());
		return entryCreated;
	}


	private NotesData createThread(NotesData existingEntry, String content) {
		NotesData newThread = new NotesData();
		newThread.setContent(content);
		newThread.setThreadGuidParent(existingEntry.getThreadGuid());
		NotesData newThreadCreated = notesService.saveNew(newThread);
		assertEquals(newThread.getExternalGuid(), newThreadCreated.getExternalGuid());
		assertEquals(null, newThreadCreated.getHistory());
		assertEquals(null, newThreadCreated.getThreads());
		assertEquals(null, newThreadCreated.getArchived());
		assertEquals(existingEntry.getThreadGuid(), newThreadCreated.getThreadGuidParent());
		assertNotEquals(existingEntry.getEntryGuid(), newThreadCreated.getEntryGuid());
		assertNotEquals(existingEntry.getCreated(), newThreadCreated.getCreated());
		assertNotEquals(existingEntry.getContent(), newThreadCreated.getContent());
		assertNotEquals(existingEntry.getThreadGuid(), newThreadCreated.getThreadGuid());
		return newThreadCreated;
	}

	private NotesData updateGuid(NotesData existingEntry, String content) {
		NotesData newEntry = new NotesData();
		newEntry.setGuid(existingEntry.getGuid());
		newEntry.setContent(content);
		newEntry.setEntryGuid(existingEntry.getEntryGuid());
		NotesData newThreadUpdated = notesService.update(newEntry);
		assertEquals(existingEntry.getExternalGuid(), newThreadUpdated.getExternalGuid());
		assertEquals(existingEntry.getEntryGuid(), newThreadUpdated.getEntryGuid());
		assertEquals(existingEntry.getThreadGuid(), newThreadUpdated.getThreadGuid());
		assertEquals(existingEntry.getThreadGuidParent(), newThreadUpdated.getThreadGuidParent());
		assertNotEquals(existingEntry.getCreated(), newThreadUpdated.getCreated());
		assertNotEquals(existingEntry.getContent(), newThreadUpdated.getContent());
		return newThreadUpdated;
	}

	@Test
	void getByExternalGuid_all() {
		List<NotesData> result = notesService.search(new Queries.SearchByExternalGuid().setExternalGuid("10a14259-ca84-4c7d-8d46-7ad398000002")
				.setGetUpdateHistory(true).setGetArchived(true));
		validateAll(result,11,6,2);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void getByExternalGuid_noHistories() {
		List<NotesData> result = notesService.search(new Queries.SearchByExternalGuid().setExternalGuid("10a14259-ca84-4c7d-8d46-7ad398000002")
				.setGetUpdateHistory(false).setGetArchived(true));
		validateAll(result,9,6,0);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void getByExternalGuid_noArchive() {
		List<NotesData> result = notesService.search(new Queries.SearchByExternalGuid().setExternalGuid("10a14259-ca84-4c7d-8d46-7ad398000002")
				.setGetUpdateHistory(true).setGetArchived(false));
		validateAll(result,7,3,1);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void getByExternalGuid_noHistoryAndArchives() {
		List<NotesData> result = notesService.search(new Queries.SearchByExternalGuid().setExternalGuid("10a14259-ca84-4c7d-8d46-7ad398000002")
				.setGetUpdateHistory(false).setGetArchived(false));
		validateAll(result,6,3,0);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void searchContent() {
		List<NotesData> result = notesService.search(new Queries.SearchByContent().setContentToSearch("content")
				.setRequestType(RequestType.CONTENT).setGetUpdateHistory(false).setGetArchived(false));
		checkDuplicates(result);
		assertEquals(11,result.size());
	}

	@Test
	void getByEntryGuid_all() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("7f20d0eb-3907-4647-9584-3d7814cd3a55")
				.setGetUpdateHistory(true).setGetArchived(true).setSearchField(ESIndexNotesFields.ENTRY));

		validateAll(result,4,2,1);
	}

	@Test
	void getByEntryGuid_all_test_1() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("ba7a0762-935d-43f3-acb0-c33d86e7f350")
				.setGetUpdateHistory(true).setGetArchived(true).setSearchField(ESIndexNotesFields.ENTRY));
		
		validateAll(result,8,5,2);
	}

	@Test
	void getByEntryGuid_all_test_2() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("06a418c3-7475-473e-9e9d-3e952d672d4c")
				.setGetUpdateHistory(true).setGetArchived(true).setSearchField(ESIndexNotesFields.ENTRY));
		
		validateAll(result,6,4,1);
	}

	@Test
	void getByEntryGuid_noHistories() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("7f20d0eb-3907-4647-9584-3d7814cd3a55")
				.setGetUpdateHistory(false).setGetArchived(true).setSearchField(ESIndexNotesFields.ENTRY));
		
		validateAll(result,3,2,0);
	}

	@Test
	void getByEntryGuid_noHistories_test_1() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("ba7a0762-935d-43f3-acb0-c33d86e7f350")
				.setGetUpdateHistory(false).setGetArchived(true).setSearchField(ESIndexNotesFields.ENTRY));
		
		validateAll(result,6,5,0);
	}

	@Test
	void getByEntryGuid_noArchived_test_1() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("7f20d0eb-3907-4647-9584-3d7814cd3a55")
				.setGetUpdateHistory(true).setGetArchived(false).setSearchField(ESIndexNotesFields.ENTRY));
		assertEquals(0,result.size());
	}

	@Test
	void getByEntryGuid_noArchived_test_2() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("16b8d331-92ab-424b-b69a-3181f6d80f5a")
				.setGetUpdateHistory(true).setGetArchived(false).setSearchField(ESIndexNotesFields.ENTRY));
		validateAll(result,1,0,0);
	}

	@Test
	void getByEntryGuid_noArchived_test_3() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("ba7a0762-935d-43f3-acb0-c33d86e7f350")
				.setGetUpdateHistory(true).setGetArchived(false).setSearchField(ESIndexNotesFields.ENTRY));
		validateAll(result,4,2,1);
	}

	@Test
	void getByEntryGuid_NoHistoriesAndArchives_test_1() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("7f20d0eb-3907-4647-9584-3d7814cd3a55")
				.setGetUpdateHistory(false).setGetArchived(false).setSearchField(ESIndexNotesFields.ENTRY));
		
		validateAll(result,0,0,0);
	}

	@Test
	void getByEntryGuid_NoHistoriesAndArchives_test_2() {
		List<NotesData> result = notesService.search(new Queries.SearchByEntryGuid().setEntryGuid("ba7a0762-935d-43f3-acb0-c33d86e7f350")
				.setGetUpdateHistory(false).setGetArchived(false).setSearchField(ESIndexNotesFields.ENTRY));
		
		validateAll(result,3,2,0);
	}

	private void validateAll(List<NotesData> result, int expectedTotalCount, int expectedThreadCount, int expectedHistoryCount) {
		List<NotesData> total = new ArrayList<>();
		List<NotesData> totalThreads = new ArrayList<>();
		List<NotesData> totalHistories = new ArrayList<>();
		result.forEach(r -> flatten(r, total));
		result.forEach(r -> flattenThreads(r, totalThreads));
		result.forEach(r -> flattenHistories(r, totalHistories));
		assertEquals(expectedTotalCount,total.size());
		assertEquals(expectedThreadCount,totalThreads.size());
		assertEquals(expectedHistoryCount,totalHistories.size());

		// this is to check each individual external entries will have different entryGuid
		for(int i = 0; i < result.size(); i++) {
			for (int j = i + 1; j < result.size(); j++) {
				assertNotEquals(result.get(i).getEntryGuid(),result.get(j).getEntryGuid());
			}
		}

		// this is to check each external entry and its history will have the same entryGuid
		for(int i = 0; i < result.size(); i++) {
			if(result.get(i).getHistory() != null) {
				for (int j = 0; j < result.get(i).getHistory().size(); j++) {
					assertEquals(result.get(i).getEntryGuid(),result.get(i).getHistory().get(j).getEntryGuid());
				}
			}
		}

		// this is to check each threads will have same thread guid as their parent 
		for(int i = 0; i < result.size(); i++) {
			if(result.get(i).getThreads() != null) {
				for (int j = 0; j < result.get(i).getThreads().size(); j++) {
					assertEquals(result.get(i).getThreadGuid(),result.get(i).getThreads().get(j).getThreadGuidParent());
				}
			}
		}

		checkDuplicates(result);
	}

	private void checkDuplicates(List<NotesData> result){
		// check for duplicate entries
		List<NotesData> flattenEntries = new ArrayList<>();
		Set<String> entryCount = new HashSet<>();
		int j = 0;
		for(int i = 0; i < result.size(); i++) {
			flatten(result.get(i),flattenEntries);
			while(j < flattenEntries.size()) {
				String guidKey = flattenEntries.get(j).getGuid().toString();
				if(entryCount.contains(guidKey)) { // this check is for debug just in case
					assertFalse(entryCount.contains(guidKey));
				}
				entryCount.add(guidKey);
				j++;
			}
		}
	}

	private void flatten(NotesData root, List<NotesData> entries) {
		entries.add(root);
		if(root.getThreads() != null)
			root.getThreads().forEach(e -> flatten(e,entries));
		if(root.getHistory() != null)
			root.getHistory().forEach(e -> flatten(e,entries));
	}

	private void flattenThreads(NotesData root, List<NotesData> entries) {
		if(root.getThreads() != null)
			root.getThreads().forEach(e -> {
				entries.add(e);
				flattenThreads(e,entries);
			});
	}

	private void flattenHistories(NotesData root, List<NotesData> entries) {
		if(root.getThreads() != null)
			root.getThreads().forEach(e -> {
				flattenHistories(e,entries);
			});
		if(root.getHistory() != null)
			root.getHistory().forEach(e -> {
				entries.add(e);
				flattenHistories(e,entries);
			});
	}

	private Map<String,NotesData> getEntries() {
		Map<String,NotesData> entries = new HashMap<>();
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
			for(int i=0; i< jsonArray.length(); i++) {
				NotesData data = NotesData.fromJson(jsonArray.getString(i));
				entries.put(data.getGuid().toString(),data);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return entries;
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

}
