package com.kiyoshi87.application.kyoshitsu.repository;

import com.kiyoshi87.application.kyoshitsu.model.entity.AttendanceRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttendanceRecordRepository extends MongoRepository<AttendanceRecord, String> {
        boolean existsBySessionIdAndStudentId(String sessionId, String studentId);
}
