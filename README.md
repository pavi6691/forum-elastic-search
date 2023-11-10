# Notes

...TBD...



## Structure

| Folder/File                                  | Description                                                                     |
|----------------------------------------------|---------------------------------------------------------------------------------|
| [clients/](clients/README.md)                | Vanilla Java client(s) for interacting with the service **Not yet implemented** |
| [core/](core/README.md)                      | Vanilla Java classes/enums etc that is shared between clients and services      |
| [models/](models/README.md)                  | Vanilla Java records used as requests and response models                       |
| [services/](services/rest-service/README.md) | Java Spring Boot microservice                                                   |
| [documentation/](documentation/README.md)    | General documentation (links, diagrams, images etc)                             |



## Run configurations

#### IntelliJ IDEA Ultimate

The folder [.run/](.run/) contains IntelliJ IDEA Run configurations.



## Docker

Execute below command to start Elasticsearch + Kibana

```shell
cd Docker
docker compose up
```

Then connect to Kibana using http://localhost:5601
