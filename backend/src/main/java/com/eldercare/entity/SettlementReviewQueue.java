package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "settlement_review_queue")
public class SettlementReviewQueue extends BaseEntity {

    @Column(name = "review_code", unique = true, nullable = false)
    private String reviewCode;

    @Column(name = "settlement_id", nullable = false)
    private Long settlementId;

    @Column(name = "settlement_code")
    private String settlementCode;

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "elder_id")
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "nurse_id")
    private Long nurseId;

    @Column(name = "nurse_name")
    private String nurseName;

    @Column(name = "service_package_id")
    private Long servicePackageId;

    @Column(name = "service_package_name")
    private String servicePackageName;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "settlement_amount", precision = 10, scale = 2)
    private BigDecimal settlementAmount;

    @Column(name = "review_trigger_type")
    private String reviewTriggerType;

    @Column(name = "abnormal_event_id")
    private Long abnormalEventId;

    @Column(name = "abnormal_event_type")
    private String abnormalEventType;

    @Column(name = "status")
    private String status;

    @Column(name = "review_level")
    private Integer reviewLevel = 1;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_result")
    private String reviewResult;

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "final_settlement_amount", precision = 10, scale = 2)
    private BigDecimal finalSettlementAmount;

    @Column(name = "adjustment_amount", precision = 10, scale = 2)
    private BigDecimal adjustmentAmount = BigDecimal.ZERO;

    @Column(name = "adjustment_reason")
    private String adjustmentReason;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
