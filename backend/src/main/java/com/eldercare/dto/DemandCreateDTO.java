package com.eldercare.dto;

import com.eldercare.common.enums.RiskLevel;
import com.eldercare.common.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandCreateDTO {

    @NotNull(message = "老人ID不能为空")
    private Long elderId;

    private Long servicePackageId;

    private ServiceType serviceType;

    private String nursingLevelCode;

    @NotNull(message = "服务日期不能为空")
    private LocalDate requestedDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime requestedStartTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime requestedEndTime;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String description;

    private String specialRequirements;

    private String contraindicationIds;

    private RiskLevel riskLevel;

    private String submittedBy;

    private String remark;
}
