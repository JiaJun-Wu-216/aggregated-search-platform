package com.chipswu.aggregatedsearchplatform.model.enums;

import lombok.Getter;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索类型枚举
 *
 * @author WuJiaJun
 */
@Getter
public enum SearchTypeEnum {
    ARTICLE("文章", "article"),
    USER("用户", "user"),
    PICTURE("图片", "picture"),
    VIDEO("视频", "video");

    private final String text;

    private final String value;

    SearchTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static SearchTypeEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (SearchTypeEnum anEnum : SearchTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
