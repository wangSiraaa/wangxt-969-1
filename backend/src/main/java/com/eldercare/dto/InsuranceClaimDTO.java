package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaimDTO {

    private Long settlementId;

    private Long workOrderId;

    private Long elderId;

    private String insurantNo;

    private String insuranceType;

    private BigDecimal claimAmount;

    private String claimDescription;

    private String claimMaterials;

    private String appliedBy;

    private String remark;
}
