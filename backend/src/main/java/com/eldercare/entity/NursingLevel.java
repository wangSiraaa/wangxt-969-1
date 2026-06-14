package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import com.eldercare.common.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nursing_level")
public class NursingLevel extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "min_score")
    private Integer minScore;

    @Column(name = "max_score")
    private Integer maxScore;

    @Column(name = "base_hourly_rate", precision = 10, scale = 2)
    private BigDecimal baseHourlyRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel;

    @Column(name = "required_qualifications")
    private String requiredQualifications;

    @Column(name = "enabled")
    private Boolean enabled = true;
}
