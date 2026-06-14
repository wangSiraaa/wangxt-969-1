package com.eldercare.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult implements Serializable {

    private boolean valid = true;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private Object extra;

    public void addError(String error) {
        this.valid = false;
        this.errors.add(error);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public static ValidationResult valid() {
        return new ValidationResult(true, new ArrayList<>(), new ArrayList<>(), null);
    }

    public static ValidationResult invalid(String error) {
        ValidationResult result = new ValidationResult();
        result.addError(error);
        return result;
    }
}
