package com.ling;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author 31229
 */
@SpringBootApplication
public class RelicsAppApplication {

    private static final Logger log = LoggerFactory.getLogger(RelicsAppApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RelicsAppApplication.class, args);
    }

    @Bean
    public CommandLineRunner checkDataSources(
            @Qualifier("mysqlDataSource") DataSource mysqlDataSource,
            @Qualifier("pgVectorDataSource") DataSource pgVectorDataSource) {
        return args -> {
            try {
                log.info("正在测试MySQL数据源连接...");
                mysqlDataSource.getConnection().close();
                log.info("MySQL数据源连接成功！");

                log.info("正在测试PostgreSQL数据源连接...");
                pgVectorDataSource.getConnection().close();
                log.info("PostgreSQL数据源连接成功！");
            } catch (Exception e) {
                log.error("数据源连接测试失败", e);
                throw e;
            }
        };
    }
}

