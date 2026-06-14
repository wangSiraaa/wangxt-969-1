package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchCreateDTO {

    private Long demandId;

    private Long elderId;

    private Long nurseId;

    private String remark;

    private String dispatchedBy;
}
