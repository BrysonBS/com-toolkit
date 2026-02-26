package cn.com.toolkit.tools.image.support;

import org.apache.commons.lang3.StringUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class ImageSupport {
    public static BufferedImage resizeImage(BufferedImage originalImage,
                                            int targetWidth,
                                            int targetHeight,
                                            boolean keepAspectRatio) {
        if (originalImage == null) throw new IllegalArgumentException("原始图片不能为空");

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 保持宽高比计算
        if (keepAspectRatio) {
            double widthRatio = (double) targetWidth / originalWidth;
            double heightRatio = (double) targetHeight / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);

            targetWidth = (int) (originalWidth * ratio);
            targetHeight = (int) (originalHeight * ratio);
        }

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());

        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    public static void convertToIco(BufferedImage originalImage, File outputFile, int[] sizes,String format) throws IOException {
        BufferedImage[] iconImages = new BufferedImage[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            iconImages[i] = resizeToSquare(originalImage, size);
        }
        if(StringUtils.isBlank(format)) format = "ico";
        writeIcoFile(iconImages, outputFile,format);
    }
    private static void writeIcoFile(BufferedImage[] images, File outputFile,String format) throws IOException {
        // 获取ICO格式的ImageWriter
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);

        if (!writers.hasNext()) {
            throw new IOException("没有找到ICO格式的写入器。请确保已添加TwelveMonkeys依赖。");
        }

        ImageWriter writer = writers.next();
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);
            for (BufferedImage image : images) {
                IIOImage iioImage = new IIOImage(image, null, null);
                writer.writeToSequence(iioImage, null);
            }
            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }
    private static BufferedImage resizeToSquare(BufferedImage src, int size) {
        // 关键：必须使用 TYPE_4BYTE_ABGR
        BufferedImage dest = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D g2d = dest.createGraphics();

        // 设置渲染质量
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // 设置透明背景（重要！）
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, size, size);
        g2d.setComposite(AlphaComposite.SrcOver);

        // 计算缩放尺寸
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        double scale = Math.min((double) size / srcWidth, (double) size / srcHeight);
        int scaledWidth = (int) (srcWidth * scale);
        int scaledHeight = (int) (srcHeight * scale);

        // 计算居中位置
        int x = (size - scaledWidth) / 2;
        int y = (size - scaledHeight) / 2;

        // 绘制图像
        g2d.drawImage(src, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return dest;
    }


    public static BufferedImage convertToBinaryImage(BufferedImage source) {
        if(source.getType() == BufferedImage.TYPE_BYTE_BINARY) return source;
        // 先转换为灰度图
        BufferedImage grayImage = convertToGrayscale(source);
        // 计算大津阈值
        int threshold = calculateOtsuThreshold(grayImage);
        // 进行二值化
        return ditheringBinarization(grayImage, threshold);
    }
    private static int calculateOtsuThreshold(BufferedImage grayImage) {
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        // 计算灰度直方图
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = grayImage.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // 对于灰度图，R=G=B
                histogram[gray]++;
            }
        }

        // 大津法计算最佳阈值
        int total = width * height;
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        float sumB = 0;
        int wB = 0;
        int wF;
        float maxVariance = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;

            wF = total - wB;
            if (wF == 0) break;

            sumB += i * histogram[i];

            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            // 计算类间方差
            float variance = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }

        return threshold;
    }
    private static BufferedImage convertToGrayscale(BufferedImage colorImage) {
        BufferedImage grayImage = new BufferedImage(
                colorImage.getWidth(),
                colorImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        ColorConvertOp op = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                null
        );
        op.filter(colorImage, grayImage);

        return grayImage;
    }
    private static BufferedImage ditheringBinarization(BufferedImage grayImage, int threshold) {
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        // 复制灰度数据
        int[][] grayData = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grayData[y][x] = (grayImage.getRGB(x, y) >> 16) & 0xFF;
            }
        }

        // Floyd-Steinberg抖动算法
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int oldPixel = grayData[y][x];
                int newPixel = oldPixel > threshold ? 255 : 0;
                grayData[y][x] = newPixel;

                int error = oldPixel - newPixel;

                // 传播误差
                if (x + 1 < width) {
                    grayData[y][x + 1] = clamp(grayData[y][x + 1] + error * 7 / 16);
                }
                if (y + 1 < height) {
                    if (x > 0) {
                        grayData[y + 1][x - 1] = clamp(grayData[y + 1][x - 1] + error * 3 / 16);
                    }
                    grayData[y + 1][x] = clamp(grayData[y + 1][x] + error * 5 / 16);
                    if (x + 1 < width) {
                        grayData[y + 1][x + 1] = clamp(grayData[y + 1][x + 1] + error * 1 / 16);
                    }
                }
            }
        }

        // 创建二值图像
        BufferedImage binaryImage = new BufferedImage(
                width, height, BufferedImage.TYPE_BYTE_BINARY
        );

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = grayData[y][x] > 128 ? 0xFFFFFF : 0x000000;
                binaryImage.setRGB(x, y, value);
            }
        }

        return binaryImage;
    }
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

}
