package com.eldercare.repository;

import com.eldercare.entity.SettlementReviewQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementReviewQueueRepository extends JpaRepository<SettlementReviewQueue, Long> {

    Optional<SettlementReviewQueue> findByReviewCode(String reviewCode);

    Optional<SettlementReviewQueue> findBySettlementId(Long settlementId);

    List<SettlementReviewQueue> findByStatusOrderBySubmittedAtDesc(String status);

    Page<SettlementReviewQueue> findByStatus(String status, Pageable pageable);

    List<SettlementReviewQueue> findByElderIdOrderBySubmittedAtDesc(Long elderId);

    List<SettlementReviewQueue> findByNurseIdOrderBySubmittedAtDesc(Long nurseId);

    List<SettlementReviewQueue> findByReviewTriggerTypeOrderBySubmittedAtDesc(String reviewTriggerType);

    Page<SettlementReviewQueue> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    long countByStatus(String status);
}
