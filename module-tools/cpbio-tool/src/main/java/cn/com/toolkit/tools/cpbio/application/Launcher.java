package cn.com.toolkit.tools.cpbio.application;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);
    public static void main(String[] args) {
        try {
            Application.launch(CpbioToolApplication.class);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }

    }
}
