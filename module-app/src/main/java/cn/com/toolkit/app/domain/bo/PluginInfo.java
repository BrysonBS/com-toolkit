package cn.com.toolkit.app.domain.bo;

import javafx.scene.Parent;
import lombok.Data;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@Data
public class PluginInfo {
    private String name;
    private String fxml;
    private String jar;
    private String fxmlLoader;
    private Parent root;


    public String getJarPath(){
        String parent = null;
        try {
            File jarFile = new File(this.getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation().toURI());
            if(!jarFile.isFile())
                parent = System.getProperty("user.dir") + "/module-app/build/libs/";
            else {
                parent = jarFile.getParent() + File.separator;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (parent + Strings.CS.removeStart(getJar(),"/"))
                .replace("/",File.separator)
                .replace("\\",File.separator);
    }
}
