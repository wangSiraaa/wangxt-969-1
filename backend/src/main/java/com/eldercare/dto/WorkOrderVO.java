package com.eldercare.dto;

import com.eldercare.common.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderVO {

    private Long id;
    private String orderCode;
    private Long demandId;
    private String demandCode;
    private Long elderId;
    private String elderName;
    private Long nurseId;
    private String nurseName;
    private Long servicePackageId;
    private String servicePackageName;
    private String serviceType;
    private String scheduledDate;
    private String scheduledStartTime;
    private String scheduledEndTime;
    private String address;
    private OrderStatus status;
    private String riskLevel;
    private String dispatchedAt;
    private String acceptedAt;
    private String startedAt;
    private String completedAt;
    private String remark;
    private String createdAt;
}
