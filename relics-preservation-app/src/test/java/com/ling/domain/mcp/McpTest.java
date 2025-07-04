package com.ling.domain.mcp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: LingRJ
 * @Description: McpTest
 * @DateTime: 2025/7/3 23:51
 **/
@SpringBootTest
@Slf4j
public class McpTest {
    @Autowired
    private ChatClient chatClient;

    @Test
    public void too_test() {
        String userInput = "有哪些工具可以使用";

        System.out.println("\n>>> QUESTION: " + userInput);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
    }

    @Test
    public void tool_test2() {
        String input = "查询sensor_data_hourly，根据type，获取不同的传感器信息，并且分别分析变化情况";
        System.out.println("\n>>> QUESTION: " + input);
        System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(input).call().content());
    }

}
