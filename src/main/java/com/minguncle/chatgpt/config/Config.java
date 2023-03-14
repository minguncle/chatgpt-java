package com.minguncle.chatgpt.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class Config {

    @Value("${openai.apiKey}")
    private String apiKey;
    @Value("${openai.proxy-host}")
    private String proxyHost;
    @Value("${openai.proxy-port}")
    private String proxyPort;

}
