package com.freelance.forum;

import com.freelance.forum.base.BaseTest;
import com.freelance.forum.data.ElasticSearchData;
import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.elasticsearch.queries.*;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StaticDataTest extends BaseTest {

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
