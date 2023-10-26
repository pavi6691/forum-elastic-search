### Execute below command to start Elasticsearch + Kibana
    cd Docker
    docker compose up
Then connect to Kibana using http://localhost:5601

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
  `POST /create`
  ```http
  http://localhost:8080/api/v1/create
  ```
  | Field          | Type      | Description                             |
  |:---------------|:----------|:----------------------------------------|
  | `externalGuid` | `string`  | **Required**. Your External System GUID |
  | `content`      | `string`  | **Required**. Content                   |


  2. #### Create thread entry
  `POST /create`
  ```http
  http://localhost:8080/api/v1/create
  ```
  | Field              | Type     | Description                                                  |
  |:-------------------|:---------|:-------------------------------------------------------------|
  | `threadGuidParent` | `string` | **Required**. Thread GUID Of parent to create an entry under |
  | `content`          | `string` | **Required**. Content                                        | 


  3. #### Update - update entry either by entryGuid / guid. either of them needs to be provided
  `PUT /update/entry`
  ```http
  http://localhost:8080/api/v1/update
  ```
  | Field       | Type     | Description                                                                     |
  |:------------|:---------|:--------------------------------------------------------------------------------|
  | `guid`      | `string` | **Optional**. Key GUID of an entry, if its not provided, must provide entryGuid |
  | `entryGuid` | `string` | **Optional**. entryId to Update. if its not provided, must provide guid         |
  | `content`   | `string` | **Required**. Content                                                           |


  4. #### Search - by externalGuid
  `GET /search/external`
  ```http
  http://localhost:8080/api/v1/search/external?externalGuid=External-ID&getUpdateHistory=true&getArchivedResponse=true
  ```
  | QueryParams           | Type      | Description                                                                      |
  |:----------------------|:----------|:---------------------------------------------------------------------------------|
  | `externalGuid`        | `string`  | **Required**. GUID of an external system                                         |
  | `getUpdateHistory`    | `boolean` | **optional**. **default false** true to Include history of this entry            |
  | `getArchivedResponse` | `boolean` | **optional**. **default false** true to Include archived entries if exists       |


  5. #### Search - by entryGuid
  `GET /search/entry`
  ```http
  http://localhost:8080/api/v1/search/entry?entryGuid=EntryID&getUpdateHistory=true&getArchivedResponse=true
  ```
  | QueryParams           | Type      | Description                                                                      |
  |:----------------------|:----------|:---------------------------------------------------------------------------------|
  | `entryGuid`           | `string`  | **Required**. GUID of an entry                                                   |
  | `getUpdateHistory`    | `boolean` | **optional**. **default false** true to Include all history of this entry        |
  | `getArchivedResponse` | `boolean` | **optional**. **default false** true to Include all archived entries if exits    |


  6. #### Archive - By ExternalGuid
  `PUT /archive/external`
  ```http
  http://localhost:8080/api/v1/archive/external?externalGuid=
  ```
  | QueryParams    | Type      | Description                            |
  |:---------------|:----------|:---------------------------------------|
  | `externalGuid` | `string`  | **Required**. GUID of external system  |


  7. #### Archive - By entryGuid
  `PUT /archive/entry`
  ```http
  http://localhost:8080/api/v1/archive/entry?entryGuid=
  ```
  | QueryParams | Type     | Description                        |
  |:------------|:---------|:-----------------------------------|
  | `entryGuid` | `string` | **Required**. GUID of an entryGuid |


  8. #### Search archived entries - By ExternalGuid
  `GET /search/archive/external`
  ```http
  http://localhost:8080/api/v1/search/archive/external?externalGuid=
  ```
  | QueryParams    | Type      | Description                            |
  |:---------------|:----------|:---------------------------------------|
  | `externalGuid` | `string`  | **Required**. GUID of external system  |

  9. #### Search archived entries - By entryGuid
  `GET /search/archive/entry`
  ```http
  http://localhost:8080/api/v1/search/archive/entry?entryGuid=
  ```
  | QueryParams | Type     | Description                        |
  |:------------|:---------|:-----------------------------------|
  | `entryGuid` | `string` | **Required**. GUID of an entryGuid |


  10. #### Delete - By ExternalGuid
  `DELETE /delete/external`
  ```http
  http://localhost:8080/api/v1/delete/external?externalGuid=&entriesToDelete=archived
  ```
  | QueryParams       | Type     | Description                                                                      |
  |:------------------|:---------|:---------------------------------------------------------------------------------|
  | `externalGuid`    | `string` | **Required**. GUID of external system                                            |
  | `entriesToDelete` | `String` | **Required**. "all" to Delete all entries and "archived" to delete only archived |


  11. #### Delete - By entryGuid
  `DELETE /delete/entry`
  ```http
  http://localhost:8080/api/v1/delete/entry?entryGuid=&entriesToDelete=archived
  ```
  | QueryParams       | Type       | Description                                                                      |
  |:------------------|:-----------|:---------------------------------------------------------------------------------|
  | `entryGuid`       | `string`   | **Required**. GUID of an entry                                                   |
  | `entriesToDelete` | `String`   | **Required**. "all" to Delete all entries and "archived" to delete only archived |