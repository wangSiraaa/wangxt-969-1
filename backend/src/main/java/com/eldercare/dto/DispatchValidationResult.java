package com.eldercare.dto;

import com.eldercare.common.enums.AbnormalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchValidationResult {

    private boolean valid = true;

    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();

    @Builder.Default
    private List<AbnormalType> abnormalTypes = new ArrayList<>();

    private String nurseName;

    private String elderName;

    private String orderCode;

    public void addError(String message, AbnormalType type) {
        this.valid = false;
        this.errorMessages.add(message);
        if (type != null && !this.abnormalTypes.contains(type)) {
            this.abnormalTypes.add(type);
        }
    }
}
