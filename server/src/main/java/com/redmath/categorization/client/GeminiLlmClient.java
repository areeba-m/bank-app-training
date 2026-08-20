package com.redmath.categorization.client;

import com.redmath.categorization.client.dto.LlmCategorizationRequest;
import com.redmath.categorization.client.dto.LlmCategorizationResponse;
import com.redmath.categorization.entity.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@EnableConfigurationProperties(LlmProperties.class)
public class GeminiLlmClient implements LlmClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiLlmClient.class);

    private final ChatClient chatClient;
    private final LlmProperties properties;

    public GeminiLlmClient(
            ChatClient chatClient,
            LlmProperties properties
    ) {
        this.chatClient = chatClient;
        this.properties = properties;
    }

    @Override
    public Optional<LlmCategorizationResponse> categorize(
            LlmCategorizationRequest request
    ) {

        if (!properties.enabled() || isBlank(properties.apiKey())) {
            return Optional.empty();
        }

        List<String> allowedCategoryNames = Stream.of(Category.values())
                .map(Enum::name)
                .toList();

        String prompt = """
                You are a bank transaction categorizer.%n\
                Allowed categories:%n\
                %s%n\
                %n\
                Analyze the following transaction.%n\
                Description: %s%n\
                Amount: %s%n\
                Type: %s%n\
                Date: %s%n\
                %n\
                Return ONLY valid JSON in exactly this format:%n\
                {%n\
                  "category": "CATEGORY_NAME",%n\
                  "confidence": 0.0%n\
                }%n\
                %n\
                Rules:%n\
                - category must be one of the allowed categories.%n\
                - confidence must be between 0 and 1.%n\
                - If the description is too generic, numeric, or reference-like,%n\
                  return UNCATEGORIZED.%n\
                - Do not add markdown.%n\
                - Do not add explanations.%n\
                """
                .formatted(
                        String.join(", ", allowedCategoryNames),
                        request.description(),
                        request.amount(),
                        request.type(),
                        request.date()
                );

        try {

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            LOGGER.info("Gemini categorization response: {}", response);

            return parseCategorizationResponse(response);

        } catch (RuntimeException ex) {

            LOGGER.error(
                    "Gemini categorization call failed",
                    ex
            );

            return Optional.empty();
        }
    }

    @Override
    public Optional<String> explainSpending(
            Map<String, Object> currentPeriod,
            Map<String, Object> previousPeriod
    ) {

        if (!properties.enabled() || isBlank(properties.apiKey())) {
            return Optional.empty();
        }

        String prompt = """
                You are a helpful personal finance assistant.%n\
                %n\
                You will receive already-calculated category spending totals%n\
                 for the current period and previous period.%n\
                %n\
                Write a short 2-4 sentence summary.%n\
                %n\
                Rules:%n\
                - Describe notable changes.%n\
                - Mention the largest spending category.%n\
                - Do not invent numbers.%n\
                - Only use the data provided.%n\
                %n\
                Current period totals:%n\
                %s%n\
                %n\
                Previous period totals:%n\
                %s%n\
                """
                .formatted(currentPeriod, previousPeriod);

        try {

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(response.trim());

        } catch (RuntimeException ex) {

            LOGGER.error(
                    "Gemini spending insight call failed",
                    ex
            );

            return Optional.empty();
        }
    }

    private Optional<LlmCategorizationResponse>
    parseCategorizationResponse(String response) {

        if (response == null || response.isBlank()) {
            return Optional.empty();
        }

        try {

            String cleanedResponse = response
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            String categoryValue = extractJsonValue(
                    cleanedResponse,
                    "category"
            );

            String confidenceValue = extractJsonValue(
                    cleanedResponse,
                    "confidence"
            );

            if (categoryValue == null || confidenceValue == null) {
                LOGGER.warn(
                        "Invalid categorization response: {}",
                        response
                );

                return Optional.empty();
            }

            Category category = Category.valueOf(
                    categoryValue
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

            double confidence = Double.parseDouble(
                    confidenceValue
            );

            return Optional.of(
                    new LlmCategorizationResponse(
                            category.name(),
                            confidence
                    )
            );

        } catch (Exception ex) {

            LOGGER.warn(
                    "Failed to parse Gemini response: {}",
                    response,
                    ex
            );

            return Optional.empty();
        }
    }

    private String extractJsonValue(
            String json,
            String field
    ) {

        String regex;

        if ("category".equals(field)) {
            regex = "\"category\"\\s*:\\s*\"([^\"]+)\"";
        } else {
            regex = "\"confidence\"\\s*:\\s*([0-9.]+)";
        }

        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile(regex);

        java.util.regex.Matcher matcher =
                pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
