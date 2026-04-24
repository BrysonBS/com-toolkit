package cn.com.toolkit.framework.core.enums;

public enum PropagationEnum {
    REQUIRED,      // 支持当前事务，如果不存在则创建新事务
    REQUIRES_NEW,  // 创建新事务，挂起当前事务
    SUPPORTS,      // 支持当前事务，如果不存在则以非事务方式执行
    NOT_SUPPORTED, // 以非事务方式执行，挂起当前事务
    MANDATORY,     // 支持当前事务，如果不存在则抛出异常
    NEVER,         // 以非事务方式执行，如果存在事务则抛出异常
    NESTED         // 如果当前存在事务，则在嵌套事务内执行
}
