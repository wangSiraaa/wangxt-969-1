package com.eldercare.enums;

public enum DemandStatus {
    PENDING_DISPATCH("待派单"),
    DISPATCHED("已派单"),
    IN_SERVICE("服务中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;

    DemandStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
