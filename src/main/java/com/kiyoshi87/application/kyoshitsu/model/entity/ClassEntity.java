package com.kiyoshi87.application.kyoshitsu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Document(collection = "class")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1948642370759535739L;

    @Id
    private String id;

    private String name;
    private String teacherId; // ID for the teacher user
    private List<String> studentIds; // IDs for the students
}
