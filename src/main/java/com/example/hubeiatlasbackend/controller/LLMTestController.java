package com.example.hubeiatlasbackend.controller;

import com.example.hubeiatlasbackend.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/test")
public class LLMTestController extends BaseController {

    @Autowired
    private LLMService llmService;

    @PostMapping("/llm/keywords")
    public Object testKeywordExtraction(@RequestParam("input") String input) {
        try {
            List<String> keywords = llmService.extractGeoKeywords(input);
            return renderSuccess("关键词提取成功", keywords);
        } catch (Exception e) {
            return renderError("关键词提取失败: " + e.getMessage());
        }
    }
}