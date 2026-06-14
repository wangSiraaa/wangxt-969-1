package com.eldercare.repository;

import com.eldercare.entity.DemandVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandVersionRepository extends JpaRepository<DemandVersion, Long> {

    List<DemandVersion> findByDemandIdOrderByVersionDesc(Long demandId);

    DemandVersion findByDemandIdAndVersion(Long demandId, Integer version);
}
