package com.eldercare.repository;

import com.eldercare.entity.Settlement;
import com.eldercare.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByStatus(SettlementStatus status);
    List<Settlement> findByWorkOrderId(Long workOrderId);
    List<Settlement> findByElderId(Long elderId);
    List<Settlement> findByCaregiverId(Long caregiverId);
    Optional<Settlement> findBySettlementNo(String settlementNo);
    boolean existsByWorkOrderId(Long workOrderId);
}
