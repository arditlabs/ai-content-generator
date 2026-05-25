package com.aicontentgenerator.ai.client;

import com.aicontentgenerator.ai.client.dto.OpenAiRequest;
import com.aicontentgenerator.ai.client.dto.OpenAiRequest.Message;
import com.aicontentgenerator.ai.client.dto.OpenAiResponse;
import com.aicontentgenerator.ai.config.AiProperties;
import com.aicontentgenerator.common.exception.AppException;
import com.aicontentgenerator.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Real OpenAI provider — active when app.ai.provider=openai.
 *
 * Uses Spring 6 RestClient (synchronous, replaces RestTemplate cleanly).
 * Targets the Chat Completions API with the configured model.
 *
 * Error handling:
 *  - 4xx → likely bad API key or malformed request → AppException BAD_GATEWAY
 *  - 5xx → OpenAI outage → AppException BAD_GATEWAY
 *  - empty choices → AppException AI_EMPTY_RESPONSE
 */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
@Slf4j
public class OpenAiProviderClient implements AiProviderClient {

    private final RestClient     restClient;
    private final AiProperties   properties;

    public OpenAiProviderClient(
            @Qualifier("openAiRestClient") RestClient restClient,
            AiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public String generate(String prompt) {
        AiProperties.OpenAi config = properties.getOpenai();

        OpenAiRequest request = new OpenAiRequest(
                config.getModel(),
                List.of(
                        Message.system(config.getSystemPrompt()),
                        Message.user(prompt)
                ),
                config.getMaxTokens()
        );

        log.info("Sending request to OpenAI. Model: {}, Prompt length: {} chars",
                config.getModel(), prompt.length());

        OpenAiResponse response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.error("OpenAI client error: {} {}", res.getStatusCode(), res.getStatusText());
                    throw new AppException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_PROVIDER_ERROR);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.error("OpenAI server error: {} {}", res.getStatusCode(), res.getStatusText());
                    throw new AppException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_PROVIDER_ERROR);
                })
                .body(OpenAiResponse.class);

        if (response == null || response.firstChoiceContent() == null) {
            log.error("OpenAI returned null or empty response");
            throw new AppException(HttpStatus.BAD_GATEWAY, ErrorCode.AI_EMPTY_RESPONSE);
        }

        String result = response.firstChoiceContent();
        log.info("OpenAI response received. Length: {} chars", result.length());
        return result;
    }
}
