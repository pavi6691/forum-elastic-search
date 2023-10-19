package com.freelance.forum;

import com.freelance.forum.elasticsearch.pojo.NotesData;
import com.freelance.forum.service.AbstractService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class RestHighLevelClientSearchService {


	AbstractService restHighLevelClientSearchService;

	@BeforeAll
	void setup() {
		List<NotesData> results = new ArrayList<>();
		NotesData rootEntry = new NotesData(UUID.randomUUID(),UUID.randomUUID(), UUID.randomUUID(),UUID.randomUUID(),
				null,"All is Well",new Date(),new Date(),new ArrayList<>(), new ArrayList<>());
		results.add(rootEntry);
		restHighLevelClientSearchService = Mockito.mock(AbstractService.class);
		Mockito.when(restHighLevelClientSearchService.execQueryOnEs(""))
				.thenReturn(results.iterator());
	}

	@Test
	void contextLoads() {
	}

}
