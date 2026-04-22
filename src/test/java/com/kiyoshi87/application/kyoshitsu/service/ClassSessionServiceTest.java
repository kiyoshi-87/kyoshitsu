package com.kiyoshi87.application.kyoshitsu.service;

import com.kiyoshi87.application.kyoshitsu.event.AttendanceEventPublisher;
import com.kiyoshi87.application.kyoshitsu.exceptions.ApiException;
import com.kiyoshi87.application.kyoshitsu.model.ApiResponseEntity;
import com.kiyoshi87.application.kyoshitsu.model.Role;
import com.kiyoshi87.application.kyoshitsu.model.auth.CustomUserDetails;
import com.kiyoshi87.application.kyoshitsu.model.entity.AttendanceRecord;
import com.kiyoshi87.application.kyoshitsu.model.entity.ClassSession;
import com.kiyoshi87.application.kyoshitsu.model.entity.UserEntity;
import com.kiyoshi87.application.kyoshitsu.model.response.AttendanceMarkedResponse;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassSessionResponse;
import com.kiyoshi87.application.kyoshitsu.repository.AttendanceRecordRepository;
import com.kiyoshi87.application.kyoshitsu.repository.ClassSessionRepository;
import com.kiyoshi87.application.kyoshitsu.repository.ClassroomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassSessionServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private ClassSessionRepository classSessionRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private AttendanceEventPublisher eventPublisher;

    @InjectMocks
    private ClassSessionService classSessionService;

    @Test
    void startSessionShouldCreateActiveSessionWhenTeacherOwnsClass() {
        UserEntity teacher = user("teacher-1", Role.TEACHER);

        when(classroomRepository.existsByIdAndTeacherId("class-1", "teacher-1")).thenReturn(true);
        when(classSessionRepository.existsByClassIdAndActiveTrue("class-1")).thenReturn(false);
        when(classSessionRepository.save(any(ClassSession.class))).thenAnswer(invocation -> {
            ClassSession session = invocation.getArgument(0);
            session.setId("session-1");
            return session;
        });

        ApiResponseEntity<ClassSessionResponse> response = classSessionService.startSession("class-1", authenticationFor(teacher));

        assertTrue(response.isSuccess());
        assertEquals("class-1", response.getData().getClassId());
        assertNotNull(response.getData().getStartTime());
        assertNull(response.getData().getEndTime());

        ArgumentCaptor<ClassSession> captor = ArgumentCaptor.forClass(ClassSession.class);
        verify(classSessionRepository).save(captor.capture());
        assertEquals("class-1", captor.getValue().getClassId());
        assertTrue(captor.getValue().isActive());
        assertNotNull(captor.getValue().getStartTime());
        assertNull(captor.getValue().getEndTime());
        verify(eventPublisher).publishSessionStarted("class-1", "session-1");
    }

    @Test
    void startSessionShouldThrowWhenTeacherDoesNotOwnClass() {
        when(classroomRepository.existsByIdAndTeacherId("class-1", "teacher-1")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> classSessionService.startSession("class-1", authenticationFor(user("teacher-1", Role.TEACHER))));

        assertEquals("Invalid class ID or you do not have permission to start or end the session", exception.getMessage());
        verify(classSessionRepository, never()).save(any(ClassSession.class));
        verify(eventPublisher, never()).publishSessionStarted(any(), any());
    }

    @Test
    void startSessionShouldThrowWhenSessionAlreadyActive() {
        when(classroomRepository.existsByIdAndTeacherId("class-1", "teacher-1")).thenReturn(true);
        when(classSessionRepository.existsByClassIdAndActiveTrue("class-1")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> classSessionService.startSession("class-1", authenticationFor(user("teacher-1", Role.TEACHER))));

        assertEquals("A session for this class is already active. Please end the current session before starting a new one", exception.getMessage());
        verify(classSessionRepository, never()).save(any(ClassSession.class));
        verify(eventPublisher, never()).publishSessionStarted(any(), any());
    }

    @Test
    void markAttendanceShouldSaveAttendanceForActiveSession() {
        UserEntity student = user("student-1", Role.STUDENT);
        ClassSession session = ClassSession.builder()
                .id("session-1")
                .classId("class-1")
                .active(true)
                .build();

        when(classSessionRepository.findByClassIdAndActiveTrue("class-1")).thenReturn(Optional.of(session));
        when(attendanceRecordRepository.existsBySessionIdAndStudentId("session-1", "student-1")).thenReturn(false);

        ApiResponseEntity<AttendanceMarkedResponse> response =
                classSessionService.markAttendance("class-1", authenticationFor(student));

        assertTrue(response.isSuccess());
        assertEquals("class-1", response.getData().getClassId());

        ArgumentCaptor<AttendanceRecord> captor = ArgumentCaptor.forClass(AttendanceRecord.class);
        verify(attendanceRecordRepository).save(captor.capture());
        assertEquals("class-1", captor.getValue().getClassId());
        assertEquals("session-1", captor.getValue().getSessionId());
        assertEquals("student-1", captor.getValue().getStudentId());
        assertNotNull(captor.getValue().getTimestamp());
        verify(eventPublisher).publishAttendanceMarked("class-1", "session-1", "student-1");
    }

    @Test
    void markAttendanceShouldThrowWhenNoSessionIsActive() {
        when(classSessionRepository.findByClassIdAndActiveTrue("class-1")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> classSessionService.markAttendance("class-1", authenticationFor(user("student-1", Role.STUDENT))));

        assertEquals("Class is not active. Attendance not marked", exception.getMessage());
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
        verify(eventPublisher, never()).publishAttendanceMarked(any(), any(), any());
    }

    @Test
    void markAttendanceShouldThrowWhenAttendanceAlreadyMarked() {
        UserEntity student = user("student-1", Role.STUDENT);
        ClassSession session = ClassSession.builder()
                .id("session-1")
                .classId("class-1")
                .active(true)
                .build();

        when(classSessionRepository.findByClassIdAndActiveTrue("class-1")).thenReturn(Optional.of(session));
        when(attendanceRecordRepository.existsBySessionIdAndStudentId("session-1", "student-1")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> classSessionService.markAttendance("class-1", authenticationFor(student)));

        assertEquals("Attendance already marked for this session", exception.getMessage());
        verify(attendanceRecordRepository, never()).save(any(AttendanceRecord.class));
        verify(eventPublisher, never()).publishAttendanceMarked(any(), any(), any());
    }

    @Test
    void endSessionShouldDeactivateActiveSessionAndSetEndTime() {
        UserEntity teacher = user("teacher-1", Role.TEACHER);
        ClassSession activeSession = ClassSession.builder()
                .id("session-1")
                .classId("class-1")
                .active(true)
                .build();

        when(classroomRepository.existsByIdAndTeacherId("class-1", "teacher-1")).thenReturn(true);
        when(classSessionRepository.existsByClassIdAndActiveTrue("class-1")).thenReturn(true);
        when(classSessionRepository.findByClassIdAndActiveTrue("class-1")).thenReturn(Optional.of(activeSession));
        when(classSessionRepository.save(any(ClassSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponseEntity<ClassSessionResponse> response = classSessionService.endSession("class-1", authenticationFor(teacher));

        assertTrue(response.isSuccess());
        assertEquals("class-1", response.getData().getClassId());
        assertNotNull(response.getData().getEndTime());

        ArgumentCaptor<ClassSession> captor = ArgumentCaptor.forClass(ClassSession.class);
        verify(classSessionRepository).save(captor.capture());
        assertFalse(captor.getValue().isActive());
        assertNotNull(captor.getValue().getEndTime());
        verify(eventPublisher).publishSessionEnded("class-1", "session-1");
    }

    @Test
    void endSessionShouldThrowWhenNoSessionIsActive() {
        when(classroomRepository.existsByIdAndTeacherId("class-1", "teacher-1")).thenReturn(true);
        when(classSessionRepository.existsByClassIdAndActiveTrue("class-1")).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> classSessionService.endSession("class-1", authenticationFor(user("teacher-1", Role.TEACHER))));

        assertEquals("Class session is not active. Cannot end session", exception.getMessage());
        verify(classSessionRepository, never()).save(any(ClassSession.class));
        verify(eventPublisher, never()).publishSessionEnded(any(), any());
    }

    private static Authentication authenticationFor(UserEntity user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(user));
        return authentication;
    }

    private static UserEntity user(String id, Role role) {
        return UserEntity.builder()
                .id(id)
                .name(id)
                .email(id + "@example.com")
                .password("secret")
                .role(role)
                .build();
    }
}
