package com.eldercare.repository;

import com.eldercare.entity.SupplementOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplementOrderRepository extends JpaRepository<SupplementOrder, Long> {

    List<SupplementOrder> findByOriginalWorkOrderId(Long originalWorkOrderId);

    List<SupplementOrder> findByApprovalStatus(String approvalStatus);

    List<SupplementOrder> findByAppliedBy(String appliedBy);
}
