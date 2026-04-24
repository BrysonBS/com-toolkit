package cn.com.toolkit.framework.core.support;

import cn.com.toolkit.framework.core.datasource.DynamicRoutingDataSource;
import cn.com.toolkit.framework.core.proxy.MapperProxyInterceptor;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import lombok.Getter;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.*;
import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;

public class MybatisPlusManager {
    private static final ConcurrentHashMap<Class<?>,Object> serviceCache = new ConcurrentHashMap<>();
    @Getter
    private static final DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
    private static String DEFAULT_CONFIG_LOCATION = "mybatis-config.xml";
    private static String DEFAULT_MAPPER_LOCATIONS = "classpath*:/mapper/**/*.xml";

    private static class Holder {
        private static final SqlSessionFactory SQL_SESSION_FACTORY;
        private static final SqlSessionManager SQL_SESSION_MANAGER;
        private static final ClassLoader DEFAULT_CLASS_LOADER;
        static {
            try {
                DEFAULT_CLASS_LOADER = Thread.currentThread().getContextClassLoader();
                SQL_SESSION_FACTORY = new MybatisSqlSessionFactoryBuilder().build(mybatisConfiguration(DEFAULT_CONFIG_LOCATION,DEFAULT_MAPPER_LOCATIONS));
                SQL_SESSION_MANAGER = SqlSessionManager.newInstance(SQL_SESSION_FACTORY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void initialize(String mybatisConfigLocation, String mapperLocations){
        DEFAULT_CONFIG_LOCATION = mybatisConfigLocation;
        DEFAULT_MAPPER_LOCATIONS = mapperLocations;
        clearAllServiceImplCache();
    }
    public static SqlSessionFactory getSqlSessionFactory(){
        return Holder.SQL_SESSION_FACTORY;
    }
    public static SqlSessionManager getSqlSessionManager() {
        return Holder.SQL_SESSION_MANAGER;
    }
    @SuppressWarnings("unchecked")
    public static <T> T getMapper(Class<T> mapperClass) {
        return (T)Proxy.newProxyInstance(mapperClass.getClassLoader(), new Class[]{mapperClass}, new MapperProxyInterceptor(mapperClass));
    }
    public static <T> T getSingletonServiceImpl(Class<T> clazz){
        return (T)serviceCache.computeIfAbsent(clazz, key -> getNewServiceImpl(key));
    }
    public static <T> T getNewServiceImpl(Class<T> clazz){
        try {
            //实例化
            Object service = clazz.getDeclaredConstructor().newInstance();
            //注入mapper
            if(CrudRepository.class.isAssignableFrom(clazz)){
                Class<?> currentClass = clazz;
                while (currentClass != null && currentClass != CrudRepository.class){
                    if(currentClass == Object.class) break;
                    currentClass = currentClass.getSuperclass();
                }
                if(currentClass == CrudRepository.class){
                    for (Field field : currentClass.getDeclaredFields()) {
                        if (!"baseMapper".equals(field.getName())) continue;
                        Type genericSuperclass = clazz.getGenericSuperclass();
                        if (genericSuperclass instanceof ParameterizedType parameterizedType){
                            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                            if(actualTypeArguments == null || actualTypeArguments.length < 1) continue;
                            Class<?> mapperType = (Class<?>) actualTypeArguments[0];
                            Object mapper = getMapper(mapperType);
                            field.setAccessible(true);
                            field.set(service, mapper);
                        }
                    }
                }
            }
            return (T) service;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void clearServiceImplCache(Class<?> clazz){
        serviceCache.remove(clazz);
    }
    public static void clearAllServiceImplCache(){
        serviceCache.clear();
    }
    private static MybatisConfiguration mybatisConfiguration(String mybatisConfigPath,String mapperLocations) throws IOException {
        Reader reader = Resources.getResourceAsReader(Holder.DEFAULT_CLASS_LOADER, mybatisConfigPath);
        XMLConfigBuilder parser = new XMLConfigBuilder(reader);
        Configuration nativeConfig = parser.parse();
        MybatisConfiguration configuration = new MybatisConfiguration();
        Environment sourceEnvironment = nativeConfig.getEnvironment();
        dynamicRoutingDataSource.addDefaultDataSource(sourceEnvironment.getDataSource());
        Environment targetEnvironment = new Environment(sourceEnvironment.getId(),sourceEnvironment.getTransactionFactory(),dynamicRoutingDataSource);
        configuration.setEnvironment(targetEnvironment);
        configuration.setMapUnderscoreToCamelCase(nativeConfig.isMapUnderscoreToCamelCase());
        configuration.setCacheEnabled(nativeConfig.isCacheEnabled());
        configuration.setDefaultEnumTypeHandler(EnumOrdinalTypeHandler.class);
        configuration.setLazyLoadingEnabled(nativeConfig.isLazyLoadingEnabled());
        configuration.setAggressiveLazyLoading(nativeConfig.isAggressiveLazyLoading());
        configuration.setDefaultStatementTimeout(nativeConfig.getDefaultStatementTimeout());
        configuration.setDefaultFetchSize(nativeConfig.getDefaultFetchSize());
        configuration.setLogImpl(nativeConfig.getLogImpl());

        //解析mapper.xml文件
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(mapperLocations);
        for (Resource resource : resources) {
            // 使用 XMLMapperBuilder 解析每个 XML 文件
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                    resource.getInputStream(),
                    configuration,
                    resource.getURL().toString(),
                    configuration.getSqlFragments()
            );
            mapperBuilder.parse(); // 执行解析，将 SQL 注册到 configuration 中
        }
        nativeConfig.getInterceptors().forEach(configuration::addInterceptor);
        //分页插件
        configuration.addInterceptor(paginationInnerInterceptor());

        return configuration;
    }
    private static MybatisPlusInterceptor paginationInnerInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        // 设置最大单页限制数量，默认 1000 条，-1 不受限制
        paginationInterceptor.setMaxLimit(1000L);
        // 溢出总页数后是否进行处理
        paginationInterceptor.setOverflow(true);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setOptimizeJoin(true);
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }

}
