package com.acme.poc.notes.elasticsearch.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
public class PolicyInfo {

    private Map<String, Object> policy;
    private String policyName;
    @JsonProperty("schema_version")
    private int schemaVersion;

    public PolicyInfo(String policyName, int schemaVersion, Map<String, Object> policy) {
        this.policyName = policyName;
        this.schemaVersion = schemaVersion;
        this.policy = policy;
    }

    public PolicyInfo() {
    }

    public String getPolicyName() {
        return policyName;
    }

    public Map<String, Object> getPolicy() {
        return policy;
    }

    public void setPolicy(Map<String, Object> policy) {
        this.policy = policy;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

}