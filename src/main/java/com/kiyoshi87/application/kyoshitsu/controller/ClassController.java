package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.model.ApiResponse;
import com.kiyoshi87.application.kyoshitsu.model.request.AddStudentsRequest;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassDetailResponse;
import com.kiyoshi87.application.kyoshitsu.model.response.ClassResponse;
import com.kiyoshi87.application.kyoshitsu.service.ClassroomService;
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
public class ClassController {

    private final ClassroomService service;

    /**
     * <h3>Creates an empty class with the teacher's ID<h3/>
     *
     * @param className
     * @return success response on creation of new classroom
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ClassResponse> createClass(@RequestParam String className, Authentication authentication) {
        return service.createClassroom(className, authentication);
    }

    @PostMapping("/add-students")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<ClassResponse> addStudent(@RequestBody AddStudentsRequest request, Authentication authentication) {
        return service.addStudents(request, authentication);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    public ApiResponse<ClassDetailResponse> getClass(@PathVariable String id, Authentication authentication) {
        return service.getClassroom(id, authentication);
    }
}
