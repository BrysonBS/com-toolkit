package cn.com.toolkit.framework.core.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Google ZXing 二维码生成通用工具类
 * 支持自定义尺寸、添加Logo、设置纠错级别、边距等
 */
public class QRCodeUtil {

    // 默认字符集
    private static final String DEFAULT_CHARSET = "UTF-8";

    // 默认图片格式
    private static final String DEFAULT_FORMAT = "PNG";

    // 默认二维码宽度
    private static final int DEFAULT_WIDTH = 300;

    // 默认二维码高度
    private static final int DEFAULT_HEIGHT = 300;

    // Logo默认大小比例（二维码宽度的15%）
    private static final double DEFAULT_LOGO_PART = 0.15;

    // Logo默认边框颜色
    private static final Color DEFAULT_BORDER_COLOR = Color.WHITE;


    // 默认前景色（黑色）
    private static final int DEFAULT_BLACK = 0xFF000000;

    // 默认背景色（白色）
    private static final int DEFAULT_WHITE = 0xFFFFFFFF;

    /**
     * QRCode配置类 - 链式调用
     */
    public static class QRCodeConfig {
        private String content;                 // 二维码内容
        private String charset = DEFAULT_CHARSET; // 字符集
        private String format = DEFAULT_FORMAT;   // 图片格式
        private int width = DEFAULT_WIDTH;       // 宽度
        private int height = DEFAULT_HEIGHT;     // 高度
        private int margin = 1;                   // 边距
        private ErrorCorrectionLevel errorLevel = ErrorCorrectionLevel.H; // 纠错级别
        private Integer onColor = DEFAULT_BLACK;  // 前景色
        private Integer offColor = DEFAULT_WHITE; // 背景色
        private BufferedImage logoImage;                     // Logo文件
        private double logoPart = DEFAULT_LOGO_PART;  // Logo大小比例
        private Color borderColor = DEFAULT_BORDER_COLOR; // Logo边框颜色
        private Integer borderWidth;                // Logo边框宽度
        private Integer logoWidth;                  // 自定义Logo宽度
        private Integer logoHeight;                  // 自定义Logo高度
        private Boolean needBorder = true;           // 是否需要边框

        public QRCodeConfig(String content) {
            this.content = content;
        }

        public QRCodeConfig setCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public QRCodeConfig setFormat(String format) {
            this.format = format;
            return this;
        }

        public QRCodeConfig setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public QRCodeConfig setWidth(int width) {
            this.width = width;
            return this;
        }

        public QRCodeConfig setHeight(int height) {
            this.height = height;
            return this;
        }

        public QRCodeConfig setMargin(int margin) {
            this.margin = margin;
            return this;
        }

        public QRCodeConfig setErrorLevel(ErrorCorrectionLevel errorLevel) {
            this.errorLevel = errorLevel;
            return this;
        }

        public QRCodeConfig setColor(int onColor, int offColor) {
            this.onColor = onColor;
            this.offColor = offColor;
            return this;
        }

        public QRCodeConfig setLogo(BufferedImage logoImage) {
            this.logoImage = logoImage;
            return this;
        }
        public QRCodeConfig setLogo(BufferedImage logoImage, int logoPart) {
            this.logoImage = logoImage;
            this.logoPart = logoPart;
            return this;
        }
        public QRCodeConfig setLogo(BufferedImage logoImage, int logoWidth, int logoHeight) {
            this.logoImage = logoImage;
            this.logoWidth = logoWidth;
            this.logoHeight = logoHeight;
            return this;
        }
        public QRCodeConfig setLogoPart(double logoPart) {
            this.logoPart = logoPart;
            return this;
        }

        public QRCodeConfig setLogoBorder(Color borderColor, Integer borderWidth) {
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
            return this;
        }

        public QRCodeConfig setNeedBorder(boolean needBorder) {
            this.needBorder = needBorder;
            return this;
        }
    }

    /**
     * 生成二维码（默认配置）
     */
    public static BufferedImage generateQRCode(String content) throws Exception {
        return generateQRCode(new QRCodeConfig(content));
    }

    /**
     * 生成二维码（指定尺寸）
     */
    public static BufferedImage generateQRCode(String content, int width, int height) throws Exception {
        return generateQRCode(new QRCodeConfig(content).setSize(width, height));
    }

    /**
     * 生成二维码（核心方法）
     * @param config 二维码配置
     * @return BufferedImage对象
     */
    public static BufferedImage generateQRCode(QRCodeConfig config) throws Exception {
        if (config.content == null || config.content.trim().isEmpty()) {
            throw new IllegalArgumentException("二维码内容不能为空");
        }

        // 1. 设置编码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, config.charset);
        hints.put(EncodeHintType.ERROR_CORRECTION, config.errorLevel);
        hints.put(EncodeHintType.MARGIN, config.margin);

        // 2. 创建BitMatrix比特矩阵 [citation:3]
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                config.content,
                BarcodeFormat.QR_CODE,
                config.width,
                config.height,
                hints
        );

