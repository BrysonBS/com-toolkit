package cn.com.toolkit.framework.core.support;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorSupport {
    private static class Holder{
        public static final ExecutorService executor;
        static {
            executor = new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().availableProcessors() * 2,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(10000),
                    new ThreadFactoryBuilder().setNameFormat("image-convert-%d").build(),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                ExecutorService ex = Holder.executor;
                ex.shutdown();
                try {
                    if (!ex.awaitTermination(10, TimeUnit.SECONDS))
                        ex.shutdownNow();
                } catch (InterruptedException ignored) {}
            }));
        }
    }
    public static void execute(Runnable command) {
        Holder.executor.execute(command);
    }
    public static <T> Future<T> submit(Callable<T> task) {
        return Holder.executor.submit(task);
    }

    public static <T> Future<T> submit(Runnable task, T result) {
        return Holder.executor.submit(task, result);
    }
    public static Future<?> submit(Runnable task) {
        return Holder.executor.submit(task);
    }
    public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return Holder.executor.invokeAll(tasks);
    }
    public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return Holder.executor.invokeAll(tasks, timeout, unit);
    }
    public static <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return Holder.executor.invokeAny(tasks);
    }
    public static <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return Holder.executor.invokeAny(tasks, timeout, unit);
    }
    public static int getActiveCount() {
        return ((ThreadPoolExecutor) Holder.executor).getActiveCount();
    }
    public static int getQueueSize() {
        return ((ThreadPoolExecutor) Holder.executor).getQueue().size();
    }
}
