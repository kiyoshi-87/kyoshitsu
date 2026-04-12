package com.kiyoshi87.application.kyoshitsu.service;

import com.kiyoshi87.application.kyoshitsu.exceptions.ApiException;
import com.kiyoshi87.application.kyoshitsu.model.ApiResponse;
import com.kiyoshi87.application.kyoshitsu.model.entity.AttendanceRecord;
import com.kiyoshi87.application.kyoshitsu.model.entity.ClassSession;
import com.kiyoshi87.application.kyoshitsu.model.entity.UserEntity;
import com.kiyoshi87.application.kyoshitsu.model.response.AttendanceMarkedResponse;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassSessionResponse;
import com.kiyoshi87.application.kyoshitsu.repository.AttendanceRecordRepository;
import com.kiyoshi87.application.kyoshitsu.repository.ClassSessionRepository;
import com.kiyoshi87.application.kyoshitsu.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.kiyoshi87.application.kyoshitsu.service.ClassroomService.fetchUser;

@Service
@RequiredArgsConstructor
public class ClassSessionService {

    private final ClassroomRepository classroomRepository;
    private final ClassSessionRepository classSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public ApiResponse<ClassSessionResponse> startSession(String classId, Authentication authentication) {
        UserEntity teacher = fetchUser(authentication);

        // Check if the classId is valid and the teacher has permission to start the session
        validateClassAndPermission(classId, teacher);

        boolean isSessionActive = classSessionRepository.existsByClassIdAndActiveTrue(classId);

        if (isSessionActive) {
            throw new ApiException("A session for this class is already active. Please end the current session before starting a new one");
        }

        ClassSession sessionEntity = buildAndSaveSessionEntity(classId, false);

        // TODO: Start websocket session right here ----

        return ApiResponse.success(ClassSessionResponse.builder()
                .classId(classId)
                .startTime(sessionEntity.getStartTime())
                .endTime(sessionEntity.getEndTime())
                .build());
    }

    public ApiResponse<AttendanceMarkedResponse> markAttendance(String classId, Authentication authentication) {
        UserEntity student = fetchUser(authentication);
        validateAndSaveAttendance(classId, student);

        AttendanceMarkedResponse response = AttendanceMarkedResponse.builder()
                .classId(classId)
                .build();

        return ApiResponse.success(response);
    }

    public ApiResponse<ClassSessionResponse> endSession(String classId, Authentication authentication) {
        UserEntity teacher = fetchUser(authentication);

        // Check if the classId is valid and the teacher has permission to end the session
        validateClassAndPermission(classId, teacher);
        validateSessionIsActive(classId);

        // TODO: Stop websocket session right here ----

        ClassSession sessionEntity = buildAndSaveSessionEntity(classId, true);

        return ApiResponse.success(ClassSessionResponse.builder()
                .classId(classId)
                .startTime(sessionEntity.getStartTime())
                .endTime(sessionEntity.getEndTime())
                .build());
    }

    private void validateClassAndPermission(String classId, UserEntity teacher) {
        boolean isValidClass = classroomRepository.existsByIdAndTeacherId(classId, teacher.getId());

        if (!isValidClass) {
            throw new ApiException("Invalid class ID or you do not have permission to start or end the session");
        }
    }

    private ClassSession buildAndSaveSessionEntity(String classId, boolean isEnd) {
        ClassSession sessionEntity;

        if (!isEnd) {
            sessionEntity = ClassSession.builder()
                    .classId(classId)
                    .startTime(Instant.now()) // Set start time when the session actually starts
                    .active(true)
                    .endTime(null) // Set end time when the session ends
                    .build();
        } else {
            sessionEntity = classSessionRepository
                    .findByClassIdAndActiveTrue(classId)
                    .orElseThrow(() -> new ApiException("Class session is not active. Cannot end session"));

            sessionEntity.setActive(false);
            sessionEntity.setEndTime(Instant.now()); // Set end time when the session ends
        }
        return classSessionRepository.save(sessionEntity);
    }

    private void validateAndSaveAttendance(String classId, UserEntity student) {
        ClassSession classSession = classSessionRepository
                .findByClassIdAndActiveTrue(classId)
                .orElse(null);

        if (classSession == null) {
            throw new ApiException("Class is not active. Attendance not marked");
        }

        AttendanceRecord attendanceRecord = AttendanceRecord.builder()
                .classId(classId)
                .sessionId(classSession.getId())
                .studentId(student.getId())
                .timestamp(Instant.now())
                .build();

        attendanceRecordRepository.save(attendanceRecord);
    }

    private void validateSessionIsActive(String classId) {
        if (!classSessionRepository.existsByClassIdAndActiveTrue(classId)) {
            throw new ApiException("Class session is not active. Cannot end session");
        }
    }
}
