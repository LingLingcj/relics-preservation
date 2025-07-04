package com.ling.trigger.http;

import com.ling.types.common.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: LingRJ
 * @Description: 传感器数据分析控制器
 * @DateTime: 2025/7/5
 **/
@RestController
@RequestMapping("/api/sensor/analysis")
@Slf4j
public class SensorAnalysisController {

    @Autowired
    private ChatClient chatClient;


    private static final String SENSOR_ANALYSIS_PROMPT =
            """
                    请分析文物传感器数据，对各类传感器(温度、湿度、光照、气体)数据进行分析，包括以下内容：
                    1. 各类传感器数据的变化趋势分析
                    2. 检测是否有异常值，如果有请指出
                    3. 根据不同类型传感器数据分析文物保存环境是否适宜
                    4. 给出改善文物保存环境的建议
                    5. 分析数据波动与外部环境因素的可能关联
                    请以专业、清晰的方式呈现分析结果，可以使用表格和分点说明以提高可读性。""";

    /**
     * 获取传感器数据分析报告
     * @return 传感器数据分析结果
     */
    @GetMapping("/report")
    public Response<String> getSensorAnalysisReport() {

        log.info("开始生成传感器数据分析报告");
        try {
            String analysisResult = chatClient.prompt(SENSOR_ANALYSIS_PROMPT).call().content();
            log.info("成功生成传感器数据分析报告");
            return Response.success(analysisResult);
        } catch (Exception e) {
            log.error("生成传感器数据分析报告失败", e);
            return Response.error("生成分析报告失败：" + e.getMessage());
        }
    }
} 