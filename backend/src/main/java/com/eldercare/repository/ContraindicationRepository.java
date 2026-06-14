package com.eldercare.repository;

import com.eldercare.entity.Contraindication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContraindicationRepository extends JpaRepository<Contraindication, Long>, JpaSpecificationExecutor<Contraindication> {

    Optional<Contraindication> findByCode(String code);

    List<Contraindication> findByEnabledTrue();

    List<Contraindication> findByEnabledTrueOrderBySeverityDesc();

    boolean existsByCode(String code);
}
