### Execute below command to start elastic container
    docker run --name elastic_search -d -p 9200:9200 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.8.12

### Get Started with creating index. execute below API
  ```http
  POST http://localhost:8080/api/v1/createIndex?indexName=note-v1
  ```

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


  2. #### Create thread entry
  ```http
  POST http://localhost:8080/api/v1/create
  ```
  | Field              | Type     | Description                                                  |
  |:-------------------|:---------|:-------------------------------------------------------------|
  | `threadGuidParent` | `string` | **Required**. Thread GUID Of parent to create an entry under |
  | `content`          | `string` | **Required**. Content                                        | 


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
  GET http://localhost:8080/api/v1/search/external?externalGuid=External-ID&getUpdateHistory=true&getArchivedResponse=true
  ```
  | QueryParams           | Type      | Description                                                                      |
  |:----------------------|:----------|:---------------------------------------------------------------------------------|
  | `externalGuid`        | `string`  | **Required**. GUID of an external system                                         |
  | `getUpdateHistory`    | `boolean` | **optional**. **default false** true to Include history of this entry            |
  | `getArchivedResponse` | `boolean` | **optional**. **default false** true to Include archived entries if exists       |


  6. #### Search - by entryGuid
  ```http
  GET http://localhost:8080/api/v1/search/entry?entryGuid=EntryID&getUpdateHistory=true&getArchivedResponse=true
  ```
  | QueryParams           | Type      | Description                                                                      |
  |:----------------------|:----------|:---------------------------------------------------------------------------------|
  | `entryGuid`           | `string`  | **Required**. GUID of an entry                                                   |
  | `getUpdateHistory`    | `boolean` | **optional**. **default false** true to Include all history of this entry        |
  | `getArchivedResponse` | `boolean` | **optional**. **default false** true to Include all archived entries if exits    |


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
  PUT http://localhost:8080/api/v1/delete/external?externalGuid=&deleteEntries=archived
  ```
  | QueryParams       | Type     | Description                                                                      |
  |:------------------|:---------|:---------------------------------------------------------------------------------|
  | `externalGuid`    | `string` | **Required**. GUID of external system                                            |
  | `entriesToDelete` | `String` | **Required**. "all" to Delete all entries and "archived" to delete only archived |


  12. #### Delete - By entryGuid
  ```http
  PUT http://localhost:8080/api/v1/delete/entry?entryGuid=&deleteEntries=archived
  ```
  | QueryParams       | Type       | Description                                                                      |
  |:------------------|:-----------|:---------------------------------------------------------------------------------|
  | `entryGuid`       | `string`   | **Required**. GUID of an entry                                                   |
  | `entriesToDelete` | `String`   | **Required**. "all" to Delete all entries and "archived" to delete only archived |