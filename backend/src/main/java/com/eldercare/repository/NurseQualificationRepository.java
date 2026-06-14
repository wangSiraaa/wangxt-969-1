package com.eldercare.repository;

import com.eldercare.entity.NurseQualification;
import com.eldercare.common.enums.QualificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NurseQualificationRepository extends JpaRepository<NurseQualification, Long>, JpaSpecificationExecutor<NurseQualification> {

    List<NurseQualification> findByNurseId(Long nurseId);

    List<NurseQualification> findByNurseIdAndStatus(Long nurseId, QualificationStatus status);

    List<NurseQualification> findByNurseIdAndStatusIn(Long nurseId, List<QualificationStatus> statuses);
}
