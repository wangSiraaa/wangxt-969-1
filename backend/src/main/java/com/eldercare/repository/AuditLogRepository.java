package com.eldercare.repository;

import com.eldercare.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByEntityTypeAndBusinessIdOrderByCreatedAtDesc(String entityType, Long businessId);

    List<AuditLog> findByOperatorOrderByCreatedAtDesc(String operator);

    List<AuditLog> findByModuleAndBusinessIdOrderByOperatedAtDesc(String module, Long businessId);

    List<AuditLog> findByOperatorOrderByOperatedAtDesc(String operator);

    List<AuditLog> findByOperatedAtBetweenOrderByOperatedAtDesc(LocalDateTime start, LocalDateTime end);
}
