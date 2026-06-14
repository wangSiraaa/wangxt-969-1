package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NurseCandidateVO {

    private Long nurseId;
    private String nurseCode;
    private String nurseName;
    private String gender;
    private String phone;
    private BigDecimal averageRating;
    private Integer completedOrdersCount;
    private Integer travelTimeMinutes;
    private BigDecimal distanceKm;
    private String qualificationSummary;
    private String serviceAreas;
    private boolean qualified;
    private String disqualifyReason;
    private Integer dailyRemainingSlots;
    private LocalDateTime nearestAvailableTime;
}
