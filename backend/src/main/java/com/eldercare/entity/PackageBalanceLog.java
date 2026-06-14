package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "package_balance_log")
public class PackageBalanceLog extends BaseEntity {

    @Column(name = "log_code", unique = true, nullable = false)
    private String logCode;

    @Column(name = "elder_service_package_id", nullable = false)
    private Long elderServicePackageId;

    @Column(name = "account_code")
    private String accountCode;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "service_package_id")
    private Long servicePackageId;

    @Column(name = "service_package_name")
    private String servicePackageName;

    @Column(name = "change_type")
    private String changeType;

    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "abnormal_event_id")
    private Long abnormalEventId;

    @Column(name = "event_code")
    private String eventCode;

    @Column(name = "before_sessions")
    private Integer beforeSessions;

    @Column(name = "change_sessions")
    private Integer changeSessions = 0;

    @Column(name = "after_sessions")
    private Integer afterSessions;

    @Column(name = "before_minutes")
    private Integer beforeMinutes;

    @Column(name = "change_minutes")
    private Integer changeMinutes = 0;

    @Column(name = "after_minutes")
    private Integer afterMinutes;

    @Column(name = "before_amount", precision = 10, scale = 2)
    private BigDecimal beforeAmount;

    @Column(name = "change_amount", precision = 10, scale = 2)
    private BigDecimal changeAmount = BigDecimal.ZERO;

    @Column(name = "after_amount", precision = 10, scale = 2)
    private BigDecimal afterAmount;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "operated_at")
    private LocalDateTime operatedAt;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
