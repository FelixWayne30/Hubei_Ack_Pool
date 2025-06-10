package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.config.LLMConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LLMService {

    @Resource
    private LLMConfig llmConfig;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LLMService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 智能提取地理关键词 - 增强版
     * @param userInput 用户输入的搜索内容
     * @return 提取和扩展的关键词列表
     */
    public List<String> extractGeoKeywords(String userInput) {
        try {
            log.info("开始智能关键词提取，用户输入: {}", userInput);

            // 第一步：基础关键词提取
            String basicPrompt = buildBasicKeywordPrompt(userInput);
            String basicResponse = callLLM(basicPrompt);
            List<String> basicKeywords = parseKeywordsFromResponse(basicResponse);
            log.info("基础关键词: {}", basicKeywords);

            // 第二步：地理关联扩展
            String expandPrompt = buildGeographicExpansionPrompt(userInput, basicKeywords);
            String expandResponse = callLLM(expandPrompt);
            List<String> expandedKeywords = parseKeywordsFromResponse(expandResponse);
            log.info("扩展关键词: {}", expandedKeywords);

            // 第三步：合并去重并优化
            List<String> finalKeywords = mergeAndOptimizeKeywords(basicKeywords, expandedKeywords);
            log.info("最终关键词: {}", finalKeywords);

            return finalKeywords;
        } catch (Exception e) {
            log.error("智能关键词提取失败: {}", e.getMessage(), e);
            // 降级处理：使用简单关键词提取
            return fallbackKeywordExtraction(userInput);
        }
    }

    /**
     * 构建基础关键词提取提示词
     */
    private String buildBasicKeywordPrompt(String userInput) {
        return String.format(
                "作为地理信息专家，请从用户输入中提取核心地理关键词：\n\n" +
                        "用户输入：%s\n\n" +
                        "提取要求：\n" +
                        "1. 直接的地名（省、市、县、区、乡镇、村庄）\n" +
                        "2. 自然地理要素（山、河、湖、海、岛屿、平原、盆地）\n" +
                        "3. 资源类型（森林、矿产、水资源、土地、湿地）\n" +
                        "4. 地理概念（流域、山脉、地区、区域）\n\n" +
                        "输出格式：只返回关键词，用逗号分隔，最多8个\n" +
                        "关键词：",
                userInput
        );
    }

    /**
     * 构建地理关联扩展提示词
     */
    private String buildGeographicExpansionPrompt(String userInput, List<String> basicKeywords) {
        String keywordStr = String.join("、", basicKeywords);

        return String.format(
                "作为地理专家，基于用户查询和已提取的关键词，进行地理关联扩展：\n\n" +
                        "原始查询：%s\n" +
                        "已提取关键词：%s\n\n" +
                        "请扩展相关的地理实体：\n" +
                        "1. 如果涉及\"长江流域\"，列出湖北省内长江沿线城市：武汉、宜昌、荆州、黄石、鄂州、咸宁、仙桃、潜江、天门\n" +
                        "2. 如果涉及\"汉江流域\"，列出相关城市：襄阳、十堰、随州、孝感\n" +
                        "3. 如果涉及\"鄂西\"或\"西部\"，列出：恩施、宜昌、十堰、神农架\n" +
                        "4. 如果涉及\"鄂东\"或\"东部\"，列出：黄石、鄂州、黄冈、咸宁\n" +
                        "5. 如果涉及\"鄂北\"或\"北部\"，列出：襄阳、十堰、随州、孝感\n" +
                        "6. 如果涉及\"鄂南\"或\"南部\"，列出：咸宁、通山、崇阳、嘉鱼\n" +
                        "7. 如果涉及\"江汉平原\"，列出：荆州、荆门、仙桃、潜江、天门、监利\n" +
                        "8. 如果涉及\"大别山\"，列出：黄冈、红安、麻城、罗田、英山\n" +
                        "9. 如果涉及\"武陵山\"，列出：恩施、利川、建始、巴东\n" +
                        "10. 如果涉及\"森林\"、\"林业\"，重点关注：神农架、恩施、宜昌、十堰\n" +
                        "11. 如果涉及\"矿产\"、\"采矿\"，重点关注：黄石、大冶、鄂州、随州\n" +
                        "12. 如果涉及\"水资源\"、\"湖泊\"，重点关注：武汉、洪湖、监利、仙桃\n" +
                        "13. 同义词扩展：湖北→鄂、武汉→江城、宜昌→三峡、荆州→古城\n\n" +
                        "输出要求：\n" +
                        "- 只输出与原查询相关的扩展关键词\n" +
                        "- 优先湖北省内地名\n" +
                        "- 包含可能的同义词和别名\n" +
                        "- 用逗号分隔，最多15个\n\n" +
                        "扩展关键词：",
                userInput, keywordStr
        );
    }

    /**
     * 合并和优化关键词
     */
    private List<String> mergeAndOptimizeKeywords(List<String> basicKeywords, List<String> expandedKeywords) {
        Set<String> keywordSet = new LinkedHashSet<>();

        // 添加基础关键词（优先级最高）
        keywordSet.addAll(basicKeywords);

        // 添加扩展关键词
        keywordSet.addAll(expandedKeywords);

        // 清理和标准化
        List<String> cleanedKeywords = keywordSet.stream()
                .map(this::cleanKeyword)
                .filter(keyword -> keyword.length() >= 2 && keyword.length() <= 10) // 过滤长度
                .filter(this::isValidGeoKeyword) // 过滤无效词汇
                .limit(20) // 限制总数
                .collect(Collectors.toList());

        return cleanedKeywords;
    }

    /**
     * 清理关键词
     */
    private String cleanKeyword(String keyword) {
        if (keyword == null) return "";

        return keyword.trim()
                .replaceAll("[\"\"''()（）\\[\\]【】]", "") // 移除引号和括号
                .replaceAll("\\s+", "") // 移除空格
                .replaceAll("[的地得]$", ""); // 移除词尾助词
    }

    /**
     * 验证是否为有效的地理关键词
     */
    private boolean isValidGeoKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }

        // 过滤无意义词汇
        Set<String> stopWords = Set.of(
                "情况", "状况", "分布", "覆盖", "资源", "信息", "数据", "查看", "了解",
                "显示", "地图", "图幅", "方面", "相关", "有关", "关于", "问题", "怎么样"
        );

        return !stopWords.contains(keyword);
    }

    /**
     * 降级处理：简单关键词提取
     */
    private List<String> fallbackKeywordExtraction(String userInput) {
        log.info("使用降级关键词提取");

        // 简单的地名和关键词提取
        List<String> fallbackKeywords = new ArrayList<>();

        // 常见地名匹配
        String[] commonPlaces = {
                "武汉", "宜昌", "襄阳", "荆州", "黄石", "十堰", "孝感", "荆门", "鄂州", "黄冈",
                "咸宁", "随州", "恩施", "仙桃", "潜江", "天门", "神农架", "长江", "汉江", "洞庭湖"
        };

        for (String place : commonPlaces) {
            if (userInput.contains(place)) {
                fallbackKeywords.add(place);
            }
        }

        // 关键资源词汇
        String[] resourceWords = {"森林", "矿产", "水资源", "土地", "湿地", "湖泊", "山脉"};
        for (String resource : resourceWords) {
            if (userInput.contains(resource)) {
                fallbackKeywords.add(resource);
            }
        }

        // 如果没有匹配到，使用分词
        if (fallbackKeywords.isEmpty()) {
            fallbackKeywords.addAll(Arrays.asList(userInput.trim().split("\\s+")));
        }

        return fallbackKeywords.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * 调用阿里云百炼API
     */
    private String callLLM(String prompt) {
        try {
            // 构建请求体
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
            parameters.put("temperature", 0.2); // 稍微提高创造性
            parameters.put("max_tokens", 200); // 增加token数量以支持更多扩展
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

            // 兼容性：支持choices格式
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

    /**
     * 从LLM响应中解析关键词
     */
    private List<String> parseKeywordsFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 清理响应内容
        String cleanResponse = response.trim();

        // 移除可能的前缀
        if (cleanResponse.startsWith("关键词：")) {
            cleanResponse = cleanResponse.substring(4);
        }
        if (cleanResponse.startsWith("扩展关键词：")) {
            cleanResponse = cleanResponse.substring(6);
        }

        // 按逗号或顿号分割
        String[] keywords = cleanResponse.split("[,，、]");
        List<String> result = new ArrayList<>();

        for (String keyword : keywords) {
            String cleanKeyword = cleanKeyword(keyword);
            if (!cleanKeyword.isEmpty() && isValidGeoKeyword(cleanKeyword)) {
                result.add(cleanKeyword);
            }
        }

        return result;
    }
}