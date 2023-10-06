package com.freelance.forum.resource;

import com.freelance.forum.elasticsearch.pojo.ElasticDataModel;
import com.freelance.forum.elasticsearch.pojo.GUIDType;
import com.freelance.forum.service.ESService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ESResource {

    @Autowired
    ESService service;

    @PostMapping("/create")
    public ElasticDataModel saveNew(@RequestBody ElasticDataModel elasticDataModel) {
        return service.saveNew(elasticDataModel);
    }

    @GetMapping("/search/guid")
    public ElasticDataModel searchByGuid(@RequestParam String guid) {
        return service.searchByGuid(guid);
    }

    @PutMapping("/update")
    public ElasticDataModel update(@RequestBody ElasticDataModel elasticDataModel) {
        return service.updateByEntryGuid(elasticDataModel);
    }

    @GetMapping("/search/externalGuid")
    public ElasticDataModel searchByExternalGuid(@RequestParam String externalGuid,@RequestParam boolean searchUpdateHistory,
                                                @RequestParam boolean sendArchivedResponse) {
        return service.searchByGuid(externalGuid,searchUpdateHistory,sendArchivedResponse,true, GUIDType.EXTERNAL);
    }

    @GetMapping("/search/entryGuid")
    public ElasticDataModel searchByEntryGuid(@RequestParam String entryGuid,@RequestParam boolean searchUpdateHistory,
                                             @RequestParam boolean sendArchivedResponse) {
        return service.searchByGuid(entryGuid,searchUpdateHistory,sendArchivedResponse,true,GUIDType.ENTRY);
    }

    @PutMapping("/archive/externalGuid")
    public ElasticDataModel archiveExternalGuid(@RequestParam String externalGuid) {
        return service.archive(externalGuid,GUIDType.EXTERNAL);
    }

    @PutMapping("/archive/entryGuid")
    public ElasticDataModel archiveEntryGuid(@RequestParam String entryGuid) {
        return service.archive(entryGuid,GUIDType.EXTERNAL);
    }

    @PostMapping("/createIndex")
    public String createIndex(@RequestParam String index) {
        return service.createIndex(index);
    }
}