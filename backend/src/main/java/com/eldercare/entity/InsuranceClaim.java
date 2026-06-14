package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_claim")
public class InsuranceClaim extends BaseEntity {

    @Column(name = "claim_code", unique = true, nullable = false)
    private String claimCode;

    @Column(name = "settlement_id")
    private Long settlementId;

    @Column(name = "settlement_code")
    private String settlementCode;

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "insurant_no")
    private String insurantNo;

    @Column(name = "insurance_type")
    private String insuranceType;

    @Column(name = "claim_amount", precision = 10, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "approved_amount", precision = 10, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @Column(name = "claim_description", columnDefinition = "TEXT")
    private String claimDescription;

    @Column(name = "claim_materials", columnDefinition = "TEXT")
    private String claimMaterials;

    private String status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "applied_by")
    private String appliedBy;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(columnDefinition = "TEXT")
    private String remark;
}
