# Notes REST service



## REST API details



#### Endpoints in each controller

| Controller<br/>-------------<br/>method | PGUserController | PGAdminController | ESUserController | ESAdminController |
|-----------------------------------------|:----------------:|:-----------------:|:----------------:|:-----------------:|
| create                                  |       Yes        |         -         |       Yes        |         -         |
| update                                  |       Yes        |         -         |       Yes        |         -         |
| getAll                                  |        -         |        Yes        |        -         |        Yes        |
| getByGuid                               |       Yes        |         -         |       Yes        |         -         |
| getByEntryGuid                          |       Yes        |         -         |       Yes        |         -         |
| getByExternalGuid                       |    <mark>Yes     |        Yes        |    <mark>Yes     |        Yes        |
| getByThreadGuid                         |       Yes        |         -         |       Yes        |         -         |
| searchContent                           |       Yes        |         -         |       Yes        |         -         |
| archiveByGuid                           |       Yes        |         -         |       Yes        |         -         |
| archiveExternalGuid                     |       Yes        |         -         |       Yes        |         -         |
| archiveEntryGuid                        |       Yes        |         -         |       Yes        |         -         |
| getArchivedByExternalGuid               |       Yes        |         -         |       Yes        |         -         |
| getArchivedByEntryGuid                  |       Yes        |         -         |       Yes        |         -         |
| deleteByGuid                            |        -         |        Yes        |        -         |        Yes        |
| deleteByExternalGuid                    |        -         |        Yes        |        -         |        Yes        |
| deleteByEntryGuid                       | <mark>Yes <sup>1 |        Yes        | <mark>Yes <sup>1 |        Yes        |
| deleteByThreadGuid                      |        -         |        Yes        |        -         |        Yes        |
| deleteArchivedByExternalGuid            |       Yes        |         -         |       Yes        |         -         |
| deleteArchivedByEntryGuid               |       Yes        |         -         |       Yes        |         -         |

Notes:
- Methods marked with <mark>Yes</mark> are currenlty missing.
- <sup>1</sup> In user controllers this should be a soft-deletion, whereas in admin it should hard-delete.



<mark>!!! THE REST OF THIS MARKDOWN CONTENT IS CURRENTLY NOT ALIGNED WITH IMPLEMENTATION !!!</mark>



#### Payload for create and update requests

  ```json
  {
    "guid": "",
    "externalGuid": "External-system-GUID",
    "entryGuidParent": "",
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
  POST /api/v1/notes
  ```
  | Field              | Type     | Description                                                  |
  |--------------------|----------|--------------------------------------------------------------|
  | `entryGuidParent ` | `UUID`   | **Required**. Thread GUID of parent to create an entry under |
  | `content`          | `string` | **Required**. Content                                        | 


#### Update entry either by entryGuid / guid. Either of them needs to be provided

  ```http
  PUT /api/v1/notes
  ```
  | Field       | Type     | Description                                                                     |
  |-------------|----------|---------------------------------------------------------------------------------|
  | `guid`      | `UUID`   | **Optional**. Key GUID of an entry, if its not provided, must provide entryGuid |
  | `entryGuid` | `UUID`   | **Optional**. entryId to update. if its not provided, must provide guid         |
  | `content`   | `string` | **Required**. Content                                                           |


#### Search by externalGuid

  ```http
  GET /api/v1/notes/externalguid/{externalGuid}?includeVersions=true&includeArchived=true
  ```
  | Part  | Param               | Type      | Description                                                                      |
  |-------|---------------------|-----------|----------------------------------------------------------------------------------|
  | Path  | `externalGuid`      | `UUID`    | **Required**. GUID of an external system                                         |
  | Query | `includeVersions`   | `boolean` | **Optional**. **default false** true to Include history of this entry            |
  | Query | `includeArchived`   | `boolean` | **Optional**. **default false** true to Include archived entries if exists       |


#### Search by entryGuid

  ```http
  GET /api/v1/notes/entryguid/{entryGuid}?includeVersions=true&includeArchived=true
  ```
  | Part  | Param               | Type      | Description                                                                   |
  |-------|---------------------|-----------|-------------------------------------------------------------------------------|
  | Path  | `entryGuid`         | `UUID`    | **Required**. GUID of an entry                                                |
  | Query | `includeVersions`   | `boolean` | **Optional**. **default false** true to include all history of this entry     |
  | Query | `includeArchived`   | `boolean` | **Optional**. **default false** true to include all archived entries if exits |


#### Archive by externalGuid

  ```http
  PUT /api/v1/notes/archive/externalguid/{externalGuid}
  ```
  | Part | Param          | Type   | Description                            |
  |------|----------------|--------|----------------------------------------|
  | Path | `externalGuid` | `UUID` | **Required**. GUID of external system  |


#### Archive by entryGuid

  ```http
  PUT /api/v1/notes/archive/entryguid/{entryGuid}
  ```
  | PathParam   | Type     | Description                        |
  |-------------|----------|------------------------------------|
  | `entryGuid` | `string` | **Required**. GUID of an entryGuid |


#### Search archived entries by externalGuid
<mark>Q: Is this a search or just a retrieval?</mark>

  ```http
  GET /api/v1/notes/search/archived/externalguid/{externalGuid}
  ```
  | PathParams     | Type   | Description                            |
  |----------------|--------|----------------------------------------|
  | `externalGuid` | `UUID` | **Required**. GUID of external system  |


#### Search archived entries by entryGuid
<mark>Q: Is this a search or just a retrieval?</mark>

  ```http
  GET /api/v1/notes/search/archived/entryguid/{entryGuid}
  ```
  | PathParams  | Type   | Description                        |
  |-------------|--------|------------------------------------|
  | `entryGuid` | `UUID` | **Required**. GUID of an entryGuid |


#### Delete by externalGuid

  ```http
  DELETE /api/v1/notes/externalguid/{externalGuid}?entriesToDelete=archived
  ```
  | Part  | QueryParams       | Type     | Description                                                                      |
  |-------|-------------------|----------|----------------------------------------------------------------------------------|
  | Path  | `externalGuid`    | `UUID`   | **Required**. GUID of external system                                            |
  | Query | `entriesToDelete` | `string` | **Required**. "all" to delete all entries and "archived" to delete only archived |


#### Delete by entryGuid

  ```http
  DELETE /api/v1/notes/entryguid/{entryGuid}?entriesToDelete=archived
  ```
  | Part  | QueryParams       | Type     | Description                                                                      |
  |-------|-------------------|----------|----------------------------------------------------------------------------------|
  | Path  | `entryGuid`       | `UUID`   | **Required**. GUID of an entry                                                   |
  | Query | `entriesToDelete` | `string` | **Required**. "all" to delete all entries and "archived" to delete only archived |
