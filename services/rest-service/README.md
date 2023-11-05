# Notes REST service



## REST API details


#### Payload for create and update requests

  ```json
  {
    "guid": "",
    "externalGuid": "External-system-GUID",
    "threadGuidParent": "",
    "entryGuid": "",
    "content":"This world is beautiful!"
  }
  ```



## REST API endpoints


#### Create new entry

  ```http
  POST /api/v1/create
  ```
  | Field          | Type     | Description                        |
  |----------------|----------|------------------------------------|
  | `externalGuid` | `UUID`   | **Required**. External system GUID |
  | `content`      | `string` | **Required**. Content              |


#### Create thread entry

  ```http
  POST /api/v1/create
  ```
  | Field              | Type     | Description                                                  |
  |--------------------|----------|--------------------------------------------------------------|
  | `threadGuidParent` | `UUID`   | **Required**. Thread GUID of parent to create an entry under |
  | `content`          | `string` | **Required**. Content                                        | 


#### Update entry either by entryGuid / guid. Either of them needs to be provided

  ```http
  PUT /api/v1/update
  ```
  | Field       | Type     | Description                                                                     |
  |-------------|----------|---------------------------------------------------------------------------------|
  | `guid`      | `UUID`   | **Optional**. Key GUID of an entry, if its not provided, must provide entryGuid |
  | `entryGuid` | `UUID`   | **Optional**. entryId to update. if its not provided, must provide guid         |
  | `content`   | `string` | **Required**. Content                                                           |


#### Search by externalGuid

  ```http
  GET /api/v1/search/external?externalGuid=External-ID&getUpdateHistory=true&getArchivedResponse=true
  ```
  | QueryParams           | Type      | Description                                                                      |
  |-----------------------|-----------|----------------------------------------------------------------------------------|
  | `externalGuid`        | `UUID`    | **Required**. GUID of an external system                                         |
  | `getUpdateHistory`    | `boolean` | **Optional**. **default false** true to Include history of this entry            |
  | `getArchivedResponse` | `boolean` | **Optional**. **default false** true to Include archived entries if exists       |


#### Search by entryGuid

  ```http
  GET /api/v1/search/entry?entryGuid=EntryID&getUpdateHistory=true&getArchivedResponse=true
  ```
  | QueryParams           | Type      | Description                                                                   |
  |-----------------------|-----------|-------------------------------------------------------------------------------|
  | `entryGuid`           | `UUID`    | **Required**. GUID of an entry                                                |
  | `getUpdateHistory`    | `boolean` | **Optional**. **default false** true to include all history of this entry     |
  | `getArchivedResponse` | `boolean` | **Optional**. **default false** true to include all archived entries if exits |


#### Archive by ExternalGuid

  ```http
  PUT /api/v1/archive/external?externalGuid=
  ```
  | QueryParams    | Type   | Description                            |
  |----------------|--------|----------------------------------------|
  | `externalGuid` | `UUID` | **Required**. GUID of external system  |


#### Archive by entryGuid

  ```http
  PUT /api/v1/archive/entry?entryGuid=
  ```
  | QueryParams | Type     | Description                        |
  |-------------|----------|------------------------------------|
  | `entryGuid` | `string` | **Required**. GUID of an entryGuid |


#### Search archived entries by externalGuid

  ```http
  GET /api/v1/search/archive/external?externalGuid=
  ```
  | QueryParams    | Type   | Description                            |
  |----------------|--------|----------------------------------------|
  | `externalGuid` | `UUID` | **Required**. GUID of external system  |

#### Search archived entries by entryGuid

  ```http
  GET /api/v1/search/archive/entry?entryGuid=
  ```
  | QueryParams | Type   | Description                        |
  |-------------|--------|------------------------------------|
  | `entryGuid` | `UUID` | **Required**. GUID of an entryGuid |


#### Delete by externalGuid

  ```http
  DELETE /api/v1/delete/external?externalGuid=&entriesToDelete=archived
  ```
  | QueryParams       | Type     | Description                                                                      |
  |-------------------|----------|----------------------------------------------------------------------------------|
  | `externalGuid`    | `UUID`   | **Required**. GUID of external system                                            |
  | `entriesToDelete` | `string` | **Required**. "all" to delete all entries and "archived" to delete only archived |


#### Delete by entryGuid

  ```http
  DELETE /api/v1/delete/entry?entryGuid=&entriesToDelete=archived
  ```
  | QueryParams       | Type     | Description                                                                      |
  |-------------------|----------|----------------------------------------------------------------------------------|
  | `entryGuid`       | `UUID`   | **Required**. GUID of an entry                                                   |
  | `entriesToDelete` | `string` | **Required**. "all" to delete all entries and "archived" to delete only archived |
