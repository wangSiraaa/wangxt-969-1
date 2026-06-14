package com.eldercare.repository;

import com.eldercare.entity.Nurse;
import com.eldercare.common.enums.NurseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NurseRepository extends JpaRepository<Nurse, Long>, JpaSpecificationExecutor<Nurse> {

    Optional<Nurse> findByNurseCode(String nurseCode);

    List<Nurse> findByStatus(NurseStatus status);

    @Query("SELECT n FROM Nurse n WHERE n.status = 'ACTIVE' AND n.serviceAreas LIKE %:serviceArea%")
    List<Nurse> findActiveByServiceArea(@Param("serviceArea") String serviceArea);

    Long countByStatus(NurseStatus status);

    boolean existsByNurseCode(String nurseCode);
}
