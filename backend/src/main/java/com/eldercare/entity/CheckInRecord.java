package com.eldercare.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_record")
public class CheckInRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long workOrderId;

    @Column(nullable = false)
    private Long caregiverId;

    @Transient
    private String caregiverName;

    @Column(nullable = false)
    private Long elderId;

    @Transient
    private String elderName;

    @Column(nullable = false)
    private LocalDateTime checkInTime;

    @Column(length = 200)
    private String checkInLocation;

    @Column(length = 20)
    private String checkInLatitude;

    @Column(length = 20)
    private String checkInLongitude;

    @Column(length = 500)
    private String checkInRemark;

    @Column(length = 500)
    private String signImageUrl;

    private LocalDateTime checkOutTime;

    @Column(length = 200)
    private String checkOutLocation;

    @Column(precision = 10, scale = 2)
    private BigDecimal serviceHours;

    @Column(length = 2000)
    private String serviceRecord;

    @Column(length = 500)
    private String elderCondition;

    @Column(length = 500)
    private String caregiverRemark;

    @Column(length = 500)
    private String elderFeedback;

    @Column(precision = 2)
    private Integer rating;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Long getCaregiverId() { return caregiverId; }
    public void setCaregiverId(Long caregiverId) { this.caregiverId = caregiverId; }
    public String getCaregiverName() { return caregiverName; }
    public void setCaregiverName(String caregiverName) { this.caregiverName = caregiverName; }
    public Long getElderId() { return elderId; }
    public void setElderId(Long elderId) { this.elderId = elderId; }
    public String getElderName() { return elderName; }
    public void setElderName(String elderName) { this.elderName = elderName; }
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    public String getCheckInLocation() { return checkInLocation; }
    public void setCheckInLocation(String checkInLocation) { this.checkInLocation = checkInLocation; }
    public String getCheckInLatitude() { return checkInLatitude; }
    public void setCheckInLatitude(String checkInLatitude) { this.checkInLatitude = checkInLatitude; }
    public String getCheckInLongitude() { return checkInLongitude; }
    public void setCheckInLongitude(String checkInLongitude) { this.checkInLongitude = checkInLongitude; }
    public String getCheckInRemark() { return checkInRemark; }
    public void setCheckInRemark(String checkInRemark) { this.checkInRemark = checkInRemark; }
    public String getSignImageUrl() { return signImageUrl; }
    public void setSignImageUrl(String signImageUrl) { this.signImageUrl = signImageUrl; }
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }
    public String getCheckOutLocation() { return checkOutLocation; }
    public void setCheckOutLocation(String checkOutLocation) { this.checkOutLocation = checkOutLocation; }
    public BigDecimal getServiceHours() { return serviceHours; }
    public void setServiceHours(BigDecimal serviceHours) { this.serviceHours = serviceHours; }
    public String getServiceRecord() { return serviceRecord; }
    public void setServiceRecord(String serviceRecord) { this.serviceRecord = serviceRecord; }
    public String getElderCondition() { return elderCondition; }
    public void setElderCondition(String elderCondition) { this.elderCondition = elderCondition; }
    public String getCaregiverRemark() { return caregiverRemark; }
    public void setCaregiverRemark(String caregiverRemark) { this.caregiverRemark = caregiverRemark; }
    public String getElderFeedback() { return elderFeedback; }
    public void setElderFeedback(String elderFeedback) { this.elderFeedback = elderFeedback; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
