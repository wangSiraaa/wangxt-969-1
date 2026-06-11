package com.eldercare.enums;

public enum ElderStatus {
    ACTIVE("正常"),
    SUSPENDED("暂停服务"),
    INACTIVE("已停用");

    private final String description;

    ElderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
