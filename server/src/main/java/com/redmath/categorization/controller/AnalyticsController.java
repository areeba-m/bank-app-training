package com.redmath.categorization.controller;

import com.redmath.categorization.dto.SpendingAnalysisResponse;
import com.redmath.categorization.service.SpendingAnalysisService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/user/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final SpendingAnalysisService spendingAnalysisService;

    /**
     * GET /api/v1/user/analytics/spending
     * GET /api/v1/user/analytics/spending?from=2026-08-01&to=2026-08-31
     * GET /api/v1/user/analytics/spending?insight=true*
     * With no "from"/"to" supplied, defaults to the current calendar month.
     * "to" is treated as inclusive of that whole day.
     */
    @GetMapping("/spending")
    public ResponseEntity<SpendingAnalysisResponse> getSpendingAnalysis(
            @NonNull Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "false") boolean insight) {

        Long userId = ((Jwt) Objects.requireNonNull(authentication.getPrincipal())).getClaim("userId");

        if (from == null || to == null) {
            return ResponseEntity.ok(spendingAnalysisService.analyzeCurrentMonth(userId, insight));
        }

        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return ResponseEntity.ok(spendingAnalysisService.analyze(userId, fromInstant, toInstant, insight));
    }
}
