package com.aicontentgenerator.ai.controller;

import com.aicontentgenerator.ai.dto.GenerateRequest;
import com.aicontentgenerator.ai.dto.GenerateResponse;
import com.aicontentgenerator.ai.service.AiService;
import com.aicontentgenerator.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI REST controller.
 *
 * Secured — requires a valid JWT (enforced by SecurityConfig).
 *
 * POST /api/v1/ai/generate
 *   Body: { "prompt": "Write a blog post about..." }
 *   Returns: { "prompt": "...", "result": "...", "generatedAt": "..." }
 *
 * Note: This endpoint generates content only — it does NOT persist it.
 * To save generated content, use the Content module's save endpoint.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GenerateResponse>> generate(
            @Valid @RequestBody GenerateRequest request) {

        GenerateResponse response = aiService.generate(request);
        return ResponseEntity.ok(ApiResponse.success("Content generated successfully", response));
    }
}
