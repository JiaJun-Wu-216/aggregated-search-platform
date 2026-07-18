package com.chipswu.aggregatedsearchplatform.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Jackson 配置类
 *
 * @author WuJiaJun
 */
@Configuration
public class JacksonConfig {

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /**
     * 方式一（推荐）：通过 Customizer 定制，与 Spring Boot 自动配置完美融合
     * 不会覆盖 application.yml 中 spring.jackson.* 的其他配置
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // ========== Long → String ==========
            SimpleModule longModule = new SimpleModule();
            longModule.addSerializer(Long.class, ToStringSerializer.instance);
            longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.modules(longModule);

            // ========== LocalDateTime ==========
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);
            builder.serializerByType(java.time.LocalDateTime.class,
                    new LocalDateTimeSerializer(dateTimeFormatter));
            builder.deserializerByType(java.time.LocalDateTime.class,
                    new LocalDateTimeDeserializer(dateTimeFormatter));

            // ========== LocalDate ==========
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN);
            builder.serializerByType(java.time.LocalDate.class,
                    new LocalDateSerializer(dateFormatter));
            builder.deserializerByType(java.time.LocalDate.class,
                    new LocalDateDeserializer(dateFormatter));

            // ========== LocalTime ==========
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_PATTERN);
            builder.serializerByType(java.time.LocalTime.class,
                    new LocalTimeSerializer(timeFormatter));
            builder.deserializerByType(java.time.LocalTime.class,
                    new LocalTimeDeserializer(timeFormatter));
        };
    }
}