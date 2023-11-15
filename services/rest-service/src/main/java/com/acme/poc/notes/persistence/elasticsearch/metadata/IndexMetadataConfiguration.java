package com.acme.poc.notes.persistence.elasticsearch.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
@Configuration
public class IndexMetadataConfiguration {

    private Map<String, IndexMetadata> indexMetadataMap = new HashMap<>();

    public IndexMetadataConfiguration(Map<String, IndexMetadata> indexMetadataMap) {
        this.indexMetadataMap = indexMetadataMap;
    }

    public Map<String, IndexMetadata> getIndexMetadataMap() {
        return indexMetadataMap;
    }

    public void setIndexMetadataMap(Map<String, IndexMetadata> indexMetadataMap) {
        this.indexMetadataMap = indexMetadataMap;
    }

    public IndexMetadataConfiguration() {
    }

    public IndexMetadata getIndexMetadata(String docType) {
        IndexMetadata indexMetadata = indexMetadataMap.get(docType);
        if(null != indexMetadata){
            return indexMetadata;
        } else {
            throw new RuntimeException("InvalidIndexType");
        }
    }
}