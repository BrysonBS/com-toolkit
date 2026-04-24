package cn.com.toolkit.tools.sql2pojo.support;

import cn.com.toolkit.framework.core.patcher.NgdbcPatcher;
import cn.com.toolkit.framework.core.support.DatabaseInitializer;
import cn.com.toolkit.framework.core.support.MybatisPlusManager;
import cn.com.toolkit.framework.core.util.ToolKitUtil;
import javafx.fxml.FXMLLoader;

import java.net.URL;
import java.security.Security;

public class Sql2PojoFxmlLoader extends FXMLLoader {
    static {
        NgdbcPatcher.ignoreVheckVersion();
        ToolKitUtil.createDirectories("./data/sql2pojo");
        URL resource = Thread.currentThread().getContextClassLoader().getResource("init/db.sql");
        DatabaseInitializer.executeSqlScript(MybatisPlusManager.getSqlSessionFactory(),resource);
        System.setProperty("jdk.tls.client.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        Security.setProperty("jdk.tls.disabledAlgorithms",
                "SSLv3, RC4, DES, MD5withRSA, DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL");
    }
}
