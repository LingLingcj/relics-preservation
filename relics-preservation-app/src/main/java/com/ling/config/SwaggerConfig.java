package com.ling.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI relicsPreservationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("文物保护系统API文档")
                        .description("文物保护系统后端接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("LingRJ")
                                .email("3122973174@qq.com"))
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档")
                        .url("https://github.com/LingLingcj/relics-preservation"))
                // 添加JWT安全配置
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .schemaRequirement("JWT", new SecurityScheme()
                        .name("JWT")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .description("请在此输入JWT令牌"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth-api")
                .pathsToMatch("/api/auth/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user-api")
                .pathsToMatch("/api/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi commentReviewApi() {
        return GroupedOpenApi.builder()
                .group("comment-review-api")
                .displayName("评论审核管理")
                .pathsToMatch("/api/v1/admin/comments/**")
                .build();
    }

    @Bean
    public GroupedOpenApi permissionTestApi() {
        return GroupedOpenApi.builder()
                .group("permission-test-api")
                .displayName("权限测试")
                .pathsToMatch("/api/test/**")
                .build();
    }

    @Bean
    public GroupedOpenApi relicsApi() {
        return GroupedOpenApi.builder()
                .group("relics-api")
                .displayName("文物管理")
                .pathsToMatch("/api/relics/**")
                .build();
    }

    @Bean
    public GroupedOpenApi interactionApi() {
        return GroupedOpenApi.builder()
                .group("interaction-api")
                .displayName("用户交互")
                .pathsToMatch("/api/interaction/**", "/api/favorite/**", "/api/comment/**")
                .build();
    }

    @Bean
    public GroupedOpenApi knowledgeApi() {
        return GroupedOpenApi.builder()
                .group("knowledge-api")
                .displayName("知识问答")
                .pathsToMatch("/api/knowledge/**")
                .build();
    }

    @Bean
    public GroupedOpenApi sensorApi() {
        return GroupedOpenApi.builder()
                .group("sensor-api")
                .displayName("传感器监控")
                .pathsToMatch("/api/sensor/**")
                .build();
    }
}