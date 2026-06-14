package com.eldercare.repository;

import com.eldercare.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    Optional<ServiceRecord> findByWorkOrderId(Long workOrderId);

    List<ServiceRecord> findByNurseId(Long nurseId);

    List<ServiceRecord> findByElderId(Long elderId);
}
