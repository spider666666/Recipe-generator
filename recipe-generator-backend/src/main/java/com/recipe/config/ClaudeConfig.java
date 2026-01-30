package com.recipe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "claude")
@Data
public class ClaudeConfig {
    private String apiKey;
    private String apiUrl;
    private String model;
    private Integer maxTokens;
    private Double temperature;
    private Integer timeout;  // 超时时间（毫秒）
}
