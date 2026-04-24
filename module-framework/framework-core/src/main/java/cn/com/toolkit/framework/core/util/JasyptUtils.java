package cn.com.toolkit.framework.core.util;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jasypt 加密工具类（支持 ENC() 自动解密）
 * 类似 Spring Boot 的 @Value("${encrypted.property}") 自动解密功能
 */
public class JasyptUtils {
    private static final Pattern ENC_PATTERN = Pattern.compile("ENC\\(([^)]+)\\)");
    public static StandardPBEStringEncryptor getStandardEncryptor(String password) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setPassword(password);
        config.setKeyObtentionIterations("10000");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
    public static PooledPBEStringEncryptor getPooledEncryptor(String password, int poolSize) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setPassword(password);
        config.setKeyObtentionIterations("10000");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        config.setPoolSize(String.valueOf(poolSize));
        encryptor.setConfig(config);
        return encryptor;
    }
    public static String encrypt(String plainText, String password) {
        StandardPBEStringEncryptor encryptor = getStandardEncryptor(password);
        return encryptor.encrypt(plainText);
    }
    public static String decrypt(String encryptedText, String password) {
        StandardPBEStringEncryptor encryptor = getStandardEncryptor(password);
        return encryptor.decrypt(encryptedText);
    }
    public static String decryptWithPool(String encryptedText, String password, int poolSize) {
        PooledPBEStringEncryptor encryptor = getPooledEncryptor(password, poolSize);
        return encryptor.decrypt(encryptedText);
    }
    public static String encryptAuto(String plainText, String password) {
        StandardPBEStringEncryptor encryptor = getStandardEncryptor(password);
        return "ENC(" + encryptor.encrypt(plainText) + ")";
    }
    /**
     * 智能解密：如果是 ENC() 格式则解密，否则返回原值
     * @param value 待处理的值（可能是明文或 ENC(密文)）
     * @param password 主密码
     * @return 解密后的值（如果不需要解密则返回原值）
     */
    public static String decryptAuto(String value, String password) {
        if (value == null || value.isEmpty()) return value;
        Matcher matcher = ENC_PATTERN.matcher(value);
        if (matcher.matches()) {
            String encryptedText = matcher.group(1);
            return decrypt(encryptedText, password);
        }
        return value; // 明文直接返回
    }
    /**
     * 智能解密（使用连接池，高性能）
     * @param value 待处理的值
     * @param password 主密码
     * @param poolSize 连接池大小
     * @return 解密后的值
     */
    public static String decryptAutoWithPool(String value, String password, int poolSize) {
        if (value == null || value.isEmpty()) return value;
        Matcher matcher = ENC_PATTERN.matcher(value);
        if (matcher.matches()) {
            String encryptedText = matcher.group(1);
            return decryptWithPool(encryptedText, password, poolSize);
        }
        return value;
    }
}