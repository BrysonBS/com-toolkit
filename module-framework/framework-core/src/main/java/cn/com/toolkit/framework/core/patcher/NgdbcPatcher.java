package cn.com.toolkit.framework.core.patcher;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NgdbcPatcher {
    public static Logger log = LoggerFactory.getLogger(NgdbcPatcher.class);
    static {
        log.info("========ByteBuddyAgent.install(): begin==========");
        ByteBuddyAgent.install();
        log.info("========ByteBuddyAgent.install(): end==========");
    }
    public static void ignoreVheckVersion() {
        try{
            Class<?> clazz = Class.forName("com.sap.db.jdbc.Driver",false,Thread.currentThread().getContextClassLoader());
            new ByteBuddy()
                    .redefine(clazz) // 替换为实际的类名
                    .method(net.bytebuddy.matcher.ElementMatchers.nameContains("checkJavaVersion"))
                    .intercept(FixedValue.value(Void.TYPE))
                    .make()
                    .load(
                            clazz.getClassLoader(),
                            ClassReloadingStrategy.fromInstalledAgent()
                    );
        }catch (ClassNotFoundException e){
            log.error(e.getMessage(),e);
        }
    }
}
