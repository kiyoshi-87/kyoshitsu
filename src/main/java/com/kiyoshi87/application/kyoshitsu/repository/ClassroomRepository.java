package com.kiyoshi87.application.kyoshitsu.repository;

import com.kiyoshi87.application.kyoshitsu.model.entity.ClassEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClassroomRepository extends MongoRepository<ClassEntity, String> {

    boolean existsByName(String name);

    boolean existsByIdAndTeacherId(String id, String teacherId);

    List<ClassEntity> findAllByTeacherId(String id);
}
