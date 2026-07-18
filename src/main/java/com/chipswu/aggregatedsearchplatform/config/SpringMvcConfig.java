package com.chipswu.aggregatedsearchplatform.config;

import com.chipswu.aggregatedsearchplatform.interceptor.LogInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Spring MVC 配置类
 *
 * @author WuJiaJun
 */
@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

    @Resource
    private LogInterceptor logInterceptor;

    /**
     * 添加拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册日志拦截器
        registry.addInterceptor(logInterceptor).addPathPatterns("/**");
    }

    /**
     * 全局跨域配置
     *
     * @param registry 跨域配置注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}