package com.kiyoshi87.application.kyoshitsu.repository;

import com.kiyoshi87.application.kyoshitsu.model.entity.ClassSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ClassSessionRepository extends MongoRepository<ClassSession, String> {

    Optional<ClassSession> findByClassIdAndActiveTrue(String classId);

    boolean existsByClassIdAndActiveTrue(String classId);
}
