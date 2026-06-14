package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import com.eldercare.common.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "supplement_order")
public class SupplementOrder extends BaseEntity {

    @Column(name = "supplement_code", unique = true, nullable = false)
    private String supplementCode;

    @Column(name = "original_work_order_id")
    private Long originalWorkOrderId;

    @Column(name = "original_order_code")
    private String originalOrderCode;

    @Column(name = "demand_id")
    private Long demandId;

    @Column(name = "demand_code")
    private String demandCode;

    @Column(name = "supplement_reason", columnDefinition = "TEXT")
    private String supplementReason;

    @Column(name = "supplement_description", columnDefinition = "TEXT")
    private String supplementDescription;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "applied_by")
    private String appliedBy;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "approval_remark", columnDefinition = "TEXT")
    private String approvalRemark;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "nurse_id")
    private Long nurseId;

    @Column(name = "nurse_name")
    private String nurseName;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    private String status;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(columnDefinition = "TEXT")
    private String remark;
}
