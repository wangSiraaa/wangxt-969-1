package com.eldercare.repository;

import com.eldercare.entity.ElderServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElderServicePackageRepository extends JpaRepository<ElderServicePackage, Long> {

    List<ElderServicePackage> findByElderIdOrderByCreatedAtDesc(Long elderId);

    Optional<ElderServicePackage> findByAccountCode(String accountCode);

    List<ElderServicePackage> findByElderIdAndStatusOrderByCreatedAtDesc(Long elderId, String status);

    List<ElderServicePackage> findByServicePackageId(Long servicePackageId);
}
