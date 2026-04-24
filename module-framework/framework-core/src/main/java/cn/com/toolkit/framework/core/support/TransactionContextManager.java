package cn.com.toolkit.framework.core.support;

import cn.com.toolkit.framework.core.annotation.Transaction;
import cn.com.toolkit.framework.core.enums.IsolationEnum;
import cn.com.toolkit.framework.core.enums.PropagationEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;

import java.sql.Savepoint;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;


public class TransactionContextManager {
    private TransactionContextManager() {
        sqlSessionFactory = MybatisPlusManager.getSqlSessionFactory();
    }
    private static class Holder {
        private static final TransactionContextManager INSTANCE = new TransactionContextManager();
    }
    public static TransactionContextManager getInstance() {
        return Holder.INSTANCE;
    }
    @Getter
    private static final ThreadLocal<Deque<TransactionContext>> transactionContextStackLocal = ThreadLocal.withInitial(ArrayDeque::new);
    private final SqlSessionFactory sqlSessionFactory;

    /**
     * 开始事务
     */
    public void beginTransaction(Transaction transaction) throws SQLException {
        Deque<TransactionContext> stack = transactionContextStackLocal.get();
        TransactionContext currentContext = stack.isEmpty() ? null : stack.peek();
        PropagationEnum propagation = transaction.propagation();
        // 当前没有活动事务时的处理
        if (currentContext == null || currentContext.isClosed()) {
            switch (propagation) {
                case MANDATORY:
                    throw new IllegalStateException("No existing transaction found for MANDATORY propagation");

                case NEVER:
                    // NEVER：必须在非事务中执行，当前无事务符合要求，不需要创建会话
                    // 创建一个标记为NEVER的空上下文，表示非事务模式
                    createNonTransactionContext(propagation);
                    break;

                case NOT_SUPPORTED:
                case SUPPORTS:
                    // 不需要事务，创建非事务会话（自动提交模式）
                    createNonTransactionContext(propagation);
                    break;

                case REQUIRED:
                case REQUIRES_NEW:
                case NESTED:
                default:
                    // 需要事务，创建新事务
                    createNewTransactionContext(transaction);
                    break;
            }
            return;
        }

        // 当前有活动事务时的处理
        switch (propagation) {
            case MANDATORY:
            case REQUIRED:
            case SUPPORTS:
                // 这些传播行为直接使用现有事务
                currentContext.increaseCount();
                break;

            case NEVER:
                throw new IllegalStateException("Existing transaction found for NEVER propagation");

            case REQUIRES_NEW:
                // 挂起当前事务，创建新事务
                suspendCurrentTransaction(currentContext);
                createNewTransactionContext(transaction);
                break;

            case NESTED:
                // 创建嵌套事务（保存点）
                createNestedTransactionContext(currentContext, transaction);
                break;

            case NOT_SUPPORTED:
                // 挂起当前事务，以非事务方式执行
                suspendCurrentTransaction(currentContext);
                createNonTransactionContext(propagation);
                break;
        }
    }

    /**
     * 提交事务
     */
    public void commitTransaction() {
        Deque<TransactionContext> stack = transactionContextStackLocal.get();
        if (stack.isEmpty()) return;
        TransactionContext currentContext = stack.peek();
        PropagationEnum propagation = currentContext.getPropagation();
        if (currentContext.isRollbackOnly()) {
            // 标记为回滚的，执行回滚而不是提交
            rollbackTransaction();
            throw new RuntimeException("Transaction marked as rollback-only, rolled back instead of committed");
        }
        try {
            if (propagation == PropagationEnum.NESTED) {
                // 嵌套事务：释放保存点，但不提交整个事务
                if (currentContext.getSavepoint() != null){
                    currentContext.getSqlSession().getConnection().releaseSavepoint(currentContext.getSavepoint());
                    currentContext.setSavepoint(null);
                }

                cleanupContext(currentContext);
            } else if (isNonTransactional(propagation)) {
                // 非事务模式，直接清理
                cleanupContext(currentContext);
            } else {
                // 正常事务提交
                if (!currentContext.isReadOnly())
                    currentContext.getSqlSession().commit();
                cleanupContext(currentContext);
            }
        } catch (Exception e) {
            throw new RuntimeException("Transaction commit failed", e);
        } finally {
            // 恢复被挂起的事务
            resumeSuspendedTransaction();
        }
    }

    /**
     * 回滚事务
     */
    public void rollbackTransaction() {
        Deque<TransactionContext> stack = transactionContextStackLocal.get();
        if (stack.isEmpty()) return;
        TransactionContext currentContext = stack.peek();
        PropagationEnum propagation = currentContext.getPropagation();

        try {
            if (propagation == PropagationEnum.NESTED) {
                // 嵌套事务：回滚到保存点
                if (currentContext.getSavepoint() != null) {
                    currentContext.getSqlSession().getConnection().rollback(currentContext.getSavepoint());
                    currentContext.setSavepoint(null);
                }
                cleanupContext(currentContext);
            } else if (isNonTransactional(propagation)) {
                // 非事务模式，无需回滚，直接清理
                cleanupContext(currentContext);
            } else {
                // 正常事务回滚
                currentContext.getSqlSession().rollback();
                cleanupContext(currentContext);
            }

            // 标记父事务为需要回滚（如果是嵌套事务）
            if (currentContext.getParentContext() != null)
                currentContext.getParentContext().setRollbackOnly(true);
        } catch (Exception e) {
            throw new RuntimeException("Transaction rollback failed", e);
        } finally {
            // 恢复被挂起的事务
            resumeSuspendedTransaction();
        }
    }

