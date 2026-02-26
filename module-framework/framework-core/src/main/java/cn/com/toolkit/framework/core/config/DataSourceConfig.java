package cn.com.toolkit.framework.core.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

public class DataSourceConfig {
    private static String url;
    private static String username;
    private static String password;
    private static String driverClassName;

    public static String getUrl(Properties properties){
        if(StringUtils.isBlank(url)) url = properties.getProperty("spring.datasource.url","");
        return url;
    }
    public static String getUsername(Properties properties){
        if(StringUtils.isBlank(username)) username = properties.getProperty("spring.datasource.username","");
        return username;
    }
    public static String getPassword(Properties properties){
        if(StringUtils.isBlank(password)) password = properties.getProperty("spring.datasource.password","");
        return password;
    }
    public static String getDriverClassName(Properties properties){
        if(StringUtils.isBlank(driverClassName)) driverClassName = properties.getProperty("spring.datasource.driver-class-name","");
        return driverClassName;
    }

    public static void setUrl(String url) {
        DataSourceConfig.url = url;
    }
    public static void setUsername(String username) {
        DataSourceConfig.username = username;
    }
    public static void setPassword(String password) {
        DataSourceConfig.password = password;
    }
    public static void setDriverClassName(String driverClassName) {
        DataSourceConfig.driverClassName = driverClassName;
    }
}
