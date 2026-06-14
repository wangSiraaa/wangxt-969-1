package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "settlement_source")
public class SettlementSource extends BaseEntity {

    @Column(name = "source_code", unique = true, nullable = false)
    private String sourceCode;

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

    @Column(name = "service_item_code")
    private String serviceItemCode;

    @Column(name = "service_item_name")
    private String serviceItemName;

    @Column(name = "service_date")
    private LocalDate serviceDate;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "abnormal_type")
    private String abnormalType;

    @Column(name = "abnormal_event_id")
    private Long abnormalEventId;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "unit")
    private String unit;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "package_deducted")
    private Boolean packageDeducted = false;

    @Column(name = "package_balance_log_id")
    private Long packageBalanceLogId;

    @Column(name = "is_extra_charge")
    private Boolean isExtraCharge = false;

    @Column(name = "is_deduction")
    private Boolean isDeduction = false;

    @Column(name = "source_reference_id")
    private Long sourceReferenceId;

    @Column(name = "source_reference_type")
    private String sourceReferenceType;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
