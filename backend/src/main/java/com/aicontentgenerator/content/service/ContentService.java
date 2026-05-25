package com.aicontentgenerator.content.service;

import com.aicontentgenerator.common.exception.AppException;
import com.aicontentgenerator.common.exception.ErrorCode;
import com.aicontentgenerator.content.dto.ContentResponse;
import com.aicontentgenerator.content.dto.SaveContentRequest;
import com.aicontentgenerator.content.entity.Content;
import com.aicontentgenerator.content.repository.ContentRepository;
import com.aicontentgenerator.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Content service — manages all persistence for AI-generated content.
 *
 * Two entry points:
 *  save(User, SaveContentRequest) → called from ContentController (user-initiated save)
 *  save(User, String, String)     → called from SchedulerJobExecutor (internal, programmatic save)
 *
 * Every query is user-scoped to enforce data isolation between users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContentService {

    private final ContentRepository contentRepository;

    // ── Save ─────────────────────────────────────────────────────────────────

    /** API-facing save — called from ContentController. */
    public ContentResponse save(User user, SaveContentRequest request) {
        return save(user, request.getPrompt(), request.getResult());
    }

    /**
     * Internal save — called by SchedulerJobExecutor after AI generation.
     * Kept public so the scheduler module can wire it in without reflection hacks.
     */
    public ContentResponse save(User user, String prompt, String result) {
        Content content = Content.builder()
                .user(user)
                .prompt(prompt)
                .result(result)
                .build();

        Content saved = contentRepository.save(content);
        log.debug("Content [{}] saved for user [{}]", saved.getId(), user.getEmail());
        return ContentResponse.from(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ContentResponse> getAllByUser(User user) {
        return contentRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(ContentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContentResponse getByIdAndUser(Long id, User user) {
        return contentRepository.findByIdAndUser(id, user)
                .map(ContentResponse::from)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.CONTENT_NOT_FOUND));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void delete(Long id, User user) {
        if (!contentRepository.existsByIdAndUser(id, user)) {
            throw new AppException(HttpStatus.NOT_FOUND, ErrorCode.CONTENT_NOT_FOUND);
        }
        contentRepository.deleteByIdAndUser(id, user);
        log.debug("Content [{}] deleted by user [{}]", id, user.getEmail());
    }
}
