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
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.stringtemplate.v4.ST;
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

    @RequestMapping(value = "/ai", method = RequestMethod.POST)
    public Response<String> generateResponse(@RequestBody String message, @RequestBody(required = false) String ragTag) {
        // 检查message参数是否为空
        if (message == null || message.trim().isEmpty()) {
            log.error("消息内容不能为空");
            return Response.<String>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("消息不能为空")
                    .data(null)
                    .build();
        }

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
            requestBuilder.filterExpression("tag == '" + ragTag + "'");
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

        List<Message> messages = new ArrayList<>();
        // 确保使用trim后的消息
        messages.add(new UserMessage(message.trim()));

        // 检查是否找到了文档
        if (documents.isEmpty()) {
            // 如果没有找到文档，添加一个默认的系统消息
            messages.add(new SystemMessage(
                    "我的知识库中没有关于这个问题的具体信息。我将尝试根据一般知识回答您的问题。"));
            log.info("未找到相关文档，使用默认提示");
        } else {
            // 找到了文档，使用文档内容创建消息
            String documentCollectors = documents.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
            Message ragMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(Map.of("documents", documentCollectors));
            messages.add(ragMessage);
            log.info("使用找到的文档创建提示");
        }

        // 创建并返回流式响应
        Prompt prompt = new Prompt(messages, OpenAiChatOptions.builder().streamUsage(true).build());
        log.info("prompt: {}", prompt);
        return Response.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(chatClient.prompt(prompt).user(message).call().content())
                .build();
    }


    @Data
    public static class KnowledgeVO {
        private String content;
        private Map<String, Object> metadata;
    }
}
