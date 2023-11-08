# ToDo's


## Current phase

- [ ] Replace getters/setters with Lombok's @Data or @Setter/@Getter and builders with Lombok's @Builder
- [ ] First and foremost, refactor, simplify and optimize code and give design perspective to it. Use different
      strategy to consume data from elasticsearch and process it as:
  - [ ] Current implementation has incremental calls to elasticsearch for every thread. there are going to be
        many call in single searchRequest.
  - [ ] Provide different strategy, where there will be 1 or 2 calls/search searchRequest to elastic search and
        get the response in one go and process them internally. Both implementation can be kept with flag
- [ ] Add logs, handle exceptions and other validations if any
  - [ ] Use default Logback instead of Log4j2
- [ ] record class


## Under consideration

- [ ] Open search API support, flag based to switch between rest high level client and open search?
- [ ] Do need an entry that represent an item? Ex: if YouTube represents externalGuid, each video on it is an
      item. for every video,we may find list of entries with threads.


## Later phase(s)

- [ ] Create index at startup if not exists, should use mapping from configs.
- [ ] Delete/archive - These operations are performed on selected entries when queried by ExternalGuid/EntryGuid. Fixed number entries are returned from es to perform these operation, run a loop
- [ ] Address content search corner cases. content search result set may have random entries with no links to its parents. so apply an algorithm to find out nearest parent entry, if not actual 
- [ ] SpringDoc open search
- [ ] Pagination - make this searchAfter internal to backend with session timeout?
- [ ] Sorting provision by DESC/ASC
- [ ] provision for size in queryParam, if not provided, default 1K records are returned
- [ ] delete only histories?
- [ ] JUnit, Integration and PSR Test Cases
- [ ] POC on Logging and tracing
- [ ] Authentication and authorization
- [ ] Scalability - lstio API gateway?


## Fixed

- [x] In general use timestamps as `uuuu-MM-dd'T'HH:mm:ss.SSSSSSXXX`. Currently `"created"` value is stored
      with only 3 digits for milliseconds.
- [x] When saving data to Elasticsearch:
  - [x] Do not store `"_class" : "com.acme.poc.notes.elasticsearch.pojo.NotesData"`.
  - [x] Do not store `"threads" : [ ]` when it has no content.
  - [x] Do not store `"history" : [ ]` when it has no content.
  - [x] Do not store null values. I haven't seen any yet, but just to make sure.
- [x] GET `/api/v1/search/external?externalGuid=...`
  - When searching for an `externalGuid` it only returns the first document found. It should return all
    documents that has that `externalGuid`.
- [X] Addressed corner cases for search archived entries by entryGuid
- [X] Validation for update API to make sure latest version of entry is being modified. if entry is updated by other user, an error is shown