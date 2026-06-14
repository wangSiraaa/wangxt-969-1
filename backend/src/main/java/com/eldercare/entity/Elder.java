package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import com.eldercare.common.enums.ElderStatus;
import com.eldercare.common.enums.Gender;
import com.eldercare.common.enums.RiskLevel;
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
@Table(name = "elder")
public class Elder extends BaseEntity {

    @Column(name = "elder_code", unique = true, nullable = false)
    private String elderCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "id_card", unique = true)
    private String idCard;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "longitude", precision = 12, scale = 8)
    private BigDecimal longitude;

    @Column(name = "latitude", precision = 12, scale = 8)
    private BigDecimal latitude;

    @Column(name = "service_area")
    private String serviceArea;

    @Column(name = "nursing_level_id")
    private Long nursingLevelId;

    @Column(name = "service_package_id")
    private Long servicePackageId;

    @Column(name = "health_status", columnDefinition = "TEXT")
    private String healthStatus;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "contraindication_ids")
    private String contraindicationIds;

    @Column(name = "emergency_contacts")
    private String emergencyContacts;

    @Column(name = "insurant_no")
    private String insurantNo;

    @Column(name = "insurance_type")
    private String insuranceType;

    @Column(name = "insurance_coverage", precision = 12, scale = 2)
    private BigDecimal insuranceCoverage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ElderStatus status;

    @Column(name = "pause_start_time")
    private LocalDateTime pauseStartTime;

    @Column(name = "pause_end_time")
    private LocalDateTime pauseEndTime;

    @Column(name = "pause_reason")
    private String pauseReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "credit_score")
    private Integer creditScore = 100;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
