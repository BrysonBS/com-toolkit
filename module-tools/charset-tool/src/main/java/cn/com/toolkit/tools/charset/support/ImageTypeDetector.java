package cn.com.toolkit.tools.charset.support;

import java.util.HashMap;
import java.util.Map;

public class ImageTypeDetector {

    private static final Map<String, byte[]> MAGIC_NUMBERS = new HashMap<>();

    static {
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        MAGIC_NUMBERS.put("PNG", new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

        // JPEG: FF D8 FF
        MAGIC_NUMBERS.put("JPG", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});

        // GIF89a: 47 49 46 38 39 61
        MAGIC_NUMBERS.put("GIF", new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61});
        // GIF87a: 47 49 46 38 37 61
        MAGIC_NUMBERS.put("GIF", new byte[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61});

        // BMP: 42 4D
        MAGIC_NUMBERS.put("BMP", new byte[]{0x42, 0x4D});

        // WEBP: RIFF....WEBP (需要特殊处理)
        // TIFF: 49 49 2A 00 或 4D 4D 00 2A
        MAGIC_NUMBERS.put("TIFF_LE", new byte[]{0x49, 0x49, 0x2A, 0x00}); // 小端序
        MAGIC_NUMBERS.put("TIFF_BE", new byte[]{0x4D, 0x4D, 0x00, 0x2A}); // 大端序
    }

    /**
     * 根据字节数组检测图片类型
     * @param imageBytes 图片字节数组
     * @return 图片格式（PNG、JPG、GIF、BMP、WEBP、TIFF、UNKNOWN）
     */
    public static String detectImageType(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4) {
            return "UNKNOWN";
        }

        // 检测 PNG
        if (startsWith(imageBytes, MAGIC_NUMBERS.get("PNG"))) {
            return "PNG";
        }

        // 检测 JPEG
        if (startsWith(imageBytes, MAGIC_NUMBERS.get("JPG"))) {
            return "JPG";
        }

        // 检测 GIF
        if (startsWith(imageBytes, new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61}) ||
                startsWith(imageBytes, new byte[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61})) {
            return "GIF";
        }

        // 检测 BMP
        if (startsWith(imageBytes, new byte[]{0x42, 0x4D})) {
            return "BMP";
        }

        // 检测 WEBP (RIFF xxxx WEBP)
        if (imageBytes.length >= 12 &&
                startsWith(imageBytes, new byte[]{0x52, 0x49, 0x46, 0x46}) && // "RIFF"
                imageBytes[8] == 0x57 && imageBytes[9] == 0x45 && // "W" "E"
                imageBytes[10] == 0x42 && imageBytes[11] == 0x50) { // "B" "P"
            return "WEBP";
        }

        // 检测 TIFF
        if (startsWith(imageBytes, new byte[]{0x49, 0x49, 0x2A, 0x00}) ||
                startsWith(imageBytes, new byte[]{0x4D, 0x4D, 0x00, 0x2A})) {
            return "TIFF";
        }

        return "UNKNOWN";
    }

    /**
     * 检查字节数组是否以指定魔数开头
     */
    private static boolean startsWith(byte[] data, byte[] magic) {
        if (data.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (data[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取文件的 MIME 类型
     */
    public static String getMimeType(byte[] imageBytes) {
        String type = detectImageType(imageBytes);
        switch (type) {
            case "PNG": return "image/png";
            case "JPG": return "image/jpeg";
            case "GIF": return "image/gif";
            case "BMP": return "image/bmp";
            case "WEBP": return "image/webp";
            case "TIFF": return "image/tiff";
            default: return "application/octet-stream";
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(byte[] imageBytes) {
        String type = detectImageType(imageBytes);
        switch (type) {
            case "PNG": return ".png";
            case "JPG": return ".jpg";
            case "GIF": return ".gif";
            case "BMP": return ".bmp";
            case "WEBP": return ".webp";
            case "TIFF": return ".tiff";
            default: return ".bin";
        }
    }
}