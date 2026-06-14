package com.eldercare.dto;

import com.eldercare.common.enums.AbnormalStatus;
import com.eldercare.common.enums.AbnormalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalEventVO {

    private Long id;
    private String eventCode;
    private Long workOrderId;
    private String orderCode;
    private Long elderId;
    private String elderName;
    private Long nurseId;
    private String nurseName;
    private AbnormalType abnormalType;
    private AbnormalStatus status;
    private String description;
    private String occurredAt;
    private String detectedAt;
    private Long handlerId;
    private String handlerName;
    private String handlingNotes;
    private String resolvedAt;
    private String resolution;
    private String severity;
    private Boolean autoDetected;
}
