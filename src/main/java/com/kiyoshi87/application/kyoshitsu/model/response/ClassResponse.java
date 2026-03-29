package com.kiyoshi87.application.kyoshitsu.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassResponse {

    private String classId;
    private String className;
    private String teacherId;
    private List<String> studentIds;
}
