package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    private String operator;

    @Column(name = "operator_role")
    private String operatorRole;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String ip;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "operation")
    private String operation;

    @Column(name = "module")
    private String module;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "business_code")
    private String businessCode;

    @Column(name = "before_data", columnDefinition = "TEXT")
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "TEXT")
    private String afterData;

    @Column(name = "operated_at")
    private LocalDateTime operatedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
