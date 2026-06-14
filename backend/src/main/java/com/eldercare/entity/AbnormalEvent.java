package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import com.eldercare.common.enums.AbnormalStatus;
import com.eldercare.common.enums.AbnormalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "abnormal_event")
public class AbnormalEvent extends BaseEntity {

    @Column(name = "event_code", unique = true, nullable = false)
    private String eventCode;

    @Column(name = "work_order_id")
    private Long workOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "elder_id")
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "nurse_id")
    private Long nurseId;

    @Column(name = "nurse_name")
    private String nurseName;

    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_type")
    private AbnormalType abnormalType;

    @Enumerated(EnumType.STRING)
    private AbnormalStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "detected_by")
    private String detectedBy;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "handler_name")
    private String handlerName;

    @Column(name = "handling_notes", columnDefinition = "TEXT")
    private String handlingNotes;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    private String severity;

    @Column(name = "auto_detected")
    private Boolean autoDetected = false;
}
