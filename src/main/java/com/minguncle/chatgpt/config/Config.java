package com.minguncle.chatgpt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Value("${openai.apiKey}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }

}
