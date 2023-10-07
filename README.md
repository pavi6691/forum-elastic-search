Execute below command to start elastic container
-
    docker run --name elastic_search -d -p 9200:9200 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.8.1

Get Started with
-
    createIndex - POST http://localhost:8080/api/v1/createIndex?indexName=note-v1

APIs usage guidelines to follow
-
    - payload for create and update requests
      - {
           "guid": "",
           "externalGuid": "External-1",
           "threadGuidParent": "",
           "entryGuid": "",
           "content":"This World Is beautiful!"
        }
    - Below fields required to provide when calling each API
      - Create API POST http://localhost:8080/api/v1/create
        - This Api is used for creating new entry also thread of answer
          - New Entry
            - externalGuid - externalSystem
            - content
          - Answer
            - threadGuidParent - It's and threadGuid to which answer is being made
            - content
      - Update API - PUT http://localhost:8080/api/v1/update
        - for the entry being updated, its required populate below fields
          - guid - as there can be multiple entries with same entryGuid, needs unique id of entry being updated
          - entryGuid
          - content
      - Archive by ExternalGuid API - PUT http://localhost:8080/api/v1/archive/externalGuid?externalGuid=
      - Archive by EntryGuid API -  PUT http://localhost:8080/api/v1/archive/entryGuid?entryGuid=
      - SearchByExternalGuid API - GET http://localhost:8080/api/v1/search/externalGuid?externalGuid=External-10&searchUpdateHistory=true&sendArchivedResponse=true
      - SearchByEntryGuid API - GET http://localhost:8080/api/v1/search/entryGuid?externalGuid=External-10&searchUpdateHistory=true&sendArchivedResponse=true
      - deleteByExternalGuid - DELETE http://localhost:8080/api/v1/delete/externalGuid?externalGuid=
      - deleteByEntryGuid - DELETE http://localhost:8080/api/v1/delete/entryGuid?entryGuid=