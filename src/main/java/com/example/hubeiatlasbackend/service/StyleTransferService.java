package com.example.hubeiatlasbackend.service;

import org.springframework.stereotype.Service;

import java.util.*;

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
}