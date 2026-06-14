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
@Table(name = "elder_service_package")
public class ElderServicePackage extends BaseEntity {

    @Column(name = "account_code", unique = true, nullable = false)
    private String accountCode;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "service_package_id", nullable = false)
    private Long servicePackageId;

    @Column(name = "service_package_name")
    private String servicePackageName;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "used_sessions")
    private Integer usedSessions = 0;

    @Column(name = "remaining_sessions")
    private Integer remainingSessions;

    @Column(name = "total_minutes")
    private Integer totalMinutes;

    @Column(name = "used_minutes")
    private Integer usedMinutes = 0;

    @Column(name = "remaining_minutes")
    private Integer remainingMinutes;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "used_amount", precision = 10, scale = 2)
    private BigDecimal usedAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", precision = 10, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "purchase_channel")
    private String purchaseChannel;

    @Column(name = "status")
    private String status;

    @Column(name = "pause_start_time")
    private LocalDateTime pauseStartTime;

    @Column(name = "pause_end_time")
    private LocalDateTime pauseEndTime;

    @Column(name = "pause_reason")
    private String pauseReason;

    @Column(name = "paused_days")
    private Integer pausedDays = 0;

    @Column(name = "auto_renewal")
    private Boolean autoRenewal = false;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
