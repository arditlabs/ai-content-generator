package com.aicontentgenerator.content.controller;

import com.aicontentgenerator.common.response.ApiResponse;
import com.aicontentgenerator.content.dto.ContentResponse;
import com.aicontentgenerator.content.dto.SaveContentRequest;
import com.aicontentgenerator.content.service.ContentService;
import com.aicontentgenerator.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Content REST controller.
 *
 * All endpoints are secured — JWT required.
 * The @AuthenticationPrincipal annotation injects the currently authenticated
 * User directly, avoiding SecurityContextHolder boilerplate in the service layer.
 *
 * POST   /api/v1/content      → save generated content
 * GET    /api/v1/content      → list all content for the current user
 * GET    /api/v1/content/{id} → get a single content item
 * DELETE /api/v1/content/{id} → delete a content item
 */
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping
    public ResponseEntity<ApiResponse<ContentResponse>> save(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SaveContentRequest request) {

        ContentResponse response = contentService.save(currentUser, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Content saved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getAll(
            @AuthenticationPrincipal User currentUser) {

        List<ContentResponse> items = contentService.getAllByUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentResponse>> getById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {

        ContentResponse response = contentService.getByIdAndUser(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {

        contentService.delete(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Content deleted successfully"));
    }
}
