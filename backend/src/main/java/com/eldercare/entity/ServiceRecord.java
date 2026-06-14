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
@Table(name = "service_record")
public class ServiceRecord extends BaseEntity {

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "nurse_id", nullable = false)
    private Long nurseId;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "service_content", columnDefinition = "TEXT")
    private String serviceContent;

    @Column(name = "health_observation", columnDefinition = "TEXT")
    private String healthObservation;

    @Column(name = "medication_administered")
    private String medicationAdministered;

    @Column(name = "vital_signs")
    private String vitalSigns;

    @Column(name = "service_photos")
    private String servicePhotos;

    @Column(name = "abnormal_situation", columnDefinition = "TEXT")
    private String abnormalSituation;

    @Column(name = "risk_event_occurred")
    private Boolean riskEventOccurred = false;

    @Column(name = "risk_event_detail", columnDefinition = "TEXT")
    private String riskEventDetail;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
}
