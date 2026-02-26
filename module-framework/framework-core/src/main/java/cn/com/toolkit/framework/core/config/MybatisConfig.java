package cn.com.toolkit.framework.core.config;

import cn.com.toolkit.framework.core.patcher.NgdbcPatcher;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Configuration
public class MybatisConfig {
    /**
     * 加载 YAML 配置文件
     */
    @Bean("yamlProperties")
    public Properties properties(){
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        return yaml.getObject();
    }
    @Bean
    public DataSource dataSource() {

        NgdbcPatcher.ignoreVheckVersion();

        Properties properties = properties();
        HikariConfig config = new HikariConfig();

        // 基本数据库连接配置
        config.setDriverClassName(DataSourceConfig.getDriverClassName(properties));
        config.setJdbcUrl(DataSourceConfig.getUrl(properties));
        config.setUsername(DataSourceConfig.getUsername(properties));
        config.setPassword(DataSourceConfig.getPassword(properties));
        System.out.println(config.getPassword());

        // 连接池核心配置
        config.setInitializationFailTimeout(Integer.parseInt(properties.getProperty("spring.datasource.hikari.initialization-fail-timeout", "-1")));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("spring.datasource.hikari.maximum-pool-size", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("spring.datasource.hikari.minimum-idle", "5")));
        config.setConnectionTimeout(Long.parseLong(properties.getProperty("spring.datasource.hikari.connection-timeout", "30000")));
        config.setIdleTimeout(Long.parseLong(properties.getProperty("spring.datasource.hikari.idle-timeout", "600000")));
        config.setMaxLifetime(Long.parseLong(properties.getProperty("spring.datasource.hikari.max-lifetime", "1800000")));
        // 连接测试配置
        config.setConnectionTestQuery(properties.getProperty("spring.datasource.hikari.connection-test-query", "/* NOOP */"));
        config.setValidationTimeout(Long.parseLong(properties.getProperty("spring.datasource.hikari.validation-timeout", "5000")));



        return new HikariDataSource(config);
    }
    @Bean
    public MybatisPlusInterceptor paginationInnerInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型
        paginationInterceptor.setDbType(DbType.SAP_HANA);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInterceptor.setMaxLimit(1000L);
        // 溢出总页数后是否进行处理
        paginationInterceptor.setOverflow(true);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setOptimizeJoin(true);
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {

        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // 设置拦截器
        sessionFactory.setPlugins(paginationInnerInterceptor());

        //XML映射文件配置
        Properties properties = properties();
        String mapperLocations = properties.getProperty("mybatis-plus.mapper-locations", "classpath*:mapper/**/*.xml");
        final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<>();
        for (String mapperLocation : mapperLocations.split(","))
            resources.addAll(Arrays.asList(resolver.getResources(mapperLocation)));
        sessionFactory.setMapperLocations(resources.toArray(new Resource[0]));

        // 配置其他 MyBatis 设置
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(true);
        configuration.setAggressiveLazyLoading(false);
        configuration.setLogPrefix("mybatis.");
        configuration.setMapUnderscoreToCamelCase(Boolean.parseBoolean(properties.getProperty("mybatis-plus.configuration.map-underscore-to-camel-case", "true")));
        String logImpl;
        if(StringUtils.isNotEmpty(logImpl = properties.getProperty("mybatis-plus.configuration.log-impl", ""))){
            Class<?> logImplClass = Class.forName(logImpl);
            if(Log.class.isAssignableFrom(logImplClass)){
                configuration.setLogImpl((Class<? extends Log>) logImplClass);
            }
        }

        sessionFactory.setConfiguration(configuration);
        sessionFactory.setGlobalConfig(globalConfig());
        return sessionFactory.getObject();
    }

    private GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        Properties properties = properties();
        // 主键策略
        dbConfig.setIdType(IdType.valueOf(properties.getProperty("mybatis-plus.global-config.db-config.id-type", "auto").toUpperCase()));
        // 逻辑删除
        dbConfig.setLogicDeleteField(properties.getProperty("mybatis-plus.global-config.db-config.logic-delete-field", "deleted"));
        dbConfig.setLogicDeleteValue(properties.getProperty("mybatis-plus.global-config.db-config.logic-delete-value", "1"));
        dbConfig.setLogicNotDeleteValue(properties.getProperty("mybatis-plus.global-config.db-config.logic-not-delete-value", "0"));
        //dbConfig.setTableUnderline(true);
        globalConfig.setDbConfig(dbConfig);
        return globalConfig;
    }

}
