package com.ling.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.ClientMcpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpTransport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @Author: LingRJ
 * @Description: mcp
 * @DateTime: 2025/7/3 23:19
 **/
@Service
public class ToolCallService {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private McpSyncClient mcpClient;

    @Value("${mcp.server.url}")
    private String sseServerUrl;

    @PostConstruct
    public void init() {
        try {
            ClientMcpTransport transport = new HttpClientSseClientTransport(sseServerUrl);
            mcpClient = McpClient.sync(transport)
                    .requestTimeout(REQUEST_TIMEOUT)
                    .capabilities(McpSchema.ClientCapabilities.builder()
                            .roots(true)
                            .sampling()
                            .build())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("初始化mcp客户端失败");
        }
    }

    @PreDestroy
    public void destroy() {
        if (mcpClient != null) {
            mcpClient.closeGracefully();
        }
    }
}