    /**
     * 判断是否为非事务模式
     */
    private boolean isNonTransactional(PropagationEnum propagation) {
        return propagation == PropagationEnum.NEVER ||
                propagation == PropagationEnum.NOT_SUPPORTED ||
                propagation == PropagationEnum.SUPPORTS;
    }

    /**
     * 创建新事务上下文
     */
    private void createNewTransactionContext(Transaction transaction) throws SQLException {
        PropagationEnum propagation = transaction.propagation();
        TransactionIsolationLevel isolationLevel = getIsolationLevel(transaction.isolation());
        TransactionContext context = new TransactionContext();

        SqlSession sqlSession = isolationLevel != null
                ? sqlSessionFactory.openSession(isolationLevel)
                : sqlSessionFactory.openSession(false);

        sqlSession.getConnection().setReadOnly(transaction.readOnly());
        if (transaction.timeout() > 0)
            sqlSession.getConnection().setNetworkTimeout(null, transaction.timeout() * 1000);

        context.setCount(1);
        context.setSqlSession(sqlSession);
        context.setPropagation(propagation);
        context.setIsolation(transaction.isolation());
        context.setReadOnly(transaction.readOnly());
        context.setSuspended(false);
        context.setClosed(false);
        context.setRollbackOnly(false);

        transactionContextStackLocal.get().push(context);
    }

    /**
     * 创建嵌套事务上下文
     */
    private void createNestedTransactionContext(TransactionContext parentContext, Transaction transaction) throws SQLException {
        Savepoint savepoint = parentContext.getSqlSession().getConnection().setSavepoint();
        TransactionContext nestedContext = new TransactionContext();
        nestedContext.setSqlSession(parentContext.getSqlSession());
        nestedContext.setPropagation(PropagationEnum.NESTED);
        nestedContext.setIsolation(transaction.isolation());
        nestedContext.setReadOnly(transaction.readOnly());
        nestedContext.setParentContext(parentContext);
        nestedContext.setSavepoint(savepoint);
        nestedContext.setCount(1);
        nestedContext.setClosed(false);
        nestedContext.setRollbackOnly(false);

        transactionContextStackLocal.get().push(nestedContext);
    }

    /**
     * 创建非事务上下文
     */
    private void createNonTransactionContext(PropagationEnum propagation) {
        TransactionContext context = new TransactionContext();
        // 自动提交模式
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        context.setSqlSession(sqlSession);
        context.setPropagation(propagation);
        context.setCount(1);
        context.setClosed(false);
        context.setRollbackOnly(false);
        transactionContextStackLocal.get().push(context);
    }

    /**
     * 挂起当前事务
     */
    private void suspendCurrentTransaction(TransactionContext context) {
        context.setSuspended(true);
    }

    /**
     * 恢复被挂起的事务
     */
    private void resumeSuspendedTransaction() {
        Deque<TransactionContext> stack = transactionContextStackLocal.get();
        if (stack.isEmpty()) return;
        // 找到第一个被挂起的事务并恢复
        TransactionContext context = stack.peek();
        if (context.isSuspended()) context.setSuspended(false);
    }

    /**
     * 清理事务上下文
     */
    private void cleanupContext(TransactionContext context) {
        Deque<TransactionContext> stack = transactionContextStackLocal.get();
        if (stack.isEmpty()) return;

        context.decreaseCount();
        int count = context.getCount();
        // 只有当引用计数为0且未关闭时才真正关闭
        if (count <= 0 && !context.isClosed()) {
            try {
                if (context.getSqlSession() != null && context.getPropagation() != PropagationEnum.NESTED) {
                    context.getSqlSession().close();
                }
            }finally {
                context.setClosed(true);
                stack.pop();
            }
        }
    }

    /**
     * 标记当前事务为只回滚
     */
    public void setRollbackOnly() {
        Deque<TransactionContext> stack = transactionContextStackLocal.get();
        if (!stack.isEmpty()) {
            TransactionContext context = stack.peek();
            context.setRollbackOnly(true);
        }
    }
    /**
     * 检查当前事务是否标记为回滚
     */
    public boolean isRollbackOnly() {
        Deque<TransactionContext> stack = transactionContextStackLocal.get();
        return !stack.isEmpty() && stack.peek().isRollbackOnly();
    }

    /**
     * 清理所有事务上下文（线程结束时调用）
     */
    public void clear() {
        Deque<TransactionContext> stack = transactionContextStackLocal.get();
        if (stack == null || stack.isEmpty()) {
            transactionContextStackLocal.remove();
            return;
        }
        while (!stack.isEmpty()) {
            TransactionContext context = stack.pop();
            if (context.getSqlSession() != null && !context.isClosed())
                context.getSqlSession().close();
        }
        transactionContextStackLocal.remove();
    }

    /**
     * 获取隔离级别（修正映射）
     */
    private TransactionIsolationLevel getIsolationLevel(IsolationEnum isolation) {
        return switch (isolation) {
            case READ_UNCOMMITTED -> TransactionIsolationLevel.READ_UNCOMMITTED;
            case READ_COMMITTED -> TransactionIsolationLevel.READ_COMMITTED;
            case REPEATABLE_READ -> TransactionIsolationLevel.REPEATABLE_READ;
            case SERIALIZABLE -> TransactionIsolationLevel.SERIALIZABLE;
            default -> null;
        };
    }

    @Getter
    @Setter
    public static class TransactionContext {
        private TransactionContext parentContext;
        private int count = 0;
        private SqlSession sqlSession;
        private PropagationEnum propagation;
        private IsolationEnum isolation;
        private boolean suspended;
        private Savepoint savepoint;
        private boolean readOnly;
        private boolean closed;
        private boolean rollbackOnly;
        public void increaseCount() {
            count++;
        }
        public void decreaseCount() {
            if (count > 0) count--;
        }
    }
}