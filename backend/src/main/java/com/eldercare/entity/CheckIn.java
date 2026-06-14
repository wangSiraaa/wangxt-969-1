package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "check_in")
public class CheckIn extends BaseEntity {

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "nurse_id", nullable = false)
    private Long nurseId;

    @Column(name = "nurse_name")
    private String nurseName;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_in_longitude", precision = 12, scale = 8)
    private BigDecimal checkInLongitude;

    @Column(name = "check_in_latitude", precision = 12, scale = 8)
    private BigDecimal checkInLatitude;

    @Column(name = "check_in_address")
    private String checkInAddress;

    @Column(name = "check_in_photo_url")
    private String checkInPhotoUrl;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_out_longitude", precision = 12, scale = 8)
    private BigDecimal checkOutLongitude;

    @Column(name = "check_out_latitude", precision = 12, scale = 8)
    private BigDecimal checkOutLatitude;

    @Column(name = "check_out_address")
    private String checkOutAddress;

    @Column(name = "check_out_photo_url")
    private String checkOutPhotoUrl;

    @Column(name = "service_duration_minutes")
    private Integer serviceDurationMinutes;

    @Column(name = "status")
    private String status;
}
