package com.eldercare.repository;

import com.eldercare.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long>, JpaSpecificationExecutor<EmergencyContact> {

    List<EmergencyContact> findByElderId(Long elderId);

    List<EmergencyContact> findByElderIdAndIsPrimaryTrue(Long elderId);
}
