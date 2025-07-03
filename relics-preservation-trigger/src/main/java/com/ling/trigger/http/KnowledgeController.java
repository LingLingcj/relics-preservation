package com.ling.trigger.http;

import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @RequestMapping(value = "/ai", method = RequestMethod.GET)
    public Flux<ChatResponse> generateStreamRag(@RequestParam String message, @RequestParam(required = false) String ragTag) {

        String SYSTEM_PROMPT = """
                Use the information from the DOCUMENTS section to provide accurate answers but act as if you knew this information innately.
                If unsure, simply state that you don't know.
                Another thing you need to note is that your reply must be in Chinese!
                DOCUMENTS:
                    {documents}
                """;

        // 构建搜索请求
        SearchRequest.Builder requestBuilder = SearchRequest.builder().query(message).topK(5);
        
        // 如果指定了ragTag，则添加过滤条件
        if (ragTag != null && !ragTag.isEmpty()) {
            requestBuilder.filter(Map.of("tag", ragTag));
            log.info("使用指定知识库标签进行查询: {}", ragTag);
        } else {
            // 如果没有指定ragTag，查询所有可用标签的内容（不添加过滤器）
            RList<String> ragTags = redissonClient.getList("ragTag");
            log.info("未指定标签，在所有知识库中查询: {}", ragTags);
            // 不添加filter，默认在所有知识库中搜索
        }

        SearchRequest request = requestBuilder.build();
        log.info("执行向量搜索，查询: {}", message);

        List<Document> documents = pgVectorStore.similaritySearch(request);
        log.info("找到相关文档数量: {}", documents.size());
        
        String documentCollectors = documents.stream().map(Document::getText).collect(Collectors.joining());
        Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));

        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(message));
        messages.add(ragMessage);

        // 创建并返回流式响应
        Prompt prompt = new Prompt(messages, OpenAiChatOptions.builder().withStreaming(true).build());
        return chatClient.stream(prompt);
    }


    @Data
    public static class KnowledgeVO {
        private String content;
        private Map<String, Object> metadata;
    }
}
