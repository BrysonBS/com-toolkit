package cn.com.toolkit.framework.core.support;

import lombok.NoArgsConstructor;

import java.util.ArrayDeque;
import java.util.Deque;

@NoArgsConstructor
public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<Deque<String>> DATASOURCE_KEY_HOLDER = ThreadLocal.withInitial(ArrayDeque::new);
    public static String peek() {
        return DATASOURCE_KEY_HOLDER.get().peek();
    }
    public static String push(String ds) {
        String dataSourceStr = ds == null ? "" : ds;
        DATASOURCE_KEY_HOLDER.get().push(dataSourceStr);
        return dataSourceStr;
    }
    public static void poll() {
        Deque<String> deque = DATASOURCE_KEY_HOLDER.get();
        deque.poll();
        if(deque.isEmpty()) DATASOURCE_KEY_HOLDER.remove();
    }
    public static void clear() {
        DATASOURCE_KEY_HOLDER.remove();
    }
}
