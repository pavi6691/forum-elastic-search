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
    
    @GetMapping("/findByGuid/{guid}")
    public ElasticDataModel findByGuid(@PathVariable String guid) {
        return service.findByGuid(guid);
    }

    @PutMapping("/update/{copyAchievedResponse}")
    public ElasticDataModel update(@RequestBody ElasticDataModel elasticDataModel,@PathVariable boolean copyAchievedResponse) {
        return service.updateByEntryGuid(elasticDataModel,copyAchievedResponse);
    }

    @GetMapping("/findByExternalGuid/{externalGuid}/{sendArchivedResponse}")
    public ElasticDataModel queryByExternalGuid(@PathVariable String externalGuid,@PathVariable boolean sendArchivedResponse) {
        return service.queryGuid(externalGuid,sendArchivedResponse, GUIDType.EXTERNAL);
    }

    @GetMapping("/findByEntryGuid/{entryGuid}/{sendArchivedResponse}")
    public ElasticDataModel queryByEntryGuid(@PathVariable String entryGuid,@PathVariable boolean sendArchivedResponse) {
        return service.queryGuid(entryGuid,sendArchivedResponse,GUIDType.ENTRY);
    }
    @PostMapping("/createIndex/{index}")
    public String createIndex(@PathVariable String index) {
        return service.createIndex(index);
    }
}
