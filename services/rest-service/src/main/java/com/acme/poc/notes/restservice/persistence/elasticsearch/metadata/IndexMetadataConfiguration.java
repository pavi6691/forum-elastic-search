package com.acme.poc.notes.restservice.persistence.elasticsearch.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Configuration
public class IndexMetadataConfiguration {

    private Map<String, IndexMetadata> indexMetadataMap = new HashMap<>();


    public IndexMetadata getIndexMetadata(String docType) {
        IndexMetadata indexMetadata = indexMetadataMap.get(docType);
        if (null != indexMetadata){
            return indexMetadata;
        } else {
            throw new RuntimeException("InvalidIndexType");
        }
    }

}
