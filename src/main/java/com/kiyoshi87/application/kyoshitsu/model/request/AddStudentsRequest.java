package com.kiyoshi87.application.kyoshitsu.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddStudentsRequest {

    private String classId;
    private List<String> studentIds;
}
