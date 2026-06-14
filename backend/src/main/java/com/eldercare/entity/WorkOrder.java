package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.common.enums.RiskLevel;
import com.eldercare.common.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_order")
public class WorkOrder extends BaseEntity {

    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode;

    @Column(name = "demand_id", nullable = false)
    private Long demandId;

    @Column(name = "demand_code")
    private String demandCode;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "nurse_id", nullable = false)
    private Long nurseId;

    @Column(name = "nurse_name")
    private String nurseName;

    @Column(name = "service_package_id")
    private Long servicePackageId;

    @Column(name = "service_package_name")
    private String servicePackageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "scheduled_start_time")
    private LocalTime scheduledStartTime;

    @Column(name = "scheduled_end_time")
    private LocalTime scheduledEndTime;

    @Column(name = "address")
    private String address;

    @Column(name = "longitude", precision = 12, scale = 8)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 12, scale = 8)
    private BigDecimal latitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "dispatched_by")
    private String dispatchedBy;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
