package com.aicontentgenerator.infrastructure.ratelimit.filter;

import com.aicontentgenerator.auth.service.JwtService;
import com.aicontentgenerator.infrastructure.ratelimit.dto.RateLimitResponse;
import com.aicontentgenerator.infrastructure.ratelimit.factory.BucketType;
import com.aicontentgenerator.infrastructure.ratelimit.service.RateLimitDecision;
import com.aicontentgenerator.infrastructure.ratelimit.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate limiting filter — the first line of defence.
 *
 * Runs BEFORE JwtAuthFilter in the Spring Security chain so that:
 *  - AI endpoints are protected before any expensive processing starts
 *  - Malformed or repeated login attempts are blocked early
 *  - Unauthenticated spam is stopped before JWT parsing overhead
 *
 * Client key resolution:
 *  - Bearer token present + valid JWT  → "user:<email>"   (user-based limit)
 *  - No token or invalid JWT           → "ip:<address>"   (IP-based limit)
 *
 * On ALLOW: sets X-RateLimit-* informational headers and continues chain.
 * On DENY:  writes HTTP 429 JSON body + Retry-After header and stops chain.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String HEADER_LIMIT       = "X-RateLimit-Limit";
    private static final String HEADER_REMAINING   = "X-RateLimit-Remaining";
    private static final String HEADER_RESET       = "X-RateLimit-Reset";
    private static final String HEADER_RETRY_AFTER = "Retry-After";
    private static final String BEARER_PREFIX      = "Bearer ";

    private final RateLimitService rateLimitService;
    private final JwtService       jwtService;
    private final ObjectMapper     objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        String    clientKey  = resolveClientKey(request);
        BucketType bucketType = resolveBucketType(request.getRequestURI());

        RateLimitDecision decision = rateLimitService.tryConsume(clientKey, bucketType);

        setRateLimitHeaders(response, decision);

        if (!decision.isAllowed()) {
            writeRateLimitExceededResponse(response, decision.getRetryAfterSeconds());
            return;
        }

        filterChain.doFilter(request, response);
    }

    // ── Client key ────────────────────────────────────────────────────────────

    /**
     * Extracts a unique client identifier.
     * Tries to use the JWT subject (email) for authenticated requests;
     * falls back to the client IP for anonymous ones.
     *
     * NOTE: JWT parsing here is lightweight — just extracting the subject claim.
     * Full signature verification + SecurityContext population happens later in JwtAuthFilter.
     */
    private String resolveClientKey(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                String username = jwtService.extractUsername(token);
                if (username != null && !username.isBlank()) {
                    return "user:" + username;
                }
            } catch (Exception ignored) {
                // Invalid token — fall through to IP-based limit
            }
        }
        return "ip:" + getClientIp(request);
    }

    /**
     * Respects X-Forwarded-For for clients behind a proxy or load balancer.
     * Always takes the first (leftmost) IP — the original client address.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // ── Endpoint tier ─────────────────────────────────────────────────────────

    private BucketType resolveBucketType(String uri) {
        if (uri.startsWith("/api/v1/auth")) return BucketType.AUTH;
        if (uri.startsWith("/api/v1/ai"))   return BucketType.AI;
        return BucketType.GENERAL;
    }

    // ── Response handling ─────────────────────────────────────────────────────

    private void setRateLimitHeaders(HttpServletResponse response, RateLimitDecision decision) {
        response.setHeader(HEADER_LIMIT,     String.valueOf(decision.getCapacity()));
        response.setHeader(HEADER_REMAINING, String.valueOf(decision.getRemainingTokens()));
        response.setHeader(HEADER_RESET,     String.valueOf(decision.getRetryAfterSeconds()));
    }

    private void writeRateLimitExceededResponse(
            HttpServletResponse response,
            long retryAfterSeconds
    ) throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HEADER_RETRY_AFTER, String.valueOf(retryAfterSeconds));

        RateLimitResponse body = RateLimitResponse.builder()
                .status(429)
                .error("Too Many Requests")
                .message("Rate limit exceeded — please wait before retrying")
                .retryAfterSeconds(retryAfterSeconds)
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }
}
