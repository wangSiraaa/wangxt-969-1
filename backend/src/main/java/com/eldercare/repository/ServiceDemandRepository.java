package com.eldercare.repository;

import com.eldercare.entity.ServiceDemand;
import com.eldercare.enums.DemandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceDemandRepository extends JpaRepository<ServiceDemand, Long> {
    List<ServiceDemand> findByStatus(DemandStatus status);
    List<ServiceDemand> findByElderId(Long elderId);
    List<ServiceDemand> findByStatusIn(List<DemandStatus> statuses);
}
