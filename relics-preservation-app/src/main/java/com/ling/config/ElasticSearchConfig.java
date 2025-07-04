package com.ling.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;


/**
 * @Author: LingRJ
 * @Description: es搜索配置
 * @DateTime: 2025/7/4 10:22
 **/
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticSearchConfig {

    // 是否开启ES
    private Boolean open;

    // es 集群host ip 地址
    private String hosts;

    // es用户名
    private String userName;

    // es密码
    private String password;

    // es 请求方式
    private String scheme;

    // es集群名称
    private String clusterName;

    // es 连接超时时间
    private int connectTimeOut;

    // es socket 连接超时时间
    private int socketTimeOut;

    // es 请求超时时间
    private int connectionRequestTimeOut;

    // es 最大连接数
    private int maxConnectNum;

    // es 每个路由的最大连接数
    private int maxConnectNumPerRoute;

    // es api key
    private String apiKey;

    @Bean
    public RestClient restClient() {
        log.info("初始化 Elasticsearch RestClient，hosts: {}", hosts);
        String[] hostArray = hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostArray.length];

        for (int i = 0; i < hostArray.length; i++) {
            String[] hostAndPort = hostArray[i].split(":");
            String host = hostAndPort[0];
            int port = Integer.parseInt(hostAndPort[1]);
            httpHosts[i] = new HttpHost(host, port, scheme);
        }

        RestClientBuilder builder = RestClient.builder(httpHosts);

        // 配置认证信息
        if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        // 配置连接超时
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder
                        .setConnectTimeout(connectTimeOut)
                        .setSocketTimeout(socketTimeOut)
                        .setConnectionRequestTimeout(connectionRequestTimeOut));

        return builder.build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        log.info("初始化 ElasticsearchTransport");
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public @NotNull ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        log.info("初始化 ElasticsearchClient");
        return new ElasticsearchClient(transport);
    }
}
