package com.acme.poc.notes.elasticsearch.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
@AllArgsConstructor
@NoArgsConstructor
public class PolicyInfo {

    @Setter
    @Getter
    private Map<String, Object> policy;
    @Setter @Getter
    private String policyName;
    @JsonProperty("schema_version")
    @Setter @Getter
    private int schemaVersion;

}