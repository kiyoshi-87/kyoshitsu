package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.config.OpenApiConfig;
import com.kiyoshi87.application.kyoshitsu.model.ApiResponseEntity;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassSessionResponse;
import com.kiyoshi87.application.kyoshitsu.service.ClassSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance Sessions", description = "Start and end class attendance sessions")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AttendanceSessionController {

    private final ClassSessionService service;

    @Operation(summary = "Start an attendance session")
    @ApiResponse(
            responseCode = "200",
            description = "Attendance session started",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    @ApiResponse(responseCode = "403", description = "Teacher role required")
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/start")
    public ApiResponseEntity<ClassSessionResponse> startSession(
            @Parameter(description = "Class id for the attendance session", example = "6618ff2e36e79f0fb1ddbb20")
            @RequestParam String classId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return service.startSession(classId, authentication);
    }

    @Operation(summary = "End an attendance session")
    @ApiResponse(
            responseCode = "200",
            description = "Attendance session ended",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    @ApiResponse(responseCode = "403", description = "Teacher role required")
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/end")
    public ApiResponseEntity<ClassSessionResponse> endSession(
            @Parameter(description = "Class id for the attendance session", example = "6618ff2e36e79f0fb1ddbb20")
            @RequestParam String classId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return service.endSession(classId, authentication);
    }
}
