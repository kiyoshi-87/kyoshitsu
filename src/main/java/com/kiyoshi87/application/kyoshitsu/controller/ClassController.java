package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.model.ApiResponseEntity;
import com.kiyoshi87.application.kyoshitsu.model.request.AddStudentsRequest;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassDetailResponse;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassResponse;
import com.kiyoshi87.application.kyoshitsu.config.OpenApiConfig;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/class")
@Tag(name = "Classes", description = "Classroom management endpoints")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ClassController {

    private final ClassroomService service;

    @Operation(summary = "Create a class", description = "Creates an empty class owned by the authenticated teacher.")
    @ApiResponse(
            responseCode = "200",
            description = "Class created",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    @ApiResponse(responseCode = "403", description = "Teacher role required")
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponseEntity<ClassResponse> createClass(
            @Parameter(description = "Name of the class to create", example = "Physics 101") @RequestParam String className,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return service.createClassroom(className, authentication);
    }

    @Operation(summary = "Add students to a class")
    @ApiResponse(
            responseCode = "200",
            description = "Students added",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    @ApiResponse(responseCode = "403", description = "Teacher role required")
    @PostMapping("/add-students")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponseEntity<ClassResponse> addStudent(
            @RequestBody AddStudentsRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return service.addStudents(request, authentication);
    }

    @Operation(summary = "Get class details")
    @ApiResponse(
            responseCode = "200",
            description = "Class details",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    @ApiResponse(responseCode = "403", description = "Teacher or student role required")
    @ApiResponse(responseCode = "404", description = "Class not found")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    public ApiResponseEntity<ClassDetailResponse> getClass(
            @Parameter(description = "Class id", example = "6618ff2e36e79f0fb1ddbb20") @PathVariable String id,
            @Parameter(hidden = true) Authentication authentication
    ) {
        return service.getClassroom(id, authentication);
    }
}
