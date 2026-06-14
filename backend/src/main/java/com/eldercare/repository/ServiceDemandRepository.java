package com.eldercare.repository;

import com.eldercare.entity.ServiceDemand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceDemandRepository extends JpaRepository<ServiceDemand, Long>, JpaSpecificationExecutor<ServiceDemand> {

    Optional<ServiceDemand> findByDemandCode(String demandCode);

    List<ServiceDemand> findByElderIdOrderByCreatedAtDesc(Long elderId);

    List<ServiceDemand> findByElderIdOrderBySubmittedAtDesc(Long elderId);

    List<ServiceDemand> findByStatusOrderByCreatedAtDesc(String status);

    List<ServiceDemand> findByStatusOrderBySubmittedAtDesc(String status);

    List<ServiceDemand> findByStatusOrderBySubmittedAtAsc(String status);

    Page<ServiceDemand> findAllByOrderBySubmittedAtDesc(Pageable pageable);

    Long countByStatus(String status);

    @Query("SELECT COUNT(d) FROM ServiceDemand d WHERE d.submittedAt >= :startOfDay AND d.submittedAt < :endOfDay")
    Long countTodaySubmitted(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    boolean existsByDemandCode(String demandCode);
}
