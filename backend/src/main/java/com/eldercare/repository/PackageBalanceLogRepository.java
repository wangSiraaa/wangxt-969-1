package com.eldercare.repository;

import com.eldercare.entity.PackageBalanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageBalanceLogRepository extends JpaRepository<PackageBalanceLog, Long> {

    List<PackageBalanceLog> findByElderServicePackageIdOrderByOperatedAtDesc(Long elderServicePackageId);

    List<PackageBalanceLog> findByElderIdOrderByOperatedAtDesc(Long elderId);

    Page<PackageBalanceLog> findByElderId(Long elderId, Pageable pageable);

    List<PackageBalanceLog> findByWorkOrderId(Long workOrderId);

    List<PackageBalanceLog> findByChangeTypeOrderByOperatedAtDesc(String changeType);
}
