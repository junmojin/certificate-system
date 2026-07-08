package com.junmo.certificatesystem.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.junmo.certificatesystem.entity.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query(value = """
            SELECT a.* FROM activity_logs a
            INNER JOIN users u ON u.id = a.actor_id
            WHERE u.role = 'USER'
            AND (:userId IS NULL OR a.user_id = :userId)
            AND (:logDate IS NULL OR DATE(a.created_at) = :logDate)
            ORDER BY a.created_at DESC
            """, nativeQuery = true)
    List<ActivityLog> search(
            @Param("userId") String userId,
            @Param("logDate") LocalDate logDate);
}
