package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compensation")
public class Compensation extends BaseEntity {

    @Column(name = "compensation_code", unique = true, nullable = false)
    private String compensationCode;

    @Column(name = "abnormal_event_id")
    private Long abnormalEventId;

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

    @Column(name = "compensation_type")
    private String compensationType;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "compensation_amount", precision = 10, scale = 2)
    private BigDecimal compensationAmount;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "compensation_description", columnDefinition = "TEXT")
    private String compensationDescription;

    private String status;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "applied_by")
    private String appliedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String remark;
}
