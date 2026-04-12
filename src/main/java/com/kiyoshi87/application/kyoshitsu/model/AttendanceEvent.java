package com.kiyoshi87.application.kyoshitsu.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceEvent {

    private AttendanceEventType type;
    private String sessionId;
    private String classId;
    private String studentId;
}
