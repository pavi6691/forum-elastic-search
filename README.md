Prototype with some use cases for this project are done. Here is a brief of what's been done so far -
- Code changes to have all setup and elastic search connection is done
- Created resource, service layer and elastic repository interface that supports many operations on elastic search
- Provision for mapping, policies and templates configurable
- Here are APIs to support first 4 requirements:
  - Save new document, provided json on post request
  - Retrieve with key which is GUID supported
  - Retrieve by externalGuid is supported,  support to include archived history as well.
  - Retrieve by threadGuid is supported, supports to include archived history as well.
  - Retrieve by entryGuid is supported, supports to include archived history as well.

Rest we can discuss on uses cases and I can implement in a day.

- Questions
- what is the index name?
- when specific guid is updated, all its answers are called and what is the common ID that 
- when update there can be multiple entries with same UUID
- Know the case of answers and history


- entryGuid is the uniqueID backed needs to be created
- Update requires entryGuid in payload, if not present, throw an exception
- Historical have same entryGuid
- when entryGuidParent provided in create, consider as an answer, search by entryGuid and 
- return null to parentGuuid
- two people editing at the same time, check content and go ahread for update
- archive & delete by ExternalId,

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