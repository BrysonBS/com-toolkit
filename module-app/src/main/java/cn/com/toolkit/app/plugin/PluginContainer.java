package cn.com.toolkit.app.plugin;

import cn.com.toolkit.app.domain.bo.PluginInfo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

public class PluginContainer {
    private final URLClassLoader classLoader;
    private final PluginInfo pluginInfo;
    public PluginContainer(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
        try {
            classLoader = new URLClassLoader(
                    new URL[]{ Paths.get(pluginInfo.getJarPath()).toUri().toURL() }
                    , ClassLoader.getSystemClassLoader()
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    public Parent getParentNode() throws Exception{
        String fxmlLoader = StringUtils.isBlank(pluginInfo.getFxmlLoader()) ? "javafx.fxml.FXMLLoader" : pluginInfo.getFxmlLoader();
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        FXMLLoader pluginFxmlLoader = (FXMLLoader) classLoader.loadClass(fxmlLoader).getDeclaredConstructor().newInstance();
        Thread.currentThread().setContextClassLoader(currentClassLoader);
        pluginFxmlLoader.setClassLoader(classLoader);
        URL resource = classLoader.getResource(Strings.CS.removeStart(pluginInfo.getFxml(), "/"));
        return pluginFxmlLoader.load(resource.openStream());
    }
    public void close() throws Exception{
        classLoader.close();
    }
}
