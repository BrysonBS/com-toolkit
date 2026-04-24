package cn.com.toolkit.framework.core.aspectj;

import cn.com.toolkit.framework.core.annotation.DSN;
import cn.com.toolkit.framework.core.support.DynamicDataSourceContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class DSNAspect {
    @Around("@annotation(cn.com.toolkit.framework.core.annotation.DSN) " +
            "|| @within(cn.com.toolkit.framework.core.annotation.DSN)")
    public Object pointcut(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DSN dsn = method.getAnnotation(DSN.class);
        if (dsn == null) dsn = joinPoint.getTarget().getClass().getAnnotation(DSN.class);
        if (dsn == null || StringUtils.isEmpty(dsn.value())
                || dsn.value().equals(DynamicDataSourceContextHolder.peek())){
            return joinPoint.proceed();
        }
        DynamicDataSourceContextHolder.push(dsn.value());
        try {
            return joinPoint.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
