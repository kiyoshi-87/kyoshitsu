package com.kiyoshi87.application.kyoshitsu.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassSessionResponse {

    private String classId;
    private Instant startTime; // DS might be changed in future
    private Instant endTime;
}
