package com.eldercare.repository;

import com.eldercare.entity.NursingLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NursingLevelRepository extends JpaRepository<NursingLevel, Long>, JpaSpecificationExecutor<NursingLevel> {

    Optional<NursingLevel> findByCode(String code);

    List<NursingLevel> findByEnabledTrue();

    List<NursingLevel> findByEnabledTrueOrderByIdAsc();

    boolean existsByCode(String code);
}
