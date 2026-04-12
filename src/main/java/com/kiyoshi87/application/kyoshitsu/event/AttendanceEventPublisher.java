package com.kiyoshi87.application.kyoshitsu.event;

import com.kiyoshi87.application.kyoshitsu.model.AttendanceEvent;
import com.kiyoshi87.application.kyoshitsu.model.AttendanceEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttendanceEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String DESTINATION_PATH = "/topic/attendance/";

    public void publishSessionStarted(String classId, String sessionId) {
        messagingTemplate.convertAndSend(
                DESTINATION_PATH + classId,
                AttendanceEvent.builder()
                        .type(AttendanceEventType.STARTED)
                        .classId(classId)
                        .sessionId(sessionId)
                        .build()
        );
    }

    public void publishSessionEnded(String classId, String sessionId) {
        messagingTemplate.convertAndSend(
                DESTINATION_PATH + classId,
                AttendanceEvent.builder()
                        .type(AttendanceEventType.ENDED)
                        .classId(classId)
                        .sessionId(sessionId)
                        .build()
        );
    }

    public void publishAttendanceMarked(String classId, String sessionId, String studentId) {
        messagingTemplate.convertAndSend(
                DESTINATION_PATH + classId,
                AttendanceEvent.builder()
                        .type(AttendanceEventType.MARKED)
                        .classId(classId)
                        .sessionId(sessionId)
                        .studentId(studentId)
                        .build()
        );
    }
}
