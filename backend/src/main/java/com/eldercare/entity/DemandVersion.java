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
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "demand_version")
public class DemandVersion extends BaseEntity {

    @Column(name = "demand_id", nullable = false)
    private Long demandId;

    @Column(name = "demand_code")
    private String demandCode;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "service_package_id")
    private Long servicePackageId;

    @Column(name = "service_package_name")
    private String servicePackageName;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

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

    @Column(name = "version_data", columnDefinition = "TEXT")
    private String versionData;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Column(name = "changed_by")
    private String changedBy;
}
