package com.ling.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @Author: LingRJ
 * @Description: MyBatis配置
 * @DateTime: 2025/6/28 21:35
 **/
@Configuration
@MapperScan(basePackages = {"com.ling.infrastructure.dao"}, sqlSessionFactoryRef = "mysqlSqlSessionFactory")
public class MyBatisConfig {

    @Bean("mysqlSqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("mysqlDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // 设置MyBatis映射文件的位置
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:/mybatis/*.xml"));
        
        return factoryBean.getObject();
    }
} 