package com.redmath.categorization.client;

import com.redmath.categorization.client.dto.LlmCategorizationRequest;
import com.redmath.categorization.client.dto.LlmCategorizationResponse;

import java.util.Map;
import java.util.Optional;

public interface LlmClient {

    /**
     * Ask the LLM to classify a single transaction.
     *
     * @return the structured classification, or empty if the LLM could not be reached
     *         or returned something unusable. Callers must treat empty as "fall back
     *         to UNCATEGORIZED" rather than retrying indefinitely.
     */
    Optional<LlmCategorizationResponse> categorize(LlmCategorizationRequest request);

    /**
     * Ask the LLM to explain already-calculated spending totals in plain language.
     * The LLM never computes the totals themselves; it only narrates numbers
     * that Java has already calculated.
     *
     * @return the generated insight text, or empty if the LLM could not be reached
     */
    Optional<String> explainSpending(Map<String, Object> currentPeriod, Map<String, Object> previousPeriod);
}
