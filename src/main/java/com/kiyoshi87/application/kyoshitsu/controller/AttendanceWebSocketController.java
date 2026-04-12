package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.model.request.AttendanceMarkRequest;
import com.kiyoshi87.application.kyoshitsu.service.ClassSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AttendanceWebSocketController {

    private final ClassSessionService classSessionService;

    @PreAuthorize("hasRole('STUDENT')")
    @MessageMapping("/attendance.mark")
    public void markAttendance(AttendanceMarkRequest request, Authentication authentication) {
        classSessionService.markAttendance(request.getClassId(), authentication);
    }
}
