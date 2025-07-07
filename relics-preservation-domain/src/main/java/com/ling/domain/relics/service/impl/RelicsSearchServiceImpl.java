package com.ling.domain.relics.service.impl;

import com.ling.domain.relics.adapter.IRelicsRepository;
import com.ling.domain.relics.adapter.es.RelicsElasticsearchRepository;
import com.ling.domain.relics.model.document.RelicsDocument;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.service.IRelicsSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RelicsSearchServiceImpl implements IRelicsSearchService {

    @Value("${elasticsearch.open:false}")
    private boolean elasticsearchEnabled;
    
    @Autowired
    private RelicsElasticsearchRepository relicsElasticsearchRepository;
    
    @Autowired
    private IRelicsRepository relicsRepository;

    @Override
    public List<RelicsEntity> searchRelicsByName(String name) {
        if (!elasticsearchEnabled) {
            log.warn("Elasticsearch未启用，使用数据库查询");
            // 如果ES未启用，可以从数据库中模糊查询
            return relicsRepository.findByNameContaining(name);
        }
        
        log.info("使用Elasticsearch搜索文物，关键词: {}", name);
        try {
            List<RelicsDocument> documents = relicsElasticsearchRepository.findByNameContaining(name);
            // 转换为实体对象
            List<RelicsEntity> entities = documents.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

            return entities;
        } catch (Exception e) {
            log.error("Elasticsearch搜索文物失败", e);
            // ES查询失败时，回退到数据库查询
            log.info("回退到数据库查询");
            return relicsRepository.findByNameContaining(name);
        }
    }

    @Override
    public List<RelicsEntity> searchRelicsByKeyword(String keyword) {
        log.info("使用Elasticsearch多字段搜索文物，关键词: {}", keyword);
        try {
            // 调用ES的多字段搜索
            List<RelicsDocument> documents = relicsElasticsearchRepository.searchByMultiField(keyword);
            // 转换为实体对象
            List<RelicsEntity> entities = documents.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

            return entities;
        } catch (Exception e) {
            log.error("Elasticsearch多字段搜索文物失败", e);
            // ES查询失败时，回退到数据库查询
            log.info("回退到数据库查询, 查询名称");
            return relicsRepository.findByNameContaining(keyword);
        }
    }

    @Override
    public boolean syncRelicsToEs(RelicsEntity relicsEntity) {
        try {
            RelicsDocument document = convertToDocument(relicsEntity);
            relicsElasticsearchRepository.save(document);
            log.info("文物数据同步到ES成功, ID: {}, 名称: {}", relicsEntity.getRelicsId(), relicsEntity.getName());
            return true;
        } catch (Exception e) {
            log.error("文物数据同步到ES失败", e);
            return false;
        }
    }

    @Override
    public boolean syncAllRelicsToEs() {
        try {
            // 清空当前索引中的所有数据，避免重复
            clearRelicsIndex();
            
            // 获取所有文物数据
            List<RelicsEntity> allRelics = getAllRelics();
            if (allRelics.isEmpty()) {
                log.warn("没有文物数据可同步");
                return false;
            }
            
            List<RelicsDocument> documents = allRelics.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());
            
            relicsElasticsearchRepository.saveAll(documents);
            log.info("所有文物数据同步到ES成功, 共 {} 条", documents.size());
            return true;
        } catch (Exception e) {
            log.error("所有文物数据同步到ES失败", e);
            return false;
        }
    }
    
    /**
     * 清空文物索引
     */
    private void clearRelicsIndex() {
        try {
            log.info("清空文物索引数据");
            relicsElasticsearchRepository.deleteAll();
            log.info("文物索引数据清空完成");
        } catch (Exception e) {
            log.error("清空文物索引数据失败", e);
        }
    }
    
    /**
     * 实体转文档
     */
    private RelicsDocument convertToDocument(RelicsEntity entity) {
        RelicsDocument document = new RelicsDocument();
        BeanUtils.copyProperties(entity, document);
        document.setImageUrl(entity.getImageUrl());
        
        // 确保使用业务ID作为文档ID
        if (entity.getRelicsId() != null) {
            document.setRelicsId(String.valueOf(entity.getRelicsId()));
        } else {
            // 如果实体没有ID，生成一个基于名称和类别的唯一标识
            String uniqueId = entity.getName() + "_" + entity.getCategory();
            document.setRelicsId(uniqueId);
            log.info("实体没有ID，使用名称和类别生成唯一标识: {}", uniqueId);
        }
        return document;
    }
    
    /**
     * 文档转实体
     */
    private RelicsEntity convertToEntity(RelicsDocument document) {
        RelicsEntity entity = new RelicsEntity();
        BeanUtils.copyProperties(document, entity);
        
        // 对于从ES查询的结果，依然尝试转换ID，但不要使用ES自动生成的ID
        // relicsId应该是业务数据中原有的ID，而不是ES生成的文档ID
        if (document.getRelicsId() != null) {
            try {
                entity.setRelicsId(Long.valueOf(document.getRelicsId()));
            } catch (NumberFormatException e) {
                // 如果确实无法转换，说明可能是ES生成的ID，这里我们不使用它
                log.warn("无法将文档ID转换为Long: {}", document.getRelicsId());
                entity.setRelicsId(null);
            }
        }
        
        entity.setSuccess(true);
        return entity;
    }
    
    /**
     * 获取所有文物数据
     */
    private List<RelicsEntity> getAllRelics() {
        try {
            return relicsRepository.findAll();
        } catch (Exception e) {
            log.error("获取所有文物数据失败", e);
            return new ArrayList<>();
        }
    }
} 