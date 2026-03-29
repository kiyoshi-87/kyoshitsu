package com.kiyoshi87.application.kyoshitsu.model.entity;

import com.kiyoshi87.application.kyoshitsu.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;

@Document(collection = "user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5339129803278515174L;

    @Id
    private String id;

    private String name;
    private String email;
    private String password;
    private Role role; // ? No need of @Enumerated
}
