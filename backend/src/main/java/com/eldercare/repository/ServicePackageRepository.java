package com.eldercare.repository;

import com.eldercare.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long>, JpaSpecificationExecutor<ServicePackage> {

    Optional<ServicePackage> findByCode(String code);

    List<ServicePackage> findByEnabledTrue();

    List<ServicePackage> findByEnabledTrueOrderByIdAsc();

    boolean existsByCode(String code);
}
