package com.ling.domain.relics.adapter.es;

import com.ling.domain.relics.model.document.RelicsDocument;
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
}
