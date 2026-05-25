package com.aicontentgenerator.scheduler.controller;

import com.aicontentgenerator.common.response.ApiResponse;
import com.aicontentgenerator.scheduler.dto.ScheduleRequest;
import com.aicontentgenerator.scheduler.dto.ScheduledJobResponse;
import com.aicontentgenerator.scheduler.service.SchedulerService;
import com.aicontentgenerator.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Scheduler REST controller.
 *
 * All endpoints are secured — JWT required.
 *
 * POST   /api/v1/scheduler      → create a scheduled job
 * GET    /api/v1/scheduler      → list all jobs for current user
 * GET    /api/v1/scheduler/{id} → get a single job
 * DELETE /api/v1/scheduler/{id} → cancel a PENDING job
 */
@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduledJobResponse>> schedule(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ScheduleRequest request) {

        ScheduledJobResponse response = schedulerService.schedule(currentUser, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job scheduled successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduledJobResponse>>> getAll(
            @AuthenticationPrincipal User currentUser) {

        List<ScheduledJobResponse> jobs = schedulerService.getAllByUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduledJobResponse>> getById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {

        ScheduledJobResponse response = schedulerService.getByIdAndUser(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {

        schedulerService.cancel(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Job cancelled successfully"));
    }
}
