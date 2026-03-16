package cn.com.toolkit.framework.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DatePatternEnum {
    DATE_PATTERN_1("yyyy-MM-dd"),
    DATE_PATTERN_2("yyyyMMdd"),
    DATE_TIME_PATTERN_1("yyyy-MM-dd HH:mm:ss"),
    DATE_TIME_PATTERN_2("yyyyMMddHHmmss"),
    ;
    private final String pattern;
}
