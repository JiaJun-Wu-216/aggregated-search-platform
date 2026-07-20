package com.chipswu.aggregatedsearchplatform.config;

import com.mybatisflex.core.audit.AuditMessage;
import com.mybatisflex.core.audit.MessageCollector;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class AuditSqlMessageCollector implements MessageCollector {

    /**
     * 慢SQL阈值（毫秒）
     */
    private static final long SLOW_THRESHOLD_MS = 500;

    /**
     * 定义需要脱敏的字段名集合（小写）
     */
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "passwd", "pwd", "secret", "token",
            "id_card", "idcard", "phone", "mobile", "bank_card"
    );

    @Override
    public void collect(AuditMessage auditMessage) {
        long elapsed = auditMessage.getElapsedTime();
        String fullSql = auditMessage.getFullSql();
        String sqlType = resolveSqlType(fullSql);

        // 获取 LOG_ID（根据实际链路追踪框架调整）
        String logId = MDC.get("LOG_ID");
        if (logId == null) {
            logId = "-";
        }

        // ---------- 分级输出 ----------
        if (elapsed < SLOW_THRESHOLD_MS) {
            // INFO: 常规审计摘要，不输出完整SQL避免日志膨胀
            log.info("[SQL_AUDIT] LOG_ID={} | SQL_TYPE={} | {} ms",
                    logId, sqlType, elapsed);
        } else {
            // WARN: 慢SQL，包含完整SQL和脱敏参数
            log.warn("[SLOW_SQL] LOG_ID={} | SQL_TYPE={} | {} ms | SQL={} | SQL_PARAMS={}",
                    logId, sqlType, elapsed, fullSql, maskParams(auditMessage));
        }

        // DEBUG: 始终记录完整细节，供开发/排查按需开启
        if (log.isDebugEnabled()) {
            log.debug("[SQL_DETAIL] LOG_ID={} | SQL_TYPE={} | {} ms | SQL={} | SQL_PARAMS={}",
                    logId, sqlType, elapsed, fullSql, maskParams(auditMessage));
        }
    }

    /**
     * 从SQL语句中解析操作类型
     */
    private String resolveSqlType(String sql) {
        if (sql == null || sql.isBlank()) {
            return "UNKNOWN";
        }
        String trimmed = sql.stripLeading().toUpperCase();
        if (trimmed.startsWith("SELECT")) return "SELECT";
        if (trimmed.startsWith("INSERT")) return "INSERT";
        if (trimmed.startsWith("UPDATE")) return "UPDATE";
        if (trimmed.startsWith("DELETE")) return "DELETE";
        return "DDL/OTHER";
    }

    /**
     * 对审计消息中的参数进行脱敏处理
     * ⚠️ 注意：MyBatis-Flex 的 AuditMessage 在不同版本中获取参数的方式可能不同，
     * 请根据实际版本调整。以下为通用思路示例。
     */
    private String maskParams(AuditMessage auditMessage) {
        try {
            List<Object> queryParams = auditMessage.getQueryParams();
            if (queryParams == null || queryParams.isEmpty()) {
                return "[]";
            }

            List<String> masked = new ArrayList<>(queryParams.size());
            for (Object param : queryParams) {
                // 1. null 值快速跳过
                if (param == null) {
                    masked.add("null");
                    continue;
                }

                String str = String.valueOf(param);

                // 2. 超长参数保护：防止大文本导致正则回溯灾难或日志膨胀
                //    超过阈值直接标记为大对象，不再尝试脱敏
                if (str.length() > 1024) {
                    masked.add("[LARGE_OBJECT:" + str.length() + "]");
                    continue;
                }

                // 3. 按字段名判断，而非按值的形状
                String fieldName = str.toLowerCase();
                masked.add(SENSITIVE_FIELDS.contains(fieldName) ? "***MASKED***" : str);
            }
            return masked.toString();

        } catch (Exception e) {
            // 审计逻辑绝不能因为自身异常影响主业务流程
            log.debug("SQL审计参数脱敏异常", e);
            return "[params_error]";
        }
    }
}