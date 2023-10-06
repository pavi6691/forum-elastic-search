- Execute below command start elastic container
  - docker run --name elastic_search -d -p 9200:9200 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.8.0

Follow below guidelines to get all APIs working correctly
- Below fields required to provide when calling each API
  - Create API - This Api is used for creating new entry also thread of answer
    - New Entry
      - externalGuid - externalSystem
      - content
    - Answer
      - threadGuidParent - It's and threadGuid to which answer is being made
      - content
  - Update API - for the entry being updated, its required populate below fields
    - guid
    - entryGuid
    - content
  - Archive API -
    - guid - updating only entry with this id will consider all childes archived. no need to update all entries by ExternalId/EntryId
  - findByExternalGuid API
    - /findByExternalGuid/{externalGuid}/{searchUpdateHistory}/{sendArchivedResponse}
  - findByEntryGuid API
    - /findByEntryGuid/{entryGuid}/{searchUpdateHistory}/{sendArchivedResponse}

Technical specifications - For Developer Only
- entryGuid is the uniqueID backed needs to be created
- Update requires entryGuid in payload, if not present, throw an exception
- Historical have same entryGuid
- When entryGuidParent provided in create, consider as an answer, search by entryGuid and
- return null to parentGuuid
- two people editing at the same time, check content and go ahread for update
- archive & delete by ExternalId and EntryId