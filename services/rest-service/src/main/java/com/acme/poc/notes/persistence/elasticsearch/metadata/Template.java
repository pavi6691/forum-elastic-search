package com.acme.poc.notes.persistence.elasticsearch.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect
@AllArgsConstructor
@NoArgsConstructor
public class Template {

    @Getter @Setter
    private String name;
    @Getter @Setter
    private int version;
    @Getter @Setter
    private String pattern;
    @Getter @Setter
    private String aliasName;
    @Getter @Setter
    private int shards;
    @Getter @Setter
    private int replicas;
    @Getter @Setter
    private int maxRegexLength;
}