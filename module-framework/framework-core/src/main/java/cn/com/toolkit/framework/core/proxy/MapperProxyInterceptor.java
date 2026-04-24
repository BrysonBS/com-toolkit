package cn.com.toolkit.framework.core.proxy;

import cn.com.toolkit.framework.core.support.MybatisPlusManager;
import cn.com.toolkit.framework.core.support.TransactionContextManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Deque;

public class MapperProxyInterceptor implements InvocationHandler {
    private final Class<?> mapperClass;
    public MapperProxyInterceptor( Class<?> mapperClass) {
        this.mapperClass = mapperClass;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) return method.invoke(this, args);
        Deque<TransactionContextManager.TransactionContext> stack = TransactionContextManager.getTransactionContextStackLocal().get();
        Object mapper = (stack.isEmpty() || stack.peek().getSqlSession() == null)
                ? MybatisPlusManager.getSqlSessionManager().getMapper(mapperClass)
                : stack.peek().getSqlSession().getMapper(mapperClass);
        return method.invoke(mapper,args);
    }
}
