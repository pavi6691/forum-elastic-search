# ToDo's

- [x] In general use timestamps as `yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX`. Currently `"created"` value is stored with only 3 digits for milliseconds.
- [x] When saving data to Elasticsearch:
  - [x] Do not store `"_class" : "com.freelance.forum.elasticsearch.pojo.NotesData"`.
  - [x] Do not store `"threads" : [ ]` when it has no content.
  - [x] Do not store `"history" : [ ]` when it has no content.
  - [x] Do not store null values. I haven't seen any yet, but just to make sure.
- [x] GET `/api/v1/search/external?externalGuid=...`
  - When searching for an `externalGuid` it only returns the first document found. It should return all documents that has that `externalGuid`.
- [ ] First and foremost, Refactor, simplify and optimize code and give design perspective to it. Use different strategy to consume data from elasticsearch and process it as -
  - [ ] Current implementation has incremental calls to elasticsearch for every thread. there are going to be many call in single request.
  - [ ] Provide different strategy, where there will be 1 or 2 calls/search request to elastic search and get the response in one go and process them
    internally. Both implementation can be kept with flag
- [ ] Add logs, handle exceptions and other validations if any
- [ ] Swagger. SpringDoc open api 3
- [ ] pagination
- [ ] JUnit Test Cases
- [ ] POC on Logging and tracing
- [ ] Authentication and authorization
- [ ] Scalability - lstio API gateway?
- [ ] Futurist code with open search API support, flag based to switch between rest hight level client and open search