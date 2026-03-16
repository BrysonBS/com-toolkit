package cn.com.toolkit.tools.cpbio.support;

import cn.com.toolkit.framework.core.config.DataSourceConfig;
import cn.com.toolkit.tools.cpbio.control.DatasourceDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

public class ConfigSupport {
    private static List<Runnable> exitTaskList = new ArrayList<>();
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("关闭钩子执行 - JVM退出前");
            exitTaskList.forEach(Runnable::run);
        }));
    }

    public static void initDatasource(String path) throws IOException, URISyntaxException {
        if(StringUtils.isBlank(path)) path = "/application.yml";
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        InputStream inputStream = ConfigSupport.class.getResourceAsStream(path);
        JsonNode rootNode = objectMapper.readTree(inputStream);
        ObjectNode dataSourceNode = (ObjectNode) rootNode.at("/spring/datasource");
        LinkedHashMap<String,String> dialogConfigMap = new LinkedHashMap<>();
        Stream.of("url", "username", "password", "driver-class-name")
                .forEach(key -> dialogConfigMap.put(key, dataSourceNode.get(key).asText("")));
        DatasourceDialog dialog = new DatasourceDialog(dialogConfigMap);
        dialog.setTitle("SAP库配置");
        dialog.showAndWait().ifPresent(map -> {
            if(!dataSourceNode.isEmpty()) {
                DataSourceConfig.setUrl(map.get("url"));
                DataSourceConfig.setUsername(map.get("username"));
                DataSourceConfig.setPassword(map.get("password"));
                DataSourceConfig.setDriverClassName(map.get("driver-class-name"));
                map.forEach(dataSourceNode::put);
            }
        });

        URL url = ConfigSupport.class.getResource(path);
        if(url == null) return;
        if("file".equals(url.getProtocol()))
            objectMapper.writeValue(new File(url.toURI()), rootNode);
        else if("jar".equals(url.getProtocol())){
            final String finalPath = path.replaceFirst("/","");
            final byte[] finalBytes = objectMapper.writeValueAsBytes(rootNode);
            Runnable runnable = () -> {
                try {
                    writeJar(finalPath,finalBytes);
                    //writeJarFile(finalPath,finalBytes);
                } catch (IOException | URISyntaxException e) {
                    System.out.println(e.getMessage());
                    throw new RuntimeException(e);
                }
            };
            exitTaskList.add(runnable);
        }

    }

    public static void writeJar(String filename, byte[] bytes) throws IOException, URISyntaxException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JarOutputStream jarOutputStream = new JarOutputStream(byteArrayOutputStream);
        String path = ConfigSupport.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        JarFile jarFile = new JarFile(path);

        try(jarOutputStream;jarFile){
            Enumeration<JarEntry> enumeration = jarFile.entries();
            while(enumeration.hasMoreElements()){
                JarEntry entry = enumeration.nextElement();
                jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
                InputStream inputStream = filename.equals(entry.getName())
                        ? new ByteArrayInputStream(bytes)
                        : jarFile.getInputStream(entry);
                inputStream.transferTo(jarOutputStream);
            }
            jarOutputStream.finish();
            byteArrayOutputStream.writeTo(new FileOutputStream(path));
        }
    }
    public static void writeJarFile(String innerFilePath,byte[] bytes) throws URISyntaxException, IOException {
        Path originalJar = Paths.get(ConfigSupport.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path tempJar = Files.createTempFile("temp-", ".jar");
        Files.copy(originalJar, tempJar, StandardCopyOption.REPLACE_EXISTING);
        try (FileSystem fs = FileSystems.newFileSystem(tempJar, Map.of("create", "false"), null)) {
            Path targetFile = fs.getPath(innerFilePath);
            Files.write(targetFile, bytes);
        }
        Files.move(tempJar, originalJar, StandardCopyOption.REPLACE_EXISTING);
    }
}
