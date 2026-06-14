package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplementOrderDTO {

    private Long originalWorkOrderId;

    private Long demandId;

    private Long nurseId;

    private String supplementReason;

    private String supplementDescription;

    private String appliedBy;

    private String remark;
}
