package com.kiyoshi87.application.kyoshitsu.service;

import com.kiyoshi87.application.kyoshitsu.exceptions.ApiException;
import com.kiyoshi87.application.kyoshitsu.helper.StaticHelper;
import com.kiyoshi87.application.kyoshitsu.model.ApiResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository repository;
    private final UserRepository userRepository;

    public ApiResponse<ClassResponse> createClassroom(String className,
                                                      Authentication authentication) {

        if (repository.existsByName(className)) {
            throw new ApiException("Class with this name already exists");
        }

        UserEntity teacher = fetchUser(authentication);

        ClassEntity classEntity = ClassEntity.builder()
                .name(className)
                .teacherId(teacher.getId())
                .studentIds(new ArrayList<>())
                .build();

        classEntity = repository.save(classEntity);

        return ApiResponse.success(ClassResponse.builder()
                        .classId(classEntity.getId())
                .className(className)
                .teacherId(teacher.getId())
                .studentIds(List.of())
                .build());
    }

    public ApiResponse<ClassResponse> addStudents(AddStudentsRequest request,
                                                  Authentication authentication) {

        UserEntity teacher = fetchUser(authentication);

        ClassEntity classroomEntity = fetchAndValidateClassroom(request, teacher);

        List<String> validStudentIds = fetchAndValidateStudents(request);

        List<String> existingStudents = classroomEntity.getStudentIds();

        for (String studentId : validStudentIds) {
            if (!existingStudents.contains(studentId)) {
                existingStudents.add(studentId);
            }
        }

        classroomEntity.setStudentIds(existingStudents);

        repository.save(classroomEntity);

        return ApiResponse.success(ClassResponse.builder()
                .classId(classroomEntity.getId())
                .className(classroomEntity.getName())
                .teacherId(classroomEntity.getTeacherId())
                .studentIds(existingStudents)
                .build());
    }

    public ApiResponse<ClassDetailResponse> getClassroom(String id, Authentication authentication) {
        ClassEntity classEntity = repository.findById(id)
                .orElseThrow(() -> new ApiException("Class not found"));

        UserEntity user = fetchUser(authentication);

        validateUserHasAccess(user, classEntity);

        List<UserEntity> studentEntities =
                userRepository.findAllById(classEntity.getStudentIds());

        List<UserDto> students = studentEntities
                .stream()
                .map(StaticHelper::toUserDto)
                .toList();

        ClassDetailResponse response = ClassDetailResponse.builder()
                .classId(classEntity.getId())
                .className(classEntity.getName())
                .teacherId(classEntity.getTeacherId())
                .students(students)
                .build();

        return ApiResponse.success(response);
    }

    public ApiResponse<List<UserDto>> getAllStudents(Authentication authentication) {
        UserEntity user = fetchUser(authentication);

        if (user.getRole() != Role.TEACHER) {
            throw new ApiException("You are not authorized to view this list");
        }

        List<UserEntity> studentEntities = userRepository.findAllByRole(Role.STUDENT);

        if (CollectionUtils.isEmpty(studentEntities)) {
            return ApiResponse.success(List.of());
        }

        List<UserDto> students = studentEntities
                .stream()
                .map(StaticHelper::toUserDto)
                .toList();

        return ApiResponse.success(students);
    }

    // + Might be extracted to a separate component if the usage increases
    private UserEntity fetchUser(Authentication authentication) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        if (userDetails == null) {
            throw new ApiException("UserDetails missing!");
        }

        return userDetails.getUser();
    }

    private ClassEntity fetchAndValidateClassroom(AddStudentsRequest request, UserEntity teacher) {
        ClassEntity classEntity = repository.findById(request.getClassId())
                .orElseThrow(() -> new ApiException("Class not found"));

        if (!classEntity.getTeacherId().equals(teacher.getId())) {
            throw new ApiException("You are not authorized to modify this class");
        }

        return classEntity;
    }

    private List<String> fetchAndValidateStudents(AddStudentsRequest request) {
        List<String> requestedIds = request.getStudentIds();

        if (requestedIds == null || requestedIds.isEmpty()) {
            throw new ApiException("Student list cannot be empty");
        }

        Set<String> uniqueIds = new HashSet<>(requestedIds);

        List<UserEntity> students = userRepository.findAllById(uniqueIds);

        if (students.size() != uniqueIds.size()) {
            throw new ApiException("Some students do not exist");
        }

        List<String> validStudentIds = new ArrayList<>();

        for (UserEntity student : students) {
            if (!student.getRole().equals(Role.STUDENT)) {
                throw new ApiException("User is not a student: " + student.getId());
            }
            validStudentIds.add(student.getId());
        }

        return validStudentIds;
    }

    private void validateUserHasAccess(UserEntity user, ClassEntity classEntity) {
        boolean isTeacherOwner =
                user.getRole() == Role.TEACHER &&
                        classEntity.getTeacherId().equals(user.getId());

        boolean isEnrolledStudent =
                user.getRole() == Role.STUDENT &&
                        classEntity.getStudentIds().contains(user.getId());

        if (!isTeacherOwner && !isEnrolledStudent) {
            throw new ApiException("You are not authorized to view this class");
        }
    }
}
