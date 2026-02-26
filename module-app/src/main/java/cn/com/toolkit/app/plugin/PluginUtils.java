package cn.com.toolkit.app.plugin;

import cn.com.toolkit.app.domain.bo.PluginInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.List;

public class PluginUtils {
    public static List<PluginInfo> loadPlugins(){
        return loadPlugins("/plugins.yml");
    }
    public static List<PluginInfo> loadPlugins(String path) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.readValue(PluginUtils.class.getResourceAsStream(path),new TypeReference<List<PluginInfo>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
