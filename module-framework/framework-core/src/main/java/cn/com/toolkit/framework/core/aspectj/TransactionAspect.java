package cn.com.toolkit.framework.core.aspectj;

import cn.com.toolkit.framework.core.annotation.Transaction;
import cn.com.toolkit.framework.core.support.TransactionContextManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Deque;


@Aspect
public class TransactionAspect {
    private final TransactionContextManager transactionManager = TransactionContextManager.getInstance();
    @Around("@annotation(transaction)")
    public Object pointcut(ProceedingJoinPoint joinPoint, Transaction transaction) throws Throwable {
        if(transaction == null) return joinPoint.proceed();
        Class<? extends Throwable>[] rollbackFor = transaction.rollbackFor();
        Class<? extends Throwable>[] noRollbackFor = transaction.noRollbackFor();
        try {
            // 开始事务
            transactionManager.beginTransaction(transaction);
            // 执行目标方法
            Object result = joinPoint.proceed();
            // 提交事务
            transactionManager.commitTransaction();
            return result;

        } catch (Throwable throwable) {
            if (shouldRollback(throwable, rollbackFor, noRollbackFor))
                transactionManager.rollbackTransaction();
            else transactionManager.commitTransaction();
            throw throwable;
        } finally {
            Deque<TransactionContextManager.TransactionContext> stack = TransactionContextManager.getTransactionContextStackLocal().get();
            if (stack == null || stack.isEmpty()) TransactionContextManager.getTransactionContextStackLocal().remove();
            else if (stack.size() == 1 && stack.peek().getCount() <= 0)
                transactionManager.clear();
        }
    }
    private boolean shouldRollback(Throwable throwable,
                                   Class<? extends Throwable>[] rollbackFor,
                                   Class<? extends Throwable>[] noRollbackFor) {
        if (isExceptionInList(throwable, noRollbackFor)) return false;
        if (rollbackFor.length > 0) return isExceptionInList(throwable, rollbackFor);
        else return true;
    }
    private boolean isExceptionInList(Throwable throwable, Class<? extends Throwable>[] exceptionTypes) {
        if (exceptionTypes == null || exceptionTypes.length == 0) return false;
        for (Class<? extends Throwable> exceptionType : exceptionTypes) {
            if (exceptionType.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            return isExceptionInList(cause, exceptionTypes);
        }
        return false;
    }
}