        // 3. 转换为BufferedImage
        BufferedImage image;
        if (config.onColor != null && config.offColor != null) {
            MatrixToImageConfig matrixConfig = new MatrixToImageConfig(config.onColor, config.offColor);
            image = MatrixToImageWriter.toBufferedImage(bitMatrix, matrixConfig);
        } else image = MatrixToImageWriter.toBufferedImage(bitMatrix);
        // 4. 如果需要添加Logo
        if (config.logoImage != null) {
            if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage argbImage = new BufferedImage(
                        image.getWidth(),
                        image.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g = argbImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                // 设置抗锯齿
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                image = argbImage;
            }
            addLogo(image, config);
        }

        return image;
    }

    /**
     * 添加Logo到二维码图片 [citation:7]
     * @param image 二维码图片
     * @param config 配置信息
     * @return 带Logo的二维码图片
     */
    private static void addLogo(BufferedImage image, QRCodeConfig config) {
        Graphics2D graphics = image.createGraphics();
        // 读取Logo图片
        BufferedImage logo = config.logoImage;

        // 计算Logo尺寸（优先使用自定义尺寸，否则按比例计算）
        int logoWidth, logoHeight;
        if (config.logoWidth != null && config.logoHeight != null) {
            logoWidth = config.logoWidth;
            logoHeight = config.logoHeight;
        } else {
            // Logo大小为二维码宽度的1/config.logoPart
            logoWidth = (int) Math.round(image.getWidth() * config.logoPart);
            logoHeight = (int) Math.round(image.getHeight() * config.logoPart);
        }

        // 如果Logo本身尺寸过大，进行缩放
        if (logo.getWidth() > logoWidth || logo.getHeight() > logoHeight) {
            // 保持宽高比缩放
            double scale = Math.min(
                    (double) logoWidth / logo.getWidth(),
                    (double) logoHeight / logo.getHeight()
            );
            logoWidth = (int) (logo.getWidth() * scale);
            logoHeight = (int) (logo.getHeight() * scale);
        }

        // 计算Logo放置位置（居中）
        int x = (image.getWidth() - logoWidth) / 2;
        int y = (image.getHeight() - logoHeight) / 2;

        //边框
        int borderWidth = config.borderWidth == null ? Math.round(Math.min(Math.min(logoWidth,logoHeight) * 0.2f, 50f)) : config.borderWidth;

        // 绘制Logo
        graphics.drawImage(
                makeRoundedCorner(logo,logoWidth,logoHeight,borderWidth, borderWidth * 2,config.borderColor,config.needBorder)
                , x, y, logoWidth, logoHeight, null);

        graphics.dispose();
        image.flush();
    }

    /**
     * 将二维码输出到文件
     */
    public static void writeToFile(QRCodeConfig config, String filePath) throws Exception {
        BufferedImage image = generateQRCode(config);
        File outputFile = new File(filePath);
        ImageIO.write(image, config.format, outputFile);
    }

    /**
     * 将二维码输出到流
     */
    public static void writeToStream(QRCodeConfig config, OutputStream stream) throws Exception {
        BufferedImage image = generateQRCode(config);
        ImageIO.write(image, config.format, stream);
    }

    /**
     * 将二维码转换为Base64字符串
     */
    public static String toBase64(QRCodeConfig config) throws Exception {
        BufferedImage image = generateQRCode(config);
        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        ImageIO.write(image, config.format, os);
        return java.util.Base64.getEncoder().encodeToString(os.toByteArray());
    }

    /**
     * 解析二维码内容
     */
    public static String decodeQRCode(File qrCodeFile) throws Exception {
        BufferedImage image = ImageIO.read(qrCodeFile);
        return decodeQRCode(image);
    }

    /**
     * 解析二维码内容
     */
    public static String decodeQRCode(BufferedImage image) throws Exception {
        if (image == null) {
            return null;
        }

        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, DEFAULT_CHARSET);

        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.getText();
    }
    private static BufferedImage makeRoundedCorner(BufferedImage image,
                                                   int targetWidth,
                                                   int targetHeight,
                                                   int borderWidth,
                                                   int borderRadius,
                                                   Color borderColor,
                                                   boolean needBorder) {
        // 创建透明背景的画布
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();

        // 开启抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1. 绘制圆角矩形路径（用于裁剪和边框）
        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(
                0, 0,
                targetWidth, targetHeight,
                borderRadius, borderRadius);

        // 2. 设置裁剪区域（确保图片只在圆角矩形内显示）
        g2d.setClip(roundedRectangle);

        // 3. 绘制缩放的Logo图片
        Image scaledLogo = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        g2d.drawImage(scaledLogo, 0, 0, null);

        // 4. 移除裁剪（准备绘制边框）
        g2d.setClip(null);
        // 5. 绘制边框
        if (needBorder) {
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(borderWidth,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(roundedRectangle);
        }
        g2d.dispose();
        return output;
    }
}