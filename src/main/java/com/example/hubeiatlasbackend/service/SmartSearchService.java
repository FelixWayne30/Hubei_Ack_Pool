package com.example.hubeiatlasbackend.service;

import com.example.hubeiatlasbackend.mapper.SubmapsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SmartSearchService {

    @Resource
    private LLMService llmService;

    @Resource
    private SubmapsMapper submapsMapper;

    /**
     * AI智能搜索（唯一搜索入口）
     * @param query 用户搜索查询
     * @param page 页码
     * @param size 每页大小
     * @return AI搜索结果
     */
    public Map<String, Object> aiSearch(String query, int page, int size) {
        try {
            log.info("开始AI智能搜索: query={}, page={}, size={}", query, page, size);

            Map<String, Object> result = new HashMap<>();

            // AI智能匹配搜索
            List<LLMService.SmartSearchResult> aiResults = llmService.intelligentMapMatching(query);
            log.info("AI匹配返回{}个结果", aiResults.size());

            // 转换AI结果为统一格式
            List<Map<String, Object>> searchResults = new ArrayList<>();
            for (LLMService.SmartSearchResult aiResult : aiResults) {
                Map<String, Object> item = new HashMap<>();
                item.put("map_id", aiResult.getMapId());
                item.put("title", aiResult.getMapTitle());
                item.put("description", "智能匹配：" + aiResult.getSubitemName());
                item.put("type", aiResult.getSubitemType());
                item.put("relevance_score", aiResult.getScore());
                item.put("subitem_name", aiResult.getSubitemName());
                searchResults.add(item);
            }

            // 分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, searchResults.size());
            List<Map<String, Object>> pagedResults = new ArrayList<>();

            if (start < searchResults.size()) {
                pagedResults = searchResults.subList(start, end);
            }

            // 构建返回结果
            result.put("query", query);
            result.put("total", searchResults.size());
            result.put("page", page);
            result.put("size", size);
            result.put("results", pagedResults);

            log.info("AI智能搜索完成，返回{}个结果", pagedResults.size());
            return result;

        } catch (Exception e) {
            log.error("AI智能搜索失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI智能搜索失败", e);
        }
    }

    /**
     * 获取搜索建议（基于子项名称）
     */
    public List<String> getSearchSuggestions(String query, int limit) {
        try {
            String searchQuery = "%" + query + "%";
            List<Map<String, Object>> subitemResults = submapsMapper.searchSubitemsByName(searchQuery, limit);
            return subitemResults.stream()
                    .map(item -> item.get("subitem_name").toString())
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取搜索建议失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}