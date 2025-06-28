package com.ling.trigger.http;

import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: LingRJ
 * @Description: 知识库接口
 * @DateTime: 2025/6/28 0:04
 **/
@RestController
@RequestMapping("/api/knowledge/rag")
@Slf4j
public class KnowledgeController {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private PgVectorStore pgVectorStore;
    @Resource
    private TokenTextSplitter tokenTextSplitter;
    @Resource
    private ChatClient chatClient;

    // 添加知识
    @PostMapping
     Response<String> uploadFile(@RequestParam String ragTag, @RequestParam List<MultipartFile> files) {
        log.info("上传知识库开始 {}", ragTag);
        for(MultipartFile file : files) {
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(file.getResource());
            List<Document> documents = tikaDocumentReader.get();
            List<Document> documentSplitterList = tokenTextSplitter.split(documents);

            documentSplitterList.forEach(document -> document.getMetadata().put("tag", ragTag));

            pgVectorStore.accept(documentSplitterList);

            RList<String> elements = redissonClient.getList("ragTag");
            if(!elements.contains(ragTag)) {
                elements.add(ragTag);
            }
        }
        log.info("上传知识库完成 {}", ragTag);

        return Response.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .build();
    };


    @Data
    public static class KnowledgeVO {
        private String content;
        private Map<String, Object> metadata;
    }
}
