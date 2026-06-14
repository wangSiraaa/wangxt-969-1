package com.eldercare.repository;

import com.eldercare.entity.Compensation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompensationRepository extends JpaRepository<Compensation, Long>, JpaSpecificationExecutor<Compensation> {

    List<Compensation> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId);

    List<Compensation> findByElderIdOrderByCreatedAtDesc(Long elderId);

    List<Compensation> findByStatusOrderByCreatedAtDesc(String status);
}
