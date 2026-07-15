package com.chipswu.aggregatedsearchplatform;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
@MapperScan("com.chipswu.aggregatedsearchplatform.mapper")
@SpringBootApplication
public class AggregatedSearchPlatformApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AggregatedSearchPlatformApplication.class);
        ConfigurableEnvironment environment = application.run(args).getEnvironment();
        log.info("【项目】启动成功......");
        log.info("地址：\thttp://127.0.0.1:{}{}",
                environment.getProperty("server.port"),
                environment.getProperty("server.servlet.context-path") == null ? "" : environment.getProperty("server.servlet.context-path"));
    }

}
