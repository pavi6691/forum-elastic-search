package com.freelance.forum.elasticsearch.repository;
import com.freelance.forum.elasticsearch.pojo.ElasticDataModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ESRepository extends ElasticsearchRepository<ElasticDataModel, String> {
    
}
