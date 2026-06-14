package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInDTO {

    @NotNull(message = "工单ID不能为空")
    private Long workOrderId;

    @NotNull(message = "护理员ID不能为空")
    private Long nurseId;

    private LocalDateTime checkInTime;

    private BigDecimal checkInLongitude;

    private BigDecimal checkInLatitude;

    private String checkInAddress;

    private String checkInPhotoUrl;
}
