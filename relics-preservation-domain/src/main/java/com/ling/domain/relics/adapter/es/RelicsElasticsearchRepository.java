package com.ling.domain.relics.adapter.es;

import com.ling.domain.relics.model.document.RelicsDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @Author: LingRJ
* @Description: es仓储
* @DateTime: 2025/7/4 10:50
**/
@Repository
public interface RelicsElasticsearchRepository extends ElasticsearchRepository<RelicsDocument, String> {
    /**
     * 根据文物名称搜索
     * @param name 文物名称
     * @return 文物文档列表
     */
    List<RelicsDocument> findByNameContaining(String name);
    
    /**
     * 多字段搜索文物
     * 搜索名称、朝代、类别、描述、材质等字段
     * 任意一个字段匹配就返回结果
     * 使用bool查询增加召回率
     * @param keyword 搜索关键词
     * @return 文物文档列表
     */
    @Query("{\"bool\": {\"should\": [" +
           "{\"match\": {\"name\": {\"query\": \"?0\", \"boost\": 3.0}}}," +
           "{\"wildcard\": {\"name\": {\"value\": \"*?0*\", \"boost\": 2.5}}}," +
           "{\"match\": {\"description\": {\"query\": \"?0\", \"boost\": 2.0}}}," +
           "{\"wildcard\": {\"description\": {\"value\": \"*?0*\", \"boost\": 1.5}}}," +
           "{\"match\": {\"era\": {\"query\": \"?0\", \"boost\": 1.0}}}," +
           "{\"wildcard\": {\"era\": {\"value\": \"*?0*\"}}}," +
           "{\"match\": {\"category\": {\"query\": \"?0\", \"boost\": 1.0}}}," +
           "{\"wildcard\": {\"category\": {\"value\": \"*?0*\"}}}," +
           "{\"match\": {\"material\": {\"query\": \"?0\", \"boost\": 1.0}}}," +
           "{\"wildcard\": {\"material\": {\"value\": \"*?0*\"}}}" +
           "], \"minimum_should_match\": 1}}")
    List<RelicsDocument> searchByMultiField(String keyword);
}
