package cn.com.toolkit.framework.core.enums;

public enum IsolationEnum {
    DEFAULT, // 使用数据库默认隔离级别
    READ_UNCOMMITTED,
    READ_COMMITTED,
    REPEATABLE_READ,
    SERIALIZABLE
}
