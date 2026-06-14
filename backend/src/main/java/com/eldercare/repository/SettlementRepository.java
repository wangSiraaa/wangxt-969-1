package com.eldercare.repository;

import com.eldercare.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long>, JpaSpecificationExecutor<Settlement> {

    Optional<Settlement> findBySettlementCode(String settlementCode);

    Optional<Settlement> findByWorkOrderId(Long workOrderId);

    List<Settlement> findByElderIdOrderByCreatedAtDesc(Long elderId);

    List<Settlement> findByNurseIdOrderByCreatedAtDesc(Long nurseId);

    List<Settlement> findByStatusOrderByCreatedAtDesc(String status);

    Page<Settlement> findAllByOrderBySettledAtDesc(Pageable pageable);

    Page<Settlement> findByStatusOrderBySettledAtDesc(String status, Pageable pageable);

    List<Settlement> findByServiceDateAndStatus(LocalDate serviceDate, String status);

    List<Settlement> findByServiceDateBetweenAndStatus(LocalDate startDate, LocalDate endDate, String status);

    boolean existsByWorkOrderId(Long workOrderId);

    boolean existsBySettlementCode(String settlementCode);
}
