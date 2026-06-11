package com.eldercare.repository;

import com.eldercare.entity.CheckInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
    List<CheckInRecord> findByWorkOrderId(Long workOrderId);
    List<CheckInRecord> findByCaregiverId(Long caregiverId);
    List<CheckInRecord> findByElderId(Long elderId);
    Optional<CheckInRecord> findTopByWorkOrderIdOrderByCheckInTimeDesc(Long workOrderId);
    boolean existsByWorkOrderId(Long workOrderId);
}
