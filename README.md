### Execute below command to start elastic container
    docker run --name elastic_search -d -p 9200:9200 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.8.1

### Get Started with
    createIndex - POST [http://localhost:8080/api/v1/createIndex?indexName=note-v1]

### API Details
  #### Payload for create and update requests
  ```json
  {
    "guid": "",
    "externalGuid": "External-System-GUID",
    "threadGuidParent": "",
    "entryGuid": "",
    "content":"This World Is beautiful!"
  }
  ```
  1. #### Create new Entry
  ```http
  POST http://localhost:8080/api/v1/create
  ```
  | Field          | Type      | Description                             |
  |:---------------|:----------|:----------------------------------------|
  | `externalGuid` | `string`  | **Required**. Your External System GUID |
  | `content`      | `string`  | **Required**. Content                   |


  2. #### Create Answer (thread entry)
  ```http
  POST http://localhost:8080/api/v1/create
  ```
  | Field              | Type     | Description                                                      |
  |:-------------------|:---------|:-----------------------------------------------------------------|
  | `threadGuidParent` | `string` | **Required**. Thread GUID Of entry to this answer is created for |
  | `content`          | `string` | **Required**. Content                                            | 


  3. #### Update - By entryGuid
  ```http
  POST http://localhost:8080/api/v1/update/entry
  ```
  | Field       | Type     | Description                                                    |
  |:------------|:---------|:---------------------------------------------------------------|
  | `entryGuid` | `string` | **Required**. entryId to Update. Recommended to update by GUID |
  | `content`   | `string` | **Required**. Content                                          |


  4. #### Update - By guid - straight forward way to update an entry, if entry of guid is available
  ```http
  POST http://localhost:8080/api/v1/update/guid
  ```
  | Field     | Type      | Description                        |
  |:----------|:----------|:-----------------------------------|
  | `guid`    | `string`  | **Required**. Key GUID of an entry |
  | `content` | `string`  | **Required**. Content              |


  5. #### Search - by externalGuid
  ```http
  GET http://localhost:8080/api/v1/search/external?externalGuid=External-ID&searchUpdateHistory=true&getArchivedResponse=true
  ```
  | QueryParams            | Type      | Description                                                                      |
  |:-----------------------|:----------|:---------------------------------------------------------------------------------|
  | `externalGuid`         | `string`  | **Required**. GUID of an external system                                         |
  | `searchUpdateHistory`  | `boolean` | **optional**. **default false** true to Include history of this entry            |
  | `getArchivedResponse` | `boolean` | **optional**. **default false** true to Include archived entries if exists       |
  | `onlyArchived`         | `boolean` | **optional**. **default true** Search only archived/all. default it searches all |


  6. #### Search - by entryGuid
  ```http
  GET http://localhost:8080/api/v1/search/entry?entryGuid=EntryID&searchUpdateHistory=true&getArchivedResponse=true
  ```
  | QueryParams            | Type      | Description                                                                      |
  |:-----------------------|:----------|:---------------------------------------------------------------------------------|
  | `entryGuid`            | `string`  | **Required**. GUID of an entry                                                   |
  | `searchUpdateHistory`  | `boolean` | **optional**. **default false** true to Include all history of this entry        |
  | `getArchivedResponse` | `boolean` | **optional**. **default false** true to Include all archived entries if exits    |
  | `onlyArchived`         | `boolean` | **optional**. **default true** Search only archived/all. default it searches all |


  7. #### Archive - By ExternalGuid
  ```http
  PUT http://localhost:8080/api/v1/archive/external?externalGuid=
  ```
  | QueryParams    | Type      | Description                            |
  |:---------------|:----------|:---------------------------------------|
  | `externalGuid` | `string`  | **Required**. GUID of external system  |


  8. #### Archive - By entryGuid
  ```http
  PUT http://localhost:8080/api/v1/archive/entry?entryGuid=
  ```
  | QueryParams | Type     | Description                        |
  |:------------|:---------|:-----------------------------------|
  | `entryGuid` | `string` | **Required**. GUID of an entryGuid |


  9. #### Search archived entries - By ExternalGuid
  ```http
  PUT http://localhost:8080/api/v1/search/archive/external?externalGuid=
  ```
  | QueryParams    | Type      | Description                            |
  |:---------------|:----------|:---------------------------------------|
  | `externalGuid` | `string`  | **Required**. GUID of external system  |


  10. #### Search archived entries - By entryGuid
  ```http
  PUT http://localhost:8080/api/v1/search/archive/entry?entryGuid=
  ```
  | QueryParams | Type     | Description                        |
  |:------------|:---------|:-----------------------------------|
  | `entryGuid` | `string` | **Required**. GUID of an entryGuid |


  11. #### Delete - By ExternalGuid
  ```http
  PUT http://localhost:8080/api/v1/delete/external?externalGuid=&deleteOnlyArchived=
  ```
  | QueryParams          | Type      | Description                            |
  |:---------------------|:----------|:---------------------------------------|
  | `externalGuid`       | `string`  | **Required**. GUID of external system  |
  | `deleteOnlyArchived` | `boolean` | **Required**. Delete only archived/all |


  12. #### Delete - By entryGuid
  ```http
  PUT http://localhost:8080/api/v1/delete/entry?entryGuid=&deleteOnlyArchived=
  ```
  | QueryParams          | Type      | Description                            |
  |:---------------------|:----------|:---------------------------------------|
  | `entryGuid`          | `string`  | **Required**. GUID of an entry         |
  | `deleteOnlyArchived` | `boolean` | **Required**. Delete only archived/all |