package com.example.hubeiatlasbackend.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.imageio.ImageIO;

@Service
public class StyleTransferService {

    private final LLMService llmService;

    public StyleTransferService(LLMService llmService) {
        this.llmService = llmService;
    }

    /**
     * 生成调色板
     */
    public Map<String, Object> generatePalette(String styleText) {
        try {
            // 调用LLM服务生成调色板
            String llmResponse = llmService.generateColorPalette(styleText);

            // 解析LLM响应
            List<int[]> colors = parseResponse(llmResponse);

            Map<String, Object> result = new HashMap<>();
            result.put("colors", colors);
            result.put("styleText", styleText);
            result.put("success", true);
            return result;

        } catch (Exception e) {
            // 发生错误时返回默认调色板
            Map<String, Object> result = new HashMap<>();
            result.put("colors", getDefaultColors());
            result.put("styleText", styleText);
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 解析LLM响应 - 匹配Python文件格式
     */
    private List<int[]> parseResponse(String response) {
        try {
            String cleaned = response.replaceAll("[^\\d,\\(\\)]", "");
            String[] parts = cleaned.split("\\),\\s*\\(");
            List<int[]> colors = new ArrayList<>();

            for (String part : parts) {
                String[] rgb = part.replaceAll("[\\(\\)]", "").split(",");
                if (rgb.length >= 3) {
                    colors.add(new int[]{
                            Integer.parseInt(rgb[0].trim()),
                            Integer.parseInt(rgb[1].trim()),
                            Integer.parseInt(rgb[2].trim())
                    });
                }
            }
            return colors.size() == 5 ? colors : getDefaultColors();
        } catch (Exception e) {
            return getDefaultColors();
        }
    }

    private List<int[]> getDefaultColors() {
        return Arrays.asList(
                new int[]{240, 248, 255},
                new int[]{176, 196, 222},
                new int[]{100, 149, 237},
                new int[]{72, 61, 139},
                new int[]{25, 25, 112}
        );
    }

    public String applyStyle(File imageInput, List<int[]> colorTable, String picloc) throws IOException {
        BufferedImage image = ImageIO.read(imageInput);
        int width = image.getWidth();
        int height = image.getHeight();

        // 1. 提取所有非透明像素
        Set<Integer> uniqueColors = new HashSet<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;
                if (alpha > 0) { // 排除透明
                    uniqueColors.add(argb);
                }
            }
        }

        // 2. 转成 List<Color> 并过滤亮度范围（5~250）
        List<Color> filteredColors = new ArrayList<>();
        for (int argb : uniqueColors) {
            Color c = new Color(argb, true);
            double brightness = 0.3 * c.getRed() + 0.59 * c.getGreen() + 0.11 * c.getBlue();
            if (brightness > 5 && brightness < 250) {
                filteredColors.add(c);
            }
        }

        // 3. 按权重亮度排序（alpha=0.9亮度 + beta=0.1色相）
        filteredColors.sort((c1, c2) -> {
            double v1 = getWeightedSortValue(c1);
            double v2 = getWeightedSortValue(c2);
            return Double.compare(v2, v1); // 从高到低
        });

        // 4. 调色板插值，匹配颜色数量
        List<Color> interpolatedPalette = interpolatePalette(colorTable, filteredColors.size());

        // 5. 建立颜色映射表
        Map<Integer, Integer> colorMap = new HashMap<>();
        for (int i = 0; i < filteredColors.size(); i++) {
            Color src = filteredColors.get(i);
            Color tgt = interpolatedPalette.get(i);
            colorMap.put(src.getRGB(), new Color(tgt.getRed(), tgt.getGreen(), tgt.getBlue(), src.getAlpha()).getRGB());
        }

        // 6. 替换图片像素
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);
                if (colorMap.containsKey(argb)) {
                    image.setRGB(x, y, colorMap.get(argb));
                }
            }
        }

        String name = UUID.randomUUID().toString();
        File dir = new File(picloc);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 拼接完整文件路径
        File outputFile = new File(dir, name + ".jpg");
        // 写入文件
        ImageIO.write(image, "jpg", outputFile);
        return name;

    }

    // 亮度 + 色相加权
    private static double getWeightedSortValue(Color c) {
        double brightness = 0.3 * c.getRed() + 0.59 * c.getGreen() + 0.11 * c.getBlue();
        float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        return 0.9 * brightness + 0.1 * (hsv[0] * 255);
    }

    // 调色板插值
    private static List<Color> interpolatePalette(List<int[]> palette, int targetCount) {
        List<Color> result = new ArrayList<>();
        int n = palette.size();
        for (int i = 0; i < targetCount; i++) {
            double t = i / (double) (targetCount - 1);
            int r = (int) Math.round(interpolateChannel(palette, t, 0));
            int g = (int) Math.round(interpolateChannel(palette, t, 1));
            int b = (int) Math.round(interpolateChannel(palette, t, 2));
            result.add(new Color(r, g, b));
        }
        return result;
    }

    private static double interpolateChannel(List<int[]> palette, double t, int channel) {
        int n = palette.size();
        double scaledT = t * (n - 1);
        int index = (int) Math.floor(scaledT);
        double frac = scaledT - index;

        if (index >= n - 1) {
            return palette.get(n - 1)[channel];
        }
        int c1 = palette.get(index)[channel];
        int c2 = palette.get(index + 1)[channel];
        return c1 + (c2 - c1) * frac;
    }
}