package com.acme.poc.notes;

import com.acme.poc.notes.base.BaseTest;
import com.acme.poc.notes.elasticsearch.pojo.NotesData;
import com.acme.poc.notes.elasticsearch.queries.SearchByExternalGuid;
import com.acme.poc.notes.elasticsearch.queries.generics.IQuery;
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
    static final int NR_OF_ENTRIES = 20; 
    static final String EXTERNAL_GUID = "164c1633-44f0-4eee-8491-d5e6a539b300";
    @Test
    void SaveEntries() {
        NotesData entry = createNewEntry(new NotesData.Builder()
                .setExternalGuid(UUID.fromString(EXTERNAL_GUID))
                .setContent("New External Entry-1").build());
        // create Threads
        for(int i = 0; i < NR_OF_ENTRIES; i++) {
            entry = createThread(entry, "New External Entry-Thread-" + i);
        }
    }

    @Test
    void searchEntries() {
        IQuery query = new SearchByExternalGuid().setSearchBy(EXTERNAL_GUID)
                .setGetUpdateHistory(true).setGetArchived(true);
        List<NotesData> searchResult = notesService.search(query);
        validateAll(searchResult,1,NR_OF_ENTRIES+1,NR_OF_ENTRIES,0);
    }

    @Test
    void deleteEntries() {
        IQuery query = new SearchByExternalGuid().setSearchBy(EXTERNAL_GUID)
                .setGetUpdateHistory(true).setGetArchived(true);
        List<NotesData> searchResult = notesService.delete(query);
        validateAll(searchResult,0,0,0,0);
    }
}
