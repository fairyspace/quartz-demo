package io.github.fairyspace.quartzdemo.config;


import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceConfig {

    //quartz数据库 dataSourceTask数据源，使用@QuartzDataSource后，不需要动态配置
    @Bean(name = "dataSourceTask")
    @ConfigurationProperties("spring.datasource.task")
    @QuartzDataSource
    public DruidDataSource dataSourceTask () { return DruidDataSourceBuilder.create().build(); }

    //将两个数据源添加至动态数据源配置类中

}
