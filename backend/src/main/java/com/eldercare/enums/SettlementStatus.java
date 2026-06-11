package com.eldercare.enums;

public enum SettlementStatus {
    PENDING("待结算"),
    SETTLED("已结算"),
    CANCELLED("已取消");

    private final String description;

    SettlementStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
