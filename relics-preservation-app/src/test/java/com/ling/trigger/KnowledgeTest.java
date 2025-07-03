package com.ling.trigger;

import com.ling.trigger.http.KnowledgeController;
import com.ling.types.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


/**
 * @Author: LingRJ
 * @Description: 知识库测试
 * @DateTime: 2025/7/3 16:31
 **/
@SpringBootTest
@Slf4j
public class KnowledgeTest {
    @Autowired
    private KnowledgeController controller;

    @Test
    public void controller_test() {
        Response<String> chatResponse = controller.generateResponse("你还好吗", null);
        log.info("chatResponse: {}", chatResponse);
    }
}
