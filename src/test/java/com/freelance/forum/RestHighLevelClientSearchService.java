package com.freelance.forum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.freelance.forum.elasticsearch.configuration.EsConfig;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.pojo.Request;
import com.freelance.forum.elasticsearch.queries.ESIndexNotesFields;
import com.freelance.forum.elasticsearch.repository.ESRepository;
import com.freelance.forum.service.AbstractService;
import com.freelance.forum.service.ESService;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestHighLevelClientSearchService {


	@Autowired
	AbstractService restHighLevelClientSearchService;
	
	@Autowired
	ESService service;

	@Autowired
	ESRepository esRepository;

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
	void getByExternalGuid_all() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("10a14259-ca84-4c7d-8d46-7ad398000002")
				.setEsIndexNotesFields(ESIndexNotesFields.EXTERNAL).setUpdateHistory(true).setArchivedResponse(true).build());
		validateAll(result,11,6,2);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void getByExternalGuid_noHistories() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("10a14259-ca84-4c7d-8d46-7ad398000002")
				.setEsIndexNotesFields(ESIndexNotesFields.EXTERNAL).setUpdateHistory(false).setArchivedResponse(true).build());
		validateAll(result,9,6,0);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void getByExternalGuid_noArchive() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("10a14259-ca84-4c7d-8d46-7ad398000002")
				.setEsIndexNotesFields(ESIndexNotesFields.EXTERNAL).setUpdateHistory(true).setArchivedResponse(false).build());
		validateAll(result,7,3,1);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void getByExternalGuid_noHistoryAndArchives() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("10a14259-ca84-4c7d-8d46-7ad398000002")
				.setEsIndexNotesFields(ESIndexNotesFields.EXTERNAL).setUpdateHistory(false).setArchivedResponse(false).build());
		validateAll(result,6,3,0);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void searchContent() {
		List<NotesData> result = service.searchEntries("Content-", ESIndexNotesFields.CONTENT,true,true, SortOrder.DESC);
		validateAll(result,10,6,2);
		// there can be multiple external entries, so check them too
		assertEquals(3,result.size());
	}

	@Test
	void getByEntryGuid_all() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("7f20d0eb-3907-4647-9584-3d7814cd3a55")
						.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(true).setArchivedResponse(true).build());

		validateAll(result,4,2,1);
	}

	@Test
	void getByEntryGuid_all_test_1() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("ba7a0762-935d-43f3-acb0-c33d86e7f350")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(true).setArchivedResponse(true).build());

		validateAll(result,8,5,2);
	}

	@Test
	void getByEntryGuid_all_test_2() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("06a418c3-7475-473e-9e9d-3e952d672d4c")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(true).setArchivedResponse(true).build());

		validateAll(result,6,4,1);
	}

	@Test
	void getByEntryGuid_noHistories() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("7f20d0eb-3907-4647-9584-3d7814cd3a55")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(false).setArchivedResponse(true).build());

		validateAll(result,3,2,0);
	}

	@Test
	void getByEntryGuid_noHistories_test_1() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("ba7a0762-935d-43f3-acb0-c33d86e7f350")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(false).setArchivedResponse(true).build());

		validateAll(result,6,5,0);
	}

	@Test
	void getByEntryGuid_noArchived_test_1() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("7f20d0eb-3907-4647-9584-3d7814cd3a55")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(true).setArchivedResponse(false).build());

		assertEquals(0,result.size());
	}

	@Test
	void getByEntryGuid_noArchived_test_2() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("16b8d331-92ab-424b-b69a-3181f6d80f5a")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(true).setArchivedResponse(false).build());

		validateAll(result,1,0,0);
	}

	@Test
	void getByEntryGuid_noArchived_test_3() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("ba7a0762-935d-43f3-acb0-c33d86e7f350")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(true).setArchivedResponse(false).build());

		validateAll(result,4,2,1);
	}

	@Test
	void getByEntryGuid_NoHistoriesAndArchives_test_1() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("7f20d0eb-3907-4647-9584-3d7814cd3a55")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(false).setArchivedResponse(false).build());

		validateAll(result,0,0,0);
	}

	@Test
	void getByEntryGuid_NoHistoriesAndArchives_test_2() {
		List<NotesData> result = restHighLevelClientSearchService.search(new Request.Builder().setSearch("ba7a0762-935d-43f3-acb0-c33d86e7f350")
				.setEsIndexNotesFields(ESIndexNotesFields.ENTRY).setUpdateHistory(false).setArchivedResponse(false).build());

		validateAll(result,3,2,0);
	}
	
	private void validateAll(List<NotesData> result, int expectedResultCount, int expectedThreadCount, int expectedHistoryCount) {
		List<NotesData> total = new ArrayList<>();
		List<NotesData> totalThreads = new ArrayList<>();
		List<NotesData> totalHistories = new ArrayList<>();
		result.forEach(r -> flatten(r, total));
		result.forEach(r -> flattenThreads(r, totalThreads));
		result.forEach(r -> flattenHistories(r, totalHistories));
		assertEquals(expectedResultCount,total.size());
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
	

//	@BeforeEach
//	void testIsContainerRunning() {
//		assertTrue(elasticsearchContainer.isRunning());
//	}

//	@Test
//	void createEntries() throws JSONException {
//		JSONArray jsonArray = new JSONArray(ElasticSearchData.ENTRIES);
//		for(int i=0; i< jsonArray.length(); i++) {
//			List<NotesData> entries = new ArrayList<>();
//			String jsonStringToStore = jsonArray.getString(i);
//			flatten(NotesData.fromJson(jsonStringToStore),entries);
//			entries.forEach(e -> {
//				NotesData notesData = esRepository.save(e);
//				assertEquals(notesData.getGuid(),e.getGuid());
//				assertEquals(notesData.getExternalGuid(),e.getExternalGuid());
//				assertEquals(notesData.getEntryGuid(),e.getEntryGuid());
//				assertEquals(notesData.getThreadGuid(),e.getThreadGuid());
//				assertEquals(notesData.getThreadGuidParent(),e.getThreadGuidParent());
//				assertEquals(notesData.getContent(),e.getContent());
//				assertEquals(notesData.getCreated(),e.getCreated());
//				assertEquals(notesData.getArchived(),e.getArchived());
//			});
//		}
//	}

}
