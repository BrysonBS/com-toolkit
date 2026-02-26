package cn.com.toolkit.tools.cpbio.support;

import cn.com.toolkit.framework.core.support.SpringFxmlLoader;

import java.io.IOException;
import java.net.URISyntaxException;


public class CpbioFxmlLoader extends SpringFxmlLoader {
    static {
        try {
            ConfigSupport.initDatasource(null);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
