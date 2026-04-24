package cn.com.toolkit.framework.core.support;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DatabaseInitializer {
    public static void executeSqlScript(SqlSessionFactory sqlSessionFactory,InputStreamReader reader){
        if(sqlSessionFactory == null) return;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            ScriptRunner scriptRunner = new ScriptRunner(session.getConnection());
            scriptRunner.setAutoCommit(true);
            scriptRunner.setStopOnError(false);
            scriptRunner.setLogWriter(null);
            scriptRunner.setErrorLogWriter(null);
            scriptRunner.runScript(reader);
        } catch (Exception e) {
            throw new RuntimeException("初始化数据库脚本失败!", e);
        }
    }
    public static void executeSqlScript(SqlSessionFactory sqlSessionFactory, URL resource) {
        if(resource == null) return;
        try {
            InputStreamReader reader = new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8);
            executeSqlScript(sqlSessionFactory, reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
