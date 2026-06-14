package com.eldercare.repository;

import com.eldercare.entity.Elder;
import com.eldercare.common.enums.ElderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElderRepository extends JpaRepository<Elder, Long>, JpaSpecificationExecutor<Elder> {

    Optional<Elder> findByElderCode(String elderCode);

    List<Elder> findByStatus(ElderStatus status);

    List<Elder> findByServiceAreaAndStatus(String serviceArea, ElderStatus status);

    Long countByStatus(ElderStatus status);

    boolean existsByElderCode(String elderCode);
}
