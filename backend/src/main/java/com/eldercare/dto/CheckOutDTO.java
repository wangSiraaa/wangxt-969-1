package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutDTO {

    private Long workOrderId;

    private Long nurseId;

    private LocalDateTime checkOutTime;

    private BigDecimal checkOutLongitude;

    private BigDecimal checkOutLatitude;

    private String checkOutAddress;

    private String checkOutPhotoUrl;

    private String serviceContent;

    private String healthObservation;

    private String medicationAdministered;

    private String vitalSigns;

    private String servicePhotos;

    private String abnormalSituation;

    private Boolean riskEventOccurred = false;

    private String riskEventDetail;
}
