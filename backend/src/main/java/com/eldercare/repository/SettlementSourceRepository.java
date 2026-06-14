package com.eldercare.repository;

import com.eldercare.entity.SettlementSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SettlementSourceRepository extends JpaRepository<SettlementSource, Long> {

    List<SettlementSource> findBySettlementIdOrderByCreatedAtDesc(Long settlementId);

    List<SettlementSource> findBySettlementCodeOrderByCreatedAtDesc(String settlementCode);

    List<SettlementSource> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId);

    List<SettlementSource> findByElderIdOrderByServiceDateDesc(Long elderId);

    Page<SettlementSource> findByElderId(Long elderId, Pageable pageable);

    List<SettlementSource> findByNurseIdOrderByServiceDateDesc(Long nurseId);

    Page<SettlementSource> findByNurseId(Long nurseId, Pageable pageable);

    List<SettlementSource> findByServicePackageIdOrderByServiceDateDesc(Long servicePackageId);

    List<SettlementSource> findByAbnormalTypeOrderByServiceDateDesc(String abnormalType);

    Page<SettlementSource> findByAbnormalType(String abnormalType, Pageable pageable);

    List<SettlementSource> findByServiceItemCodeOrderByServiceDateDesc(String serviceItemCode);

    List<SettlementSource> findByServiceDateBetweenOrderByServiceDateDesc(LocalDate startDate, LocalDate endDate);

    List<SettlementSource> findBySourceTypeOrderByServiceDateDesc(String sourceType);
}
