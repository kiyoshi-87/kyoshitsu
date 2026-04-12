package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.model.ApiResponse;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassSessionResponse;
import com.kiyoshi87.application.kyoshitsu.service.ClassSessionService;
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
public class AttendanceSessionController {

    private final ClassSessionService service;

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/start")
    public ApiResponse<ClassSessionResponse> startSession(@RequestParam String classId, Authentication authentication) {
        return service.startSession(classId, authentication);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/end")
    public ApiResponse<ClassSessionResponse> endSession(@RequestParam String classId, Authentication authentication) {
        return service.endSession(classId, authentication);
    }
}
