package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementCreateDTO {

    private Long workOrderId;

    private BigDecimal extraAmount;

    private BigDecimal deductAmount;

    private Boolean insuranceCovered;

    private String settledBy;

    private String remark;
}
