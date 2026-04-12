package com.kiyoshi87.application.kyoshitsu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Document(collection = "attendance_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "unique_attendance", def = "{'sessionId': 1, 'studentId': 1}", unique = true)
public class AttendanceRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = -5273654083774792963L;

    @Id
    private String id;

    private String sessionId;
    private String classId;
    private String studentId;
    private Instant timestamp;
}
