package com.kiyoshi87.application.kyoshitsu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;

@Document(collection = "attendance")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attendance implements Serializable {

    @Serial
    private static final long serialVersionUID = 8387822678442998053L;
    @Id
    private String id;

    private String classId;
    private String userId;  // ID for student user
    private String status; // "present" or "absent"
}
