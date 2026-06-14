package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import com.eldercare.common.enums.Gender;
import com.eldercare.common.enums.NurseStatus;
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
@Table(name = "nurse")
public class Nurse extends BaseEntity {

    @Column(name = "nurse_code", unique = true, nullable = false)
    private String nurseCode;

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

    @Column(name = "service_areas")
    private String serviceAreas;

    @Column(name = "max_daily_orders")
    private Integer maxDailyOrders = 6;

    @Column(name = "max_weekly_hours")
    private Integer maxWeeklyHours = 40;

    @Column(name = "max_continuous_hours")
    private Integer maxContinuousHours = 6;

    @Column(name = "travel_time_radius_minutes")
    private Integer travelTimeRadiusMinutes = 30;

    @Column(name = "highest_nursing_level_id")
    private Long highestNursingLevelId;

    @Column(name = "qualification_summary", columnDefinition = "TEXT")
    private String qualificationSummary;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "completed_orders_count")
    private Integer completedOrdersCount = 0;

    @Column(name = "on_time_rate", precision = 6, scale = 2)
    private BigDecimal onTimeRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NurseStatus status;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
