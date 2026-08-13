package com.redmath.categorization.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "categorization.llm")
public record LlmProperties(
        boolean enabled,
        String apiKey,
        String model,
        int timeoutMillis
) {
    public LlmProperties {
        if (timeoutMillis <= 0) {
            timeoutMillis = 60000000;
        }
        if (model == null || model.isBlank()) {
            model = "gemini-3.6-flash";
        }
    }
}
