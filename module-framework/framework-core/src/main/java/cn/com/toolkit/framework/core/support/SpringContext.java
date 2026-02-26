package cn.com.toolkit.framework.core.support;

import cn.com.toolkit.framework.core.config.FrameworkCoreConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringContext {
	private SpringContext() {}
	public static ApplicationContext getApplicationContext() {
		return Holder.applicationContext;
	}
	private static class Holder{
		private static final ApplicationContext applicationContext = new AnnotationConfigApplicationContext(FrameworkCoreConfig.class);
	}
	public static <T> T getBean(Class<T> beanClass) {
		return getApplicationContext().getBean(beanClass);
	}

	public static <T> T getBean(String name, Class<T> beanClass) {
		return getApplicationContext().getBean(name, beanClass);
	}

	public static void shutdown() {
		if (getApplicationContext() instanceof AnnotationConfigApplicationContext applicationContext)
			applicationContext.close();
	}
}
