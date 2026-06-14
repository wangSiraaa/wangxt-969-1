package com.eldercare.repository;

import com.eldercare.common.enums.OrderStatus;
import com.eldercare.entity.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByOrderCode(String orderCode);

    Optional<WorkOrder> findByDemandId(Long demandId);

    List<WorkOrder> findByElderId(Long elderId);

    List<WorkOrder> findByNurseId(Long nurseId);

    List<WorkOrder> findByStatus(OrderStatus status);

    Page<WorkOrder> findByStatus(OrderStatus status, Pageable pageable);

    Page<WorkOrder> findByNurseId(Long nurseId, Pageable pageable);

    Page<WorkOrder> findByElderId(Long elderId, Pageable pageable);

    @Query("SELECT w FROM WorkOrder w WHERE w.nurseId = :nurseId AND w.status = :status")
    List<WorkOrder> findByNurseIdAndStatus(@Param("nurseId") Long nurseId, @Param("status") OrderStatus status);

    @Query("SELECT w FROM WorkOrder w WHERE w.nurseId = :nurseId AND w.scheduledDate = :scheduledDate AND w.status NOT IN ('CANCELLED','ABNORMAL')")
    List<WorkOrder> findByNurseIdAndScheduledDate(
            @Param("nurseId") Long nurseId,
            @Param("scheduledDate") LocalDate scheduledDate);

    @Query("SELECT COUNT(w) FROM WorkOrder w WHERE w.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(w) FROM WorkOrder w WHERE w.nurseId = :nurseId AND w.scheduledDate = :date AND w.status NOT IN ('CANCELLED')")
    Integer countNurseDailyOrders(@Param("nurseId") Long nurseId, @Param("date") LocalDate date);

    @Query("SELECT w FROM WorkOrder w WHERE w.status IN :statuses AND w.completedAt BETWEEN :start AND :end")
    List<WorkOrder> findByStatusesAndCompletedAtBetween(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT w FROM WorkOrder w WHERE w.scheduledDate = :today AND w.status = 'DISPATCHED' AND w.scheduledEndTime < :now")
    List<WorkOrder> findTimeoutOrders(@Param("today") LocalDate today, @Param("now") LocalDateTime now);

    @Query("SELECT w FROM WorkOrder w WHERE w.status = 'SERVICE_COMPLETED' AND w.completedAt < :threshold")
    List<WorkOrder> findPendingFamilyConfirmTimeout(@Param("threshold") LocalDateTime threshold);
}
