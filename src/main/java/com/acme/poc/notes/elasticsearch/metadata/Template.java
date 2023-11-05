package com.acme.poc.notes.elasticsearch.metadata;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
public class Template {

    private String name;
    private int version;
    private String pattern;
    private String aliasName;
    private int shards;
    private int replicas;
    private int maxRegexLength;

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public String getAliasName() {
        return aliasName;
    }

    public int getShards() {
        return shards;
    }

    public int getReplicas() {
        return replicas;
    }

    public int getVersion() {
        return version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public int getMaxRegexLength() {
        return maxRegexLength;
    }

    public void setMaxRegexLength(int maxRegexLength) {
        this.maxRegexLength = maxRegexLength;
    }

    public Template() {
    }

    public Template(String name, int version, String pattern, String aliasName, int shards, int replicas, int maxRegexLength) {
        this.name = name;
        this.version = version;
        this.pattern = pattern;
        this.aliasName = aliasName;
        this.shards = shards;
        this.replicas = replicas;
        this.maxRegexLength = maxRegexLength;
    }
}