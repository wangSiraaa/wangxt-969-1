package com.eldercare.repository;

import com.eldercare.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByDemandId(Long demandId);
    List<WorkOrder> findByCaregiverId(Long caregiverId);
    List<WorkOrder> findByElderId(Long elderId);
    List<WorkOrder> findByOrderStatus(String orderStatus);
    WorkOrder findByOrderNo(String orderNo);
}
