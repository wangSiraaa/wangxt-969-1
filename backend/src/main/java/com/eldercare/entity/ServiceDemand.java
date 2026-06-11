package com.eldercare.entity;

import com.eldercare.enums.DemandStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_demand")
public class ServiceDemand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long elderId;

    @Transient
    private String elderName;

    @Column(length = 100)
    private String applicant;

    @Column(length = 20)
    private String applicantPhone;

    @Column(length = 50)
    private String relation;

    @Column(nullable = false, length = 100)
    private String serviceType;

    @Column(nullable = false, length = 500)
    private String serviceContent;

    @Column(nullable = false)
    private LocalDateTime expectedTime;

    @Column(length = 200)
    private String specialRequirement;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedAmount;

    @Column(length = 100)
    private String requiredQualifications;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DemandStatus status = DemandStatus.PENDING_DISPATCH;

    @Column(length = 500)
    private String cancelReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getElderId() { return elderId; }
    public void setElderId(Long elderId) { this.elderId = elderId; }
    public String getElderName() { return elderName; }
    public void setElderName(String elderName) { this.elderName = elderName; }
    public String getApplicant() { return applicant; }
    public void setApplicant(String applicant) { this.applicant = applicant; }
    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }
    public String getRelation() { return relation; }
    public void setRelation(String relation) { this.relation = relation; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getServiceContent() { return serviceContent; }
    public void setServiceContent(String serviceContent) { this.serviceContent = serviceContent; }
    public LocalDateTime getExpectedTime() { return expectedTime; }
    public void setExpectedTime(LocalDateTime expectedTime) { this.expectedTime = expectedTime; }
    public String getSpecialRequirement() { return specialRequirement; }
    public void setSpecialRequirement(String specialRequirement) { this.specialRequirement = specialRequirement; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public String getRequiredQualifications() { return requiredQualifications; }
    public void setRequiredQualifications(String requiredQualifications) { this.requiredQualifications = requiredQualifications; }
    public DemandStatus getStatus() { return status; }
    public void setStatus(DemandStatus status) { this.status = status; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
