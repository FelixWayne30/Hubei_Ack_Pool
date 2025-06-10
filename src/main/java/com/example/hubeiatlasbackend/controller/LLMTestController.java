package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/test")
public class LLMTestController extends BaseController {

    @Autowired
    private LLMService llmService;

    /**
     * 测试智能地图匹配功能
     */
    @PostMapping("/llm/intelligent-matching")
    public Object testIntelligentMapMatching(@RequestParam("input") String input) {
        try {
            List<LLMService.MapMatchResult> matchResults = llmService.intelligentMapMatching(input);

            // 构建详细的返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("query", input);
            response.put("matchCount", matchResults.size());
            response.put("matches", matchResults);

            return renderSuccess("智能地图匹配成功", response);
        } catch (Exception e) {
            return renderError("智能地图匹配失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库中所有子项名称（用于调试）
     */
    @GetMapping("/llm/subitem-names")
    public Object getAllSubitemNames() {
        try {
            List<String> subitemNames = llmService.getAllSubitemNames();

            Map<String, Object> response = new HashMap<>();
            response.put("total", subitemNames.size());
            response.put("subitemNames", subitemNames);

            return renderSuccess("获取子项名称成功", response);
        } catch (Exception e) {
            return renderError("获取子项名称失败: " + e.getMessage());
        }
    }

    /**
     * 根据子项名称获取详细信息（用于调试）
     */
    @GetMapping("/llm/subitem-detail")
    public Object getSubitemDetail(@RequestParam("subitemName") String subitemName) {
        try {
            List<Map<String, Object>> details = llmService.getMapExtendsBySubitemName(subitemName);

            Map<String, Object> response = new HashMap<>();
            response.put("subitemName", subitemName);
            response.put("details", details);

            return renderSuccess("获取子项详情成功", response);
        } catch (Exception e) {
            return renderError("获取子项详情失败: " + e.getMessage());
        }
    }
}