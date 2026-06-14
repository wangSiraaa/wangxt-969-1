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
@Table(name = "settlement")
public class Settlement extends BaseEntity {

    @Column(name = "settlement_code", unique = true, nullable = false)
    private String settlementCode;

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "order_code")
    private String orderCode;

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

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "service_duration_minutes")
    private Integer serviceDurationMinutes;

    @Column(name = "base_amount", precision = 10, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "extra_amount", precision = 10, scale = 2)
    private BigDecimal extraAmount = BigDecimal.ZERO;

    @Column(name = "deduct_amount", precision = 10, scale = 2)
    private BigDecimal deductAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "insurance_covered")
    private Boolean insuranceCovered = false;

    @Column(name = "insurance_amount", precision = 10, scale = 2)
    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    @Column(name = "self_pay_amount", precision = 10, scale = 2)
    private BigDecimal selfPayAmount;

    private String status;

    @Column(name = "family_confirmed")
    private Boolean familyConfirmed = false;

    @Column(name = "family_confirmed_at")
    private LocalDateTime familyConfirmedAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "settled_by")
    private String settledBy;

    @Column(columnDefinition = "TEXT")
    private String remark;
}
