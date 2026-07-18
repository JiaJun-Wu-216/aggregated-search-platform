package com.chipswu.aggregatedsearchplatform.aop;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * 日志切面（Jackson 版本 + 大对象保护）
 *
 * @author WuJiaJun
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 日志中单个字段最大输出字符数，超出则截断
     */
    private static final int MAX_LOG_LENGTH = 2048;

    /**
     * 参与序列化的参数数组最大元素个数，防止超大集合拖垮性能
     */
    private static final int MAX_ARGS_SERIALIZE_COUNT = 20;

    /**
     * 内部混入类，用于动态属性过滤
     */
    @JsonFilter("dynamicLogFilter")
    private static class DynamicFilterMixin {}

    /**
     * 预构建的 ObjectWriter，线程安全，全局复用
     * 已包含 Mixin、PrettyPrint、FailOnUnknownId 配置
     */
    private final ObjectWriter logWriter;

    /**
     * 注入 Spring 容器中的 ObjectMapper，自动继承 JacksonConfig 中的所有定制
     * （Long→String、LocalDateTime 格式化等）
     */
    public LogAspect(ObjectMapper objectMapper) {
        ObjectMapper logMapper = objectMapper.copy();

        logMapper.addMixIn(Object.class, DynamicFilterMixin.class);

        FilterProvider filterProvider = new SimpleFilterProvider()
                .setFailOnUnknownId(false);

        this.logWriter = logMapper
                .writer(filterProvider);
    }

    @Pointcut("execution(public * com.chipswu..*Controller.*(..))")
    public void controllerPointcut() {
    }

    @Before("controllerPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("无法获取 RequestAttributes，跳过请求日志");
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        Signature signature = joinPoint.getSignature();
        String methodName = signature.getName();

        log.info("------------- 开始 -------------");
        log.info("请求地址: {} {}", request.getRequestURL(), request.getMethod());
        log.info("当前执行方法所在类: {}", signature.getDeclaringTypeName());
        log.info("当前执行方法名: {}", methodName);
        log.info("远程地址: {}", request.getRemoteAddr());

        // ---- 参数预处理 ----
        Object[] args = joinPoint.getArgs();
        Object[] arguments = buildSafeArguments(args);

        // 敏感字段过滤
        String[] excludeProperties = {"checkPassword", "userPassword"};
        log.debug("请求参数: {}", toSafeJson(arguments, excludeProperties));
    }

    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();

        String[] excludeProperties = {};
        log.debug("返回结果: {}", toSafeJson(result, excludeProperties));
        log.info("------------- 结束 耗时：{} ms -------------", System.currentTimeMillis() - startTime);
        return result;
    }

    // ==================== 私有工具方法 ====================

    /**
     * 构建安全的参数数组：
     * 1. 跳过不可序列化的 Servlet/File 类型
     * 2. 限制最大序列化元素个数
     */
    private Object[] buildSafeArguments(Object[] args) {
        if (args == null || args.length == 0) {
            return new Object[0];
        }

        int limit = Math.min(args.length, MAX_ARGS_SERIALIZE_COUNT);
        Object[] safeArgs = new Object[limit];

        for (int i = 0; i < limit; i++) {
            Object arg = args[i];
            switch (arg) {
                case ServletRequest servletRequest -> safeArgs[i] = "[Skipped: ServletRequest]";
                case ServletResponse servletResponse -> safeArgs[i] = "[Skipped: ServletResponse]";
                case MultipartFile file -> safeArgs[i] = String.format("[Skipped: MultipartFile(name=%s, size=%d)]",
                        file.getOriginalFilename(), file.getSize());
                case null, default -> safeArgs[i] = arg;
            }
        }

        if (args.length > MAX_ARGS_SERIALIZE_COUNT) {
            log.warn("方法参数共 {} 个，仅序列化前 {} 个以避免性能问题", args.length, MAX_ARGS_SERIALIZE_COUNT);
        }
        return safeArgs;
    }

    /**
     * 安全 JSON 序列化：带字段过滤 + 大对象截断保护
     */
    private String toSafeJson(Object obj, String[] excludeProperties) {
        if (obj == null) {
            return "null";
        }
        try {
            // 动态设置本次需要排除的字段
            FilterProvider provider = new SimpleFilterProvider()
                    .addFilter("dynamicLogFilter",
                            SimpleBeanPropertyFilter.serializeAllExcept(excludeProperties))
                    .setFailOnUnknownId(false);

            String json = logWriter.with(provider).writeValueAsString(obj);

            // 大对象截断保护
            if (json.length() > MAX_LOG_LENGTH) {
                return json.substring(0, MAX_LOG_LENGTH)
                        + "...[TRUNCATED, total=" + json.length() + " chars]";
            }
            return json;
        } catch (JsonProcessingException e) {
            log.warn("JSON 序列化失败: {}", e.getMessage());
            return "[Serialization Error: " + e.getClass().getSimpleName() + "]";
        } catch (OutOfMemoryError e) {
            log.error("序列化触发 OOM，对象可能过大: {}", e.getMessage());
            return "[Serialization OOM: object too large]";
        }
    }
}