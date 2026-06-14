package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyConfirmDTO {

    private Long workOrderId;

    private Long elderId;

    private Long familyContactId;

    private Boolean confirmed;

    private String confirmRemark;

    private String rejectReason;

    private String confirmedBy;
}
