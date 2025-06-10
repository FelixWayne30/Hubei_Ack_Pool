package com.example.hubeiatlasbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "dashscope")
@Data
public class LLMConfig {
    private String apiKey;
    private String baseUrl;
    private String model;
}