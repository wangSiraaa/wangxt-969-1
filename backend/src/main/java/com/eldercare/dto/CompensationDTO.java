package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompensationDTO {

    private Long workOrderId;

    private Long abnormalEventId;

    private Long elderId;

    private String compensationType;

    private BigDecimal compensationAmount;

    private String compensationDescription;

    private String appliedBy;

    private String approvedBy;

    private String remark;
}
