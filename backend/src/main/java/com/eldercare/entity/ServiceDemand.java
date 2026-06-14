package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
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
@Table(name = "service_demand")
public class ServiceDemand extends BaseEntity {

    @Column(name = "demand_code", unique = true, nullable = false)
    private String demandCode;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "service_package_id")
    private Long servicePackageId;

    @Column(name = "service_package_name")
    private String servicePackageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "nursing_level_code")
    private String nursingLevelCode;

    @Column(name = "requested_date")
    private LocalDate requestedDate;

    @Column(name = "requested_start_time")
    private LocalTime requestedStartTime;

    @Column(name = "requested_end_time")
    private LocalTime requestedEndTime;

    @Column(name = "address")
    private String address;

    @Column(name = "longitude", precision = 12, scale = 8)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 12, scale = 8)
    private BigDecimal latitude;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @Column(name = "contraindication_ids")
    private String contraindicationIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "current_version")
    private Integer currentVersion = 1;

    @Column(name = "status")
    private String status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
