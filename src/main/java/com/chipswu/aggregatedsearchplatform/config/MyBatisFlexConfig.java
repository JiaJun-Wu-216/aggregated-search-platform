package com.chipswu.aggregatedsearchplatform.config;

import com.mybatisflex.core.audit.AuditManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MyBatisFlexConfig {

    @PostConstruct
    public void initSqlAudit() {
        AuditManager.setAuditEnable(true);
        AuditManager.setMessageCollector(new AuditSqlMessageCollector());
        log.info("MyBatis-Flex SQL审计已启用，慢SQL阈值={}ms", 500);
    }
}