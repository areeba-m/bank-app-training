package com.redmath.categorization.client;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.redmath.categorization.client.dto.LlmCategorizationRequest;
import com.redmath.categorization.client.dto.LlmCategorizationResponse;
import com.redmath.categorization.entity.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@EnableConfigurationProperties(LlmProperties.class)
public class GeminiLlmClient implements LlmClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiLlmClient.class);

    private final LlmProperties properties;
    private final ObjectMapper objectMapper;

    public GeminiLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private Client buildClient() {
        HttpOptions httpOptions = HttpOptions.builder()
                .timeout(properties.timeoutMillis())
                .build();

        return Client.builder()
                .apiKey(properties.apiKey())
                .httpOptions(httpOptions)
                .build();
    }

    @Override
    public Optional<LlmCategorizationResponse> categorize(LlmCategorizationRequest request) {
        if (!properties.enabled() || isBlank(properties.apiKey())) {
            return Optional.empty();
        }

        List<String> allowedCategoryNames = List.of(Category.values()).stream()
                .map(Enum::name)
                .toList();

        String prompt = "You are a bank transaction categorizer. "
                + "Allowed categories: " + String.join(", ", allowedCategoryNames) + ". "
                + "If the description is too generic, numeric, or reference-like to determine "
                + "a real category (for example a bare transaction ID or reference number), "
                + "respond with category UNCATEGORIZED and a low confidence rather than guessing. "
                + "Transaction - description: " + request.description()
                + ", amount: " + request.amount()
                + ", type: " + request.type()
                + ", date: " + request.date();

        // Response schema is expressed as a plain JSON-shaped Map, exactly as documented
        // in the google-genai README, rather than the typed Schema builder, since the
        // builder's field names differ across SDK versions.

        Schema responseSchema = Schema.builder()
                .type("OBJECT")
                .properties(Map.of(
                        "category", Schema.builder()
                                .type("STRING")
                                .enum_(allowedCategoryNames)
                                .build(),
                        "confidence", Schema.builder()
                                .type("NUMBER")
                                .build()
                ))
                .required(List.of("category", "confidence"))
                .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.0F)
                .candidateCount(1)
                .responseMimeType("application/json")
                .responseSchema(responseSchema)
                .build();

        try (Client client = buildClient()) {
            GenerateContentResponse response = client.models
                    .generateContent(properties.model(), prompt, config);
            return parseCategorizationJson(response.text());
        } catch (RuntimeException ex) {
            LOGGER.warn("Gemini categorization call failed, falling back to UNCATEGORIZED", ex);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> explainSpending(Map<String, Object> currentPeriod, Map<String, Object> previousPeriod) {
        if (!properties.enabled() || isBlank(properties.apiKey())) {
            return Optional.empty();
        }

        String prompt = "You are a helpful personal finance assistant. You will be given "
                + "already-calculated category spending totals for the current and previous period. "
                + "Write a short (2-4 sentence) plain-language summary of the notable changes and the "
                + "largest spending category. Do not invent numbers - only describe the numbers given. "
                + "Current period totals: " + currentPeriod + ". Previous period totals: " + previousPeriod;

        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.3F)
                .candidateCount(1)
                .build();

        try (Client client = buildClient()){
            GenerateContentResponse response = client.models
                    .generateContent(properties.model(), prompt, config);

            String text = response.text();
            return (text == null || text.isBlank()) ? Optional.empty() : Optional.of(text.trim());
        } catch (RuntimeException ex) {
            LOGGER.warn("Gemini spending insight call failed", ex);
            return Optional.empty();
        }
    }

    private Optional<LlmCategorizationResponse> parseCategorizationJson(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode node = objectMapper.readTree(content);
            String categoryText = node.path("category").asText(null);
            double confidence = node.path("confidence").asDouble(0.0);

            if (categoryText == null) {
                return Optional.empty();
            }

            // Validate against the allowed enum - the LLM must not be able to invent categories.
            Category category = Category.valueOf(categoryText.trim().toUpperCase(Locale.ROOT));

            return Optional.of(new LlmCategorizationResponse(category.name(), confidence));
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Gemini returned a category outside the allowed enum or malformed JSON: {}", content);
            return Optional.empty();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
