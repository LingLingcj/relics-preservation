package com.ling.config;

import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.DefaultChatClientBuilder;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * @Author: LingRJ
 * @Description: openai配置
 * @DateTime: 2025/6/28 10:00
 **/
@Configuration
public class OpenAiConfig {
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public OpenAiApi openAiApi(@Value("${spring.ai.openai.base-url}") String baseUrl, @Value("${spring.ai.openai.api-key}") String apiKey ) {
        return OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
    }

    @Bean("syncMcpToolCallbackProvider")
    public SyncMcpToolCallbackProvider syncMcpToolCallbackProvider(List<McpSyncClient> mcpSyncClients) {
        // 记录mcp name和index
        Map<String, Integer> nameToIndex = new HashMap<>();
        // 记录重复的 index
        Set<Integer> duplicateIndexes = new HashSet<>();

        for (int i = 0 ; i < mcpSyncClients.size() ; i++) {
            String name = mcpSyncClients.get(i).getServerInfo().name();
            if (!nameToIndex.containsKey(name)) {
                nameToIndex.put(name, i);
            } else {
                duplicateIndexes.add(i);
            }
        }
        // 删除重复的文件
        List<Integer> sortedIndexes = new ArrayList<>(duplicateIndexes);
        sortedIndexes.sort(Collections.reverseOrder());
        for (int index : sortedIndexes) {
            mcpSyncClients.remove(index);
        }

        return new SyncMcpToolCallbackProvider(mcpSyncClients);
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 @Qualifier("syncMcpToolCallbackProvider") SyncMcpToolCallbackProvider syncMcpToolCallbackProvider,
                                 ChatMemory chatMemory) {
        DefaultChatClientBuilder chatClientBuilder = new DefaultChatClientBuilder(openAiChatModel, ObservationRegistry.NOOP, null);
        return chatClientBuilder
                .defaultTools(syncMcpToolCallbackProvider)
                .defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory))
                .build();
    }
    
    @Bean
    public OpenAiEmbeddingModel openAiEmbeddingModel(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingModel(openAiApi);
    }
}
