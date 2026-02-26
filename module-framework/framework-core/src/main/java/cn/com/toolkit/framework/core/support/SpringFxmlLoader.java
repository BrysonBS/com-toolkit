package cn.com.toolkit.framework.core.support;

import javafx.fxml.FXMLLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;


public class SpringFxmlLoader extends FXMLLoader {
    private final ApplicationContext context;
    public SpringFxmlLoader() {
        context = SpringContext.getApplicationContext();;
        this.setControllerFactory(this::createController);
    }
    /**
     * Controller工厂方法
     */
    private <T> T createController(Class<T> controllerClass) {
        try {
            return context.getBean(controllerClass);
        } catch (BeansException e) {
            AutowireCapableBeanFactory factory = context.getAutowireCapableBeanFactory();
            T instance = factory.createBean(controllerClass);
            factory.autowireBean(instance);//手动装配依赖
            String simpleName = controllerClass.getSimpleName();
            String beanName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
            factory.initializeBean(instance,beanName);
            return instance;
        }
    }
}
