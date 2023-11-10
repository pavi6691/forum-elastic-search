# ToDo's


## Current phase
- [ ] [Proposal for changing guid names](documentation/NotesGuidOverview.drawio.png) (PNG with embedded Draw.io diagram)  
- [ ] Fix support for storing `customJson` as part of an entry. Saving with this:


    "customJson": {
      "timestampStart": 1,
      "timestampEnd": 2
    }

results in this in Elasticsearch:

    "customJson" : {
      "_children" : {
        "timestampStart" : {
          "_value" : 1
        },
        "timestampEnd" : {
          "_value" : 2
        }
      },
      "_nodeFactory" : {
        "_cfgBigDecimalExact" : false
      }
    }

whereas it should simply be:

    "customJson": {
      "timestampStart": 1,
      "timestampEnd": 2
    }

- [ ] Seems that note-v1_mapping.json is not used when creating the index? At least in Kibana index shows mapping as being different
- [X] Implement AdminResource (currently commented out code)
- [ ] An endpoint (API_ENDPOINT_NOTES_GET_BY_EXTERNAL_GUID) for returning everything related to an externalGuid is missing
- [ ] Use TestContainers for ElasticSearch in tests instead of relying on an existing Elasticsearch being available
- [ ] NotesData uses Date's. Shouldn't it use ZonedDateTime or similar?
- [ ] Add logs, handle exceptions and other validations if any
- [ ] Record class


## Under consideration

- [ ] Open search API support, flag based to switch between rest high level client and open search?
- [ ] Do need an entry that represent an item? Ex: if YouTube represents externalGuid, each video on it is an
      item. for every video,we may find list of entries with threads.
- Below two uses cases are covered with same fix
  - [ ] Currently search by entry guid performs query for all entries that are created after the requested one. It gets different entries that doesn't belongs to this. So index by maintaining same guid for all individual entry and threads. 
  - [ ] Address content search corner cases. content search result set may have random entries with no links to its parents. so apply an algorithm to find out nearest parent entry, if not actual
- [ ] Handling more than 1k thread entries? Wasn't this solved by V3?

## Later phase(s)

- [ ] Do we need `NotesProcessorV1` and `NotesProcessorV2`? If not they should be removed. - not required, but I think let's remove them once testing is done
- [ ] Restrict size of content?
- [ ] Actuator health and info endpoints
- [ ] SpringDoc open search
- [ ] Delete only histories?
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
- [x] GET `/api/v1/notes/externalguid/{externalGuid}`
  - When searching for an `externalGuid` it only returns the first document found. It should return all
    documents that has that `externalGuid`.
- [X] Addressed corner cases for search archived entries by entryGuid
- [X] Validation for update API to make sure latest version of entry is being modified. if entry is updated by other user, an error is shown
- [X] Replace getters/setters with Lombok's @Data or @Setter/@Getter and builders with Lombok's @Builder
- [X] First and foremost, refactor, simplify and optimize code and give design perspective to it. Use different
  strategy to consume data from elasticsearch and process it as:
    - [X] Current implementation has incremental calls to elasticsearch for every thread. there are going to be
      many call in single searchRequest.
    - [X] Provide different strategy, where there will be 1 or 2 calls/search searchRequest to elastic search and
      get the response in one go and process them internally. Both implementation can be kept with flag
- [X] When searched archived entries by entry, there are multiple corner cases to handle 
- [X] Pagination - make this searchAfter internal to backend with session timeout?
- [X] Sorting provision by DESC/ASC
- [X] Create index at startup if not exists, should use mapping from configs.
- [X] Delete/archive - These operations are performed on selected entries when queried by ExternalGuid/EntryGuid. Fixed number of entries are returned from es to perform these operation, run a loop
- [X] provision for size in queryParam, if not provided, default 1K records are returned
- [X] validation on update multiple user modifying same entry at the same time
- [X] For request by entryGuid, query gets all entries that are created after the requested one. so result set from elasticsearch may contain
      other entries that not belongs to requested entry thread. as they may have been created/updated for others but after this entry is created.
  So further filter is done as below -
   - [X] For entry request, exclude all other entries that doesn't belongs to the request one. only one record stored in results list
         and threadMapping is done only for this record
   - [X] For selection of multi entries within requested entry due to some criteria(Ex - only archived entries),
         then map(archived - for archived filter) that will have only archived entries. these entries are presented within the requested entry thread.
- [X] JUnit, Integration and PSR Test Cases
- [X] Change query string parameter `getUpdateHistory` to `includeVersions`
- [X] Change query string parameter `getArchivedResponse` to `includeArchived` ehh... how is this different from `getUpdateHistory`? - it is to include entries archived. if false archived entries are discarded. archived entries are those with archived timestamp set.