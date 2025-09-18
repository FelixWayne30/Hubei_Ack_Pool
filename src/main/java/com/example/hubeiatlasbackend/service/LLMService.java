package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.config.LLMConfig;
import com.example.hubeiatlasbackend.mapper.SubmapsMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class LLMService {

    @Resource
    private LLMConfig llmConfig;

    @Resource
    private SubmapsMapper submapsMapper;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LLMService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 智能匹配地图子项
     * @param userInput 用户输入的查询内容
     * @return 匹配结果列表（按相关性降序排列，仅返回评分>5的结果）
     */
    public List<SmartSearchResult> intelligentMapMatching(String userInput) {
        try {
            log.info("开始智能地图匹配，用户输入: {}", userInput);

            // 获取所有子项名称数据
            List<String> allSubitemNames = submapsMapper.getAllSubitemNames();
            log.info("获取到{}个子项名称用于匹配", allSubitemNames.size());

            if (allSubitemNames.isEmpty()) {
                log.warn("数据库中没有子项名称数据");
                return Collections.emptyList();
            }

            // 构建智能匹配提示词
            String matchingPrompt = buildIntelligentMatchingPrompt(userInput, allSubitemNames);

            // 调用大模型进行匹配分析
            String llmResponse = callLLM(matchingPrompt);

            // 解析匹配结果并获取详细信息
            List<SmartSearchResult> searchResults = parseAndEnrichMatchingResults(llmResponse);

            log.info("匹配完成，返回{}个结果", searchResults.size());
            return searchResults;

        } catch (Exception e) {
            log.error("智能地图匹配失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<SmartSearchResult> parseAndEnrichMatchingResults(String response) {
        List<SmartSearchResult> results = new ArrayList<>();

        if (response == null || response.trim().isEmpty()) {
            return results;
        }

        try {
            String[] lines = response.split("\n");

            for (String line : lines) {
                line = line.trim();

                // 跳过空行和标题行
                if (line.isEmpty() || line.startsWith("匹配结果") || line.startsWith("请开始")) {
                    continue;
                }

                // 匹配格式：数字. [子项名称] - [评分]
                if (line.matches("^\\d+\\.\\s*.+\\s*-\\s*\\d+.*")) {
                    try {
                        // 移除序号
                        String content = line.replaceFirst("^\\d+\\.\\s*", "");

                        // 分割名称和评分
                        String[] parts = content.split("\\s*-\\s*");
                        if (parts.length >= 2) {
                            String subitemName = parts[0].trim();

                            // 提取评分数字
                            String scoreStr = parts[1].replaceAll("[^\\d]", "");
                            if (!scoreStr.isEmpty()) {
                                int score = Integer.parseInt(scoreStr);
                                score = Math.max(1, Math.min(10, score)); // 限制在1-10范围

                                // 只处理评分大于5分的结果
                                if (score > 5) {
                                    // 从数据库获取详细信息
                                    List<Map<String, Object>> submapDetails = submapsMapper.getSubmapsBySubitemName(subitemName);

                                    if (!submapDetails.isEmpty()) {
                                        // 可能一个子项对应多个地图，取第一个
                                        Map<String, Object> detail = submapDetails.get(0);

                                        SmartSearchResult searchResult = new SmartSearchResult();
                                        searchResult.setSubitemName(subitemName);
                                        searchResult.setScore(score);

                                        if (detail.get("map_id") != null) {
                                            searchResult.setMapId(detail.get("map_id").toString());
                                        }

                                        searchResult.setMapTitle(detail.get("map_title") != null ?
                                                detail.get("map_title").toString() :
                                                detail.get("parent_map_name").toString());

                                        searchResult.setSubitemType(detail.get("subitem_type") != null ?
                                                detail.get("subitem_type").toString() : "未知类型");

                                        searchResult.setExtendsBounds(
                                                detail.get("extends_xmin"),
                                                detail.get("extends_ymin"),
                                                detail.get("extends_xmax"),
                                                detail.get("extends_ymax")
                                        );

                                        results.add(searchResult);
                                        log.debug("成功解析匹配结果: {} - 评分: {}", subitemName, score);
                                    } else {
                                        log.warn("未找到子项 '{}' 的详细信息", subitemName);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析行失败: {}, 错误: {}", line, e.getMessage());
                    }
                }
            }

            // 按评分降序排列
            results.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        } catch (Exception e) {
            log.error("解析匹配结果失败: {}", e.getMessage(), e);
        }

        return results;
    }


    private String buildIntelligentMatchingPrompt(String userInput, List<String> subitemNames) {
        // 为了避免提示词过长，采样处理
        List<String> sampledNames = sampleSubitemNames(subitemNames, 100);
        String namesList = String.join("、", sampledNames);

        return String.format(
                "你是一个地理信息专家，需要根据用户的查询意图，从湖北省地图数据库中找出最相关的地图子项。\n\n" +
                        "用户查询：%s\n\n" +
                        "可选的地图子项名称列表：\n%s\n\n" +
                        "任务要求：\n" +
                        "1. 深度理解用户的查询意图（可能是找特定地区、资源类型、地理要素等）\n" +
                        "2. 从上述列表中选出与用户查询最相关的地图子项\n" +
                        "3. 按相关性从高到低排序，返回所有相关度较高的结果\n" +
                        "4. 为每个匹配项给出相关性评分（1-10分，10分最相关）\n" +
                        "5. 只推荐评分在6分及以上的高质量匹配结果\n\n" +
                        "分析原则：\n" +
                        "- 优先匹配直接相关的地名和地理要素\n" +
                        "- 考虑同义词、别称和相关概念\n" +
                        "- 理解用户可能的隐含需求\n" +
                        "- 确保推荐结果的高质量和相关性\n" +
                        "- 宁缺毋滥，只推荐真正相关的结果\n\n" +
                        "输出格式（严格按此格式）：\n" +
                        "匹配结果：\n" +
                        "1. [子项名称] - [相关性评分]\n" +
                        "2. [子项名称] - [相关性评分]\n" +
                        "...\n\n" +
                        "请开始分析匹配：",
                userInput, namesList
        );
    }

    /**
     * 采样子项名称（避免提示词过长）
     */
    private List<String> sampleSubitemNames(List<String> allNames, int maxCount) {
        if (allNames.size() <= maxCount) {
            return allNames;
        }

        // 简单采样策略：保留前面部分 + 随机采样
        List<String> sampled = new ArrayList<>();
        sampled.addAll(allNames.subList(0, Math.min(50, allNames.size())));

        List<String> remaining = allNames.subList(50, allNames.size());
        Collections.shuffle(remaining);
        sampled.addAll(remaining.subList(0, Math.min(maxCount - 50, remaining.size())));

        return sampled;
    }

    private String callLLM(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", llmConfig.getModel());

            Map<String, Object> input = new HashMap<>();
            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);

            input.put("messages", messages);
            requestBody.put("input", input);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("temperature", 0.1); // 降低随机性，提高一致性
            parameters.put("max_tokens", 1200); // 增加token以支持更多匹配结果
            requestBody.put("parameters", parameters);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + llmConfig.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    llmConfig.getBaseUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return extractContentFromResponse(response.getBody());
        } catch (Exception e) {
            log.error("调用LLM API失败: {}", e.getMessage(), e);
            throw new RuntimeException("LLM服务调用失败", e);
        }
    }

    /**
     * 从API响应中提取内容
     */
    private String extractContentFromResponse(String response) {
        try {
            if (response == null || response.trim().isEmpty()) {
                throw new RuntimeException("LLM响应为空");
            }

            JsonNode jsonNode = objectMapper.readTree(response);

            // 检查是否有错误
            if (jsonNode.has("code") && !jsonNode.path("code").asText().equals("200")) {
                String code = jsonNode.path("code").asText();
                String message = jsonNode.path("message").asText();
                throw new RuntimeException("LLM API错误: " + code + " - " + message);
            }

            // 检查output字段
            if (!jsonNode.has("output")) {
                throw new RuntimeException("响应格式错误：缺少output字段");
            }

            JsonNode outputNode = jsonNode.path("output");

            // 阿里云百炼使用text字段
            if (outputNode.has("text")) {
                return outputNode.path("text").asText();
            }

            if (outputNode.has("choices")) {
                JsonNode choicesNode = outputNode.path("choices");
                if (choicesNode.isArray() && choicesNode.size() > 0) {
                    JsonNode firstChoice = choicesNode.get(0);
                    if (firstChoice.has("message")) {
                        return firstChoice.path("message").path("content").asText();
                    }
                }
            }

            throw new RuntimeException("响应格式错误：找不到内容字段");

        } catch (Exception e) {
            log.error("解析LLM响应失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析LLM响应失败", e);
        }
    }

    // 工具方法，保持兼容性
    public List<String> getAllSubitemNames() {
        try {
            return submapsMapper.getAllSubitemNames();
        } catch (Exception e) {
            log.error("获取所有子项名称失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getSubmapsBySubitemName(String subitemName) {
        try {
            return submapsMapper.getSubmapsBySubitemName(subitemName);
        } catch (Exception e) {
            log.error("根据子项名称获取子图信息失败: {}, subitemName: {}", e.getMessage(), subitemName, e);
            return Collections.emptyList();
        }
    }

    /**
     * 生成调色板用于风格迁移
     */
    public String generateColorPalette(String styleText) {
        try {
            log.info("开始生成调色板，风格描述: {}", styleText);

            String prompt = buildColorPalettePrompt(styleText);
            String response = callLLM(prompt);

            log.info("调色板生成完成");
            return response;

        } catch (Exception e) {
            log.error("调色板生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("调色板生成失败", e);
        }
    }

    /**
     * 调色板生成用Prompt
     */
    private String buildColorPalettePrompt(String styleText) {
        return String.format(
                "你是一位专业的地图与图像配色设计专家，请为我生成一组用于颜色转换的调色板，包含5个颜色，要求如下：\n" +
                "\n" +
                "- 每个颜色之间色相要有差异，但不要出现互补色（不能红配绿、蓝配橙等）；\n" +
                "- 每个颜色的饱和度保持适中或偏低，避免刺眼或过分鲜艳，但不能偏灰；\n" +
                "- 输出结果为5个RGB颜色值，排序要从浅到深（从背景色到突出色）；\n" +
                "- 风格要求协调、柔和、美观，适合用于地图、图像、数据可视化等领域的配色转换；\n" +
                "- 对地图配色的期望描述：%s\n" +
                "- 请仅返回如下JSON格式输出：\n" +
                "[\n" +
                "    (R1, G1, B1),\n" +
                "    (R2, G2, B2),\n" +
                "    (R3, G3, B3),\n" +
                "    (R4, G4, B4),\n" +
                "    (R5, G5, B5)\n" +
                "]"
                , styleText);
    }

    /**
     * 智能搜索结果类
     */

    public static class SmartSearchResult {
        private String subitemName;
        private String subitemType;
        private String mapId;
        private String mapTitle;
        private String parentMapName;  // 新增：父地图名称
        private int score;
        private Map<String, Object> extendsBounds;  // 边界信息

        public void setExtendsBounds(Object xmin, Object ymin, Object xmax, Object ymax) {
            this.extendsBounds = new HashMap<>();
            this.extendsBounds.put("xmin", xmin);
            this.extendsBounds.put("ymin", ymin);
            this.extendsBounds.put("xmax", xmax);
            this.extendsBounds.put("ymax", ymax);
        }

        public String getSubitemName() {
            return subitemName;
        }

        public void setSubitemName(String subitemName) {
            this.subitemName = subitemName;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getMapId() {
            return mapId;
        }

        public void setMapId(String mapId) {
            this.mapId = mapId;
        }

        public String getMapTitle() {
            return mapTitle;
        }

        public void setMapTitle(String mapTitle) {
            this.mapTitle = mapTitle;
        }

        public String getSubitemType() {
            return subitemType;
        }

        public void setSubitemType(String subitemType) {
            this.subitemType = subitemType;
        }

        @Override
        public String toString() {
            return "SmartSearchResult{" +
                    "subitemName='" + subitemName + '\'' +
                    ", score=" + score +
                    ", mapId='" + mapId + '\'' +
                    ", mapTitle='" + mapTitle + '\'' +
                    ", subitemType='" + subitemType + '\'' +
                    '}';
        }
    }
}