package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.model.ApiResponse;
import com.kiyoshi87.application.kyoshitsu.model.response.AttendanceMarkedResponse;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassSessionResponse;
import com.kiyoshi87.application.kyoshitsu.service.ClassSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AttendanceSessionController {

    private final ClassSessionService service;

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ApiResponse<ClassSessionResponse> startSession(String classId, Authentication authentication) {
        return service.startSession(classId, authentication);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping
    public ApiResponse<AttendanceMarkedResponse> markAttendance(String classId, Authentication authentication) {
        return service.markAttendance(classId, authentication);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ApiResponse<ClassSessionResponse> endSession(String classId, Authentication authentication) {
        return service.endSession(classId, authentication);
    }
}
