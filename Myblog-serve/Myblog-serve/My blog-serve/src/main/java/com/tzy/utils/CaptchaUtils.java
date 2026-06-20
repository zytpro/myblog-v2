package com.tzy.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码工具类
 * 用于生成滑动验证码图片
 */
public class CaptchaUtils {

    private static final Random RANDOM = new Random();

    /**
     * 获取指定范围内的随机整数
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机整数
     */
    public static int getRandomInt(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * 创建验证码图片
     *
     * @param width 图片宽度
     * @param height 图片高度
     * @param blockX 滑块X坐标
     * @param blockY 滑块Y坐标
     * @param blockSize 滑块大小
     * @return 包含背景图和滑块图的数组
     */
    public static BufferedImage[] createCaptchaImages(int width, int height, int blockX, int blockY, int blockSize) {
        // 创建背景图
        BufferedImage bgImage = createBackgroundImage(width, height);

        // 创建滑块图（使用ARGB支持透明）
        BufferedImage blockImage = new BufferedImage(blockSize, blockSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D blockG = blockImage.createGraphics();

        // 设置抗锯齿和高质量渲染
        blockG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        blockG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        blockG.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        // 创建滑块形状
        Shape shape = createBlockShape(blockSize);

        // 创建裁剪区域
        blockG.setClip(shape);

        // 从背景图中裁剪滑块
        BufferedImage subImage = bgImage.getSubimage(blockX, blockY, blockSize, blockSize);
        blockG.drawImage(subImage, 0, 0, null);

        // 绘制滑块内部阴影
        blockG.setClip(shape);
        blockG.setColor(new Color(0, 0, 0, 76)); // 约30%不透明度
        blockG.setStroke(new BasicStroke(4));
        blockG.draw(shape);

        // 绘制滑块外边框（加粗且更明显）
        blockG.setClip(null);
        // 绘制外层白边
        blockG.setColor(new Color(255, 255, 255, 178)); // 约70%不透明度
        blockG.setStroke(new BasicStroke(3));
        blockG.draw(shape);
        // 绘制内层深色边框
        blockG.setColor(new Color(50, 50, 50, 127)); // 约50%不透明度
        blockG.setStroke(new BasicStroke(1.5f));
        blockG.draw(shape);

        // 在背景图上绘制滑块缺口
        Graphics2D bgG = bgImage.createGraphics();
        bgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制缺口阴影
        bgG.setColor(new Color(0, 0, 0, 102)); // 约40%不透明度
        bgG.setStroke(new BasicStroke(4));
        bgG.draw(createTranslatedShape(shape, blockX + 1, blockY + 1));

        // 绘制缺口边框
        bgG.setColor(new Color(255, 255, 255, 127)); // 约50%不透明度
        bgG.setStroke(new BasicStroke(2));
        bgG.draw(createTranslatedShape(shape, blockX, blockY));

        // 释放资源
        blockG.dispose();
        bgG.dispose();

        return new BufferedImage[]{bgImage, blockImage};
    }


    /**
     * 创建滑块形状
     *
     * @param size 滑块大小
     * @return 滑块形状
     */
    private static Shape createBlockShape(int size) {
        int gap = size / 4;

        // 创建圆角矩形路径
        Path2D path = new Path2D.Float();

        // 使用更圆滑的曲线
        path.moveTo(gap, 0);
        path.lineTo(size - gap, 0);

        // 右边凸起（使用更平滑的曲线）
        path.quadTo(size - gap/2, gap/2, size - gap, gap);
        path.quadTo(size - gap/2, gap*1.5f, size, gap*2);
        path.quadTo(size - gap/2, gap*2.5f, size - gap, gap*3);
        path.lineTo(size - gap, size);

        // 底边
        path.lineTo(gap, size);

        // 左边凹陷（使用更平滑的曲线）
        path.lineTo(gap, gap*3);
        path.quadTo(gap/2, gap*2.5f, 0, gap*2);
        path.quadTo(gap/2, gap*1.5f, gap, gap);

        path.closePath();

        return path;
    }

    /**
     * 创建平移后的形状
     *
     * @param shape 原始形状
     * @param x X轴平移距离
     * @param y Y轴平移距离
     * @return 平移后的形状
     */
    private static Shape createTranslatedShape(Shape shape, int x, int y) {
        AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
        return transform.createTransformedShape(shape);
    }

    /**
     * 将图片转换为Base64编码
     *
     * @param image 图片
     * @return Base64编码字符串
     * @throws IOException 转换过程中可能出现的异常
     */
    public static String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * 创建背景图片（公共方法）
     *
     * @param width 图片宽度
     * @param height 图片高度
     * @return 背景图片
     */
    public static BufferedImage createBackgroundImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 设置较浅的背景色
        g.setColor(new Color(getRandomInt(230, 250), getRandomInt(230, 250), getRandomInt(230, 250)));
        g.fillRect(0, 0, width, height);

        // 绘制随机干扰线（减少数量，增加透明度）
        g.setStroke(new BasicStroke(0.8f));
        for (int i = 0; i < 50; i++) {
            int alpha = getRandomInt(30, 60);
            g.setColor(new Color(
                getRandomInt(100, 180),
                getRandomInt(100, 180),
                getRandomInt(100, 180),
                alpha
            ));
            int x1 = getRandomInt(0, width);
            int y1 = getRandomInt(0, height);
            int x2 = getRandomInt(0, width);
            int y2 = getRandomInt(0, height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制随机干扰圆（减少数量，增加大小）
        for (int i = 0; i < 10; i++) {
            int alpha = getRandomInt(30, 60);
            g.setColor(new Color(
                getRandomInt(100, 200),
                getRandomInt(100, 200),
                getRandomInt(100, 200),
                alpha
            ));
            int x = getRandomInt(0, width);
            int y = getRandomInt(0, height);
            int size = getRandomInt(20, 40);
            g.fillOval(x, y, size, size);
        }

        g.dispose();
        return image;
    }

    /**
     * 生成随机X位置
     *
     * @return 随机X坐标
     */
    public static int generateRandomXPosition() {
        return getRandomInt(50, 200); // 在50-200像素范围内生成随机X位置
    }

    /**
     * 创建滑块图片
     *
     * @param backgroundImage 背景图片
     * @param xPosition X位置
     * @return 滑块图片
     */
    public static BufferedImage createSliderImage(BufferedImage backgroundImage, int xPosition) {
        int blockSize = 50; // 滑块大小
        int yPosition = getRandomInt(50, backgroundImage.getHeight() - blockSize - 50);
        
        BufferedImage[] images = createCaptchaImages(
            backgroundImage.getWidth(), 
            backgroundImage.getHeight(), 
            xPosition, 
            yPosition, 
            blockSize
        );
        
        return images[1]; // 返回滑块图片
    }
}
