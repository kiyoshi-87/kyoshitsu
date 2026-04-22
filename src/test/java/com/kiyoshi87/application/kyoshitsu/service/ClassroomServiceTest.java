package com.kiyoshi87.application.kyoshitsu.service;

import com.kiyoshi87.application.kyoshitsu.exceptions.ApiException;
import com.kiyoshi87.application.kyoshitsu.model.ApiResponseEntity;
import com.kiyoshi87.application.kyoshitsu.model.Role;
import com.kiyoshi87.application.kyoshitsu.model.auth.CustomUserDetails;
import com.kiyoshi87.application.kyoshitsu.model.common.UserDto;
import com.kiyoshi87.application.kyoshitsu.model.entity.ClassEntity;
import com.kiyoshi87.application.kyoshitsu.model.entity.UserEntity;
import com.kiyoshi87.application.kyoshitsu.model.request.AddStudentsRequest;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassDetailResponse;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassResponse;
import com.kiyoshi87.application.kyoshitsu.repository.ClassroomRepository;
import com.kiyoshi87.application.kyoshitsu.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassroomServiceTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClassroomService classroomService;

    @Test
    void createClassroomShouldPersistAndReturnResponseForTeacher() {
        UserEntity teacher = user("teacher-1", "teacher@example.com", Role.TEACHER);
        Authentication authentication = authenticationFor(teacher);

        when(classroomRepository.existsByName("Physics")).thenReturn(false);
        when(classroomRepository.save(any(ClassEntity.class))).thenAnswer(invocation -> {
            ClassEntity saved = invocation.getArgument(0);
            saved.setId("class-1");
            return saved;
        });

        ApiResponseEntity<ClassResponse> response = classroomService.createClassroom("Physics", authentication);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("class-1", response.getData().getClassId());
        assertEquals("Physics", response.getData().getClassName());
        assertEquals("teacher-1", response.getData().getTeacherId());
        assertTrue(response.getData().getStudentIds().isEmpty());

        ArgumentCaptor<ClassEntity> captor = ArgumentCaptor.forClass(ClassEntity.class);
        verify(classroomRepository).save(captor.capture());
        assertEquals("Physics", captor.getValue().getName());
        assertEquals("teacher-1", captor.getValue().getTeacherId());
        assertTrue(captor.getValue().getStudentIds().isEmpty());
    }

    @Test
    void createClassroomShouldThrowWhenNameAlreadyExists() {
        when(classroomRepository.existsByName("Physics")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
                () -> classroomService.createClassroom("Physics", mock(Authentication.class)));

        assertEquals("Class with this name already exists", exception.getMessage());
        verify(classroomRepository, never()).save(any(ClassEntity.class));
    }

    @Test
    void addStudentsShouldMergeOnlyNewStudentIds() {
        UserEntity teacher = user("teacher-1", "teacher@example.com", Role.TEACHER);

        ClassEntity classEntity = ClassEntity.builder()
                .id("class-1")
                .name("Physics")
                .teacherId("teacher-1")
                .studentIds(new ArrayList<>(List.of("student-1")))
                .build();

        AddStudentsRequest request = AddStudentsRequest.builder()
                .classId("class-1")
                .studentIds(List.of("student-1", "student-2"))
                .build();

        when(classroomRepository.findById("class-1")).thenReturn(Optional.of(classEntity));
        when(userRepository.findAllById(any(Iterable.class))).thenReturn(List.of(
                user("student-1", "student1@example.com", Role.STUDENT),
                user("student-2", "student2@example.com", Role.STUDENT)
        ));
        when(classroomRepository.save(any(ClassEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponseEntity<ClassResponse> response = classroomService.addStudents(request, authenticationFor(teacher));

        assertTrue(response.isSuccess());
        assertEquals(List.of("student-1", "student-2"), response.getData().getStudentIds());

        ArgumentCaptor<ClassEntity> captor = ArgumentCaptor.forClass(ClassEntity.class);
        verify(classroomRepository).save(captor.capture());
        assertEquals(List.of("student-1", "student-2"), captor.getValue().getStudentIds());
    }

    @Test
    void addStudentsShouldThrowWhenTeacherDoesNotOwnClass() {
        UserEntity teacher = user("teacher-2", "teacher@example.com", Role.TEACHER);
        ClassEntity classEntity = ClassEntity.builder()
                .id("class-1")
                .name("Physics")
                .teacherId("teacher-1")
                .studentIds(new ArrayList<>())
                .build();

        when(classroomRepository.findById("class-1")).thenReturn(Optional.of(classEntity));

        AddStudentsRequest request = AddStudentsRequest.builder()
                .classId("class-1")
                .studentIds(List.of("student-1"))
                .build();

        ApiException exception = assertThrows(ApiException.class,
                () -> classroomService.addStudents(request, authenticationFor(teacher)));

        assertEquals("You are not authorized to modify this class", exception.getMessage());
        verify(classroomRepository, never()).save(any(ClassEntity.class));
    }

    @Test
    void addStudentsShouldThrowWhenStudentListIsEmpty() {
        UserEntity teacher = user("teacher-1", "teacher@example.com", Role.TEACHER);
        ClassEntity classEntity = ClassEntity.builder()
                .id("class-1")
                .name("Physics")
                .teacherId("teacher-1")
                .studentIds(new ArrayList<>())
                .build();

        when(classroomRepository.findById("class-1")).thenReturn(Optional.of(classEntity));

        AddStudentsRequest request = AddStudentsRequest.builder()
                .classId("class-1")
                .studentIds(List.of())
                .build();

        ApiException exception = assertThrows(ApiException.class,
                () -> classroomService.addStudents(request, authenticationFor(teacher)));

        assertEquals("Student list cannot be empty", exception.getMessage());
        verify(userRepository, never()).findAllById(any(Iterable.class));
    }

    @Test
    void getClassroomShouldReturnDetailsForEnrolledStudent() {
        UserEntity student = user("student-1", "student@example.com", Role.STUDENT);
        ClassEntity classEntity = ClassEntity.builder()
                .id("class-1")
                .name("Physics")
                .teacherId("teacher-1")
                .studentIds(List.of("student-1", "student-2"))
                .build();

        when(classroomRepository.findById("class-1")).thenReturn(Optional.of(classEntity));
        when(userRepository.findAllById(classEntity.getStudentIds())).thenReturn(List.of(
                user("student-1", "student1@example.com", Role.STUDENT),
                user("student-2", "student2@example.com", Role.STUDENT)
        ));

        ApiResponseEntity<ClassDetailResponse> response = classroomService.getClassroom("class-1", authenticationFor(student));

        assertTrue(response.isSuccess());
        assertEquals("class-1", response.getData().getClassId());
        assertEquals("Physics", response.getData().getClassName());
        assertEquals(2, response.getData().getStudents().size());
        assertEquals("student-1", response.getData().getStudents().get(0).getId());
    }

    @Test
    void getClassroomShouldThrowWhenUserHasNoAccess() {
        UserEntity outsider = user("student-9", "outsider@example.com", Role.STUDENT);
        ClassEntity classEntity = ClassEntity.builder()
                .id("class-1")
                .name("Physics")
                .teacherId("teacher-1")
                .studentIds(List.of("student-1"))
                .build();

        when(classroomRepository.findById("class-1")).thenReturn(Optional.of(classEntity));

        ApiException exception = assertThrows(ApiException.class,
                () -> classroomService.getClassroom("class-1", authenticationFor(outsider)));

        assertEquals("You are not authorized to view this class", exception.getMessage());
        verify(userRepository, never()).findAllById(any(Iterable.class));
    }

    @Test
    void getAllStudentsShouldReturnDtosForTeacher() {
        UserEntity teacher = user("teacher-1", "teacher@example.com", Role.TEACHER);
        when(userRepository.findAllByRole(Role.STUDENT)).thenReturn(List.of(
                user("student-1", "student1@example.com", Role.STUDENT),
                user("student-2", "student2@example.com", Role.STUDENT)
        ));

        ApiResponseEntity<List<UserDto>> response = classroomService.getAllStudents(authenticationFor(teacher));

        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size());
        assertEquals("student1@example.com", response.getData().get(0).getEmail());
    }

    @Test
    void getAllStudentsShouldReturnEmptyListWhenNoStudentsExist() {
        UserEntity teacher = user("teacher-1", "teacher@example.com", Role.TEACHER);
        when(userRepository.findAllByRole(Role.STUDENT)).thenReturn(List.of());

        ApiResponseEntity<List<UserDto>> response = classroomService.getAllStudents(authenticationFor(teacher));

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void getAllStudentsShouldThrowWhenRequesterIsNotTeacher() {
        UserEntity student = user("student-1", "student@example.com", Role.STUDENT);

        ApiException exception = assertThrows(ApiException.class,
                () -> classroomService.getAllStudents(authenticationFor(student)));

        assertEquals("You are not authorized to view this list", exception.getMessage());
        verify(userRepository, never()).findAllByRole(Role.STUDENT);
    }

    @Test
    void fetchUserShouldThrowWhenPrincipalIsNull() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> ClassroomService.fetchUser(authentication));

        assertEquals("UserDetails missing!", exception.getMessage());
    }

    private static Authentication authenticationFor(UserEntity user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(user));
        return authentication;
    }

    private static UserEntity user(String id, String email, Role role) {
        return UserEntity.builder()
                .id(id)
                .name(id)
                .email(email)
                .password("secret")
                .role(role)
                .build();
    }
}
