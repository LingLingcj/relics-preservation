package com.ling.config;

import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * @Author: LingRJ
 * @Description: 向量存储配置
 * @DateTime: 2025/6/28 12:00
 **/
@Configuration
public class VectorStoreConfig {

    @Bean("pgVectorDataSource")
    public DataSource pgVectorDataSource(
            @Value("${spring.ai.vectorstore.pgvector.url}") String url,
            @Value("${spring.ai.vectorstore.pgvector.username}") String username,
            @Value("${spring.ai.vectorstore.pgvector.password}") String password) {
        
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
    
    @Bean("pgJdbcTemplate")
    public JdbcTemplate pgJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    public PgVectorStore pgVectorStore(
            OpenAiApi openAiApi,
            @Qualifier("pgJdbcTemplate") JdbcTemplate jdbcTemplate) {
        
        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(openAiApi);
        
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .build();

    }
}
