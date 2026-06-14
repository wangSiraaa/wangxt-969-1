package com.eldercare.repository;

import com.eldercare.entity.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {

    List<InsuranceClaim> findBySettlementId(Long settlementId);

    List<InsuranceClaim> findByElderId(Long elderId);

    List<InsuranceClaim> findByStatus(String status);
}
