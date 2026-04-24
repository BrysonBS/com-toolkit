package cn.com.toolkit.framework.core.datasource;

import cn.com.toolkit.framework.core.support.DynamicDataSourceContextHolder;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DynamicRoutingDataSource implements DataSource {
    private final String DEFAULT_DS = "default_ds";
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    public void addDefaultDataSource(DataSource dataSource) {
        this.dataSourceMap.put(DEFAULT_DS, dataSource);
    }
    public void addDataSource(String key, DataSource dataSource) {
        this.dataSourceMap.put(key, dataSource);
    }
    public void removeDataSource(String key) {
        this.dataSourceMap.remove(key);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getLocalDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getLocalDataSource().getConnection(username,password);
    }
    public DataSource getDefaultDataSource() {
        return dataSourceMap.get(DEFAULT_DS);
    }
    public DataSource getDataSource(String key) {
        return this.dataSourceMap.get(key);
    }
    private DataSource getLocalDataSource(){
        String lookupKey = DynamicDataSourceContextHolder.peek();
        DataSource dataSource = null;
        if (lookupKey != null) dataSource = dataSourceMap.get(lookupKey);
        if(dataSource == null) dataSource = dataSourceMap.get(DEFAULT_DS);
        if(dataSource == null) throw new RuntimeException("没有找到数据源 [" + lookupKey + "]");
        return dataSource;
    }
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getLocalDataSource().getLogWriter();
    }
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getLocalDataSource().setLogWriter(out);
    }
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getLocalDataSource().setLoginTimeout(seconds);
    }
    @Override
    public int getLoginTimeout() throws SQLException {
        return getLocalDataSource().getLoginTimeout();
    }
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getLocalDataSource().getParentLogger();
    }
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getLocalDataSource().unwrap(iface);
    }
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getLocalDataSource().isWrapperFor(iface);
    }
}
