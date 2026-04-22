package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.config.OpenApiConfig;
import com.kiyoshi87.application.kyoshitsu.model.ApiResponseEntity;
import com.kiyoshi87.application.kyoshitsu.model.common.UserDto;
import com.kiyoshi87.application.kyoshitsu.service.ClassroomService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User lookup endpoints")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class GeneralController {

    private final ClassroomService service;

    @Operation(summary = "List all students")
    @ApiResponse(
            responseCode = "200",
            description = "Students returned",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    @ApiResponse(responseCode = "403", description = "Teacher role required")
    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponseEntity<List<UserDto>> getAllStudents(@Parameter(hidden = true) Authentication authentication) {
        return service.getAllStudents(authentication);
    }
}
