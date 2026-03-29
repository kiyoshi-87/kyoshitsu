package com.kiyoshi87.application.kyoshitsu.repository;

import com.kiyoshi87.application.kyoshitsu.model.Role;
import com.kiyoshi87.application.kyoshitsu.model.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String>
{
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findAllByRole(Role role);
}
