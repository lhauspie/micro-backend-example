package com.lhauspie.example.microback.spring.consumer.js;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "business.logic.provider")
public record BusinessLogicProviderConfig(String url, Duration cacheTtl) {}
