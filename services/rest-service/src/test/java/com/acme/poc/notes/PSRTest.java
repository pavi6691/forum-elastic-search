package com.acme.poc.notes;

import com.acme.poc.notes.base.BaseTest;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.SearchByExternalGuid;
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
public class PSRTest extends BaseTest {

    //for 10K, its stack overflow for V2 as JVM stack size exceeding. V3 doesn't use recursion, so we are good there
    static final int NR_OF_ENTRIES = 10000; 
    static final String EXTERNAL_GUID = "164c1633-44f0-4eee-8491-d5e6a5391300";
    @Test
    void SaveEntries() {
        NotesData entry = createNewEntry(NotesData.builder()
                .externalGuid(UUID.fromString(EXTERNAL_GUID))
                .content("New External Entry-1").build());
        // create Threads
        for(int i = 0; i < NR_OF_ENTRIES; i++) {
            entry = createThread(entry, "New External Entry-Thread-" + i);
        }
    }

    @Test
    void searchEntries() {
        SearchByExternalGuid query = SearchByExternalGuid.builder().searchGuid(EXTERNAL_GUID)
                .includeVersions(true).includeArchived(true).build();
        List<NotesData> searchResult = notesAdminService.searchByExternalGuid(query);
        validateAll(searchResult,1,NR_OF_ENTRIES,NR_OF_ENTRIES-1,0);
    }

    @Test
    void deleteEntries() {
        List<NotesData> searchResult = notesAdminService.deleteByExternalGuid(UUID.fromString(EXTERNAL_GUID));
        validateAll(searchResult,1,NR_OF_ENTRIES,NR_OF_ENTRIES-1,0);
        searchResult = notesAdminService.searchByExternalGuid(SearchByExternalGuid.builder().searchGuid(EXTERNAL_GUID)
                .includeVersions(true).includeArchived(true).build());
        validateAll(searchResult,0,0,0,0);
    }
}
