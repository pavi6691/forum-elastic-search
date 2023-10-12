# ToDo's

- In general use timestamps as `yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX`. Currently `"created"` value is stored with only 3 digits for milliseconds.
- When saving data to Elasticsearch:
  - Do not store `"_class" : "com.freelance.forum.elasticsearch.pojo.NotesData"`.
  - Do not store `"threads" : [ ]` when it has no content.
  - Do not store `"history" : [ ]` when it has no content.
  - Do not store null values. I haven't seen any yet, but just to make sure.
- GET `/api/v1/search/external?externalGuid=...`
  - When searching for an `externalGuid` it only returns the first document found. It should return all documents that has that `externalGuid`.
