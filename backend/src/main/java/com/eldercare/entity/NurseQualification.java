package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import com.eldercare.common.enums.QualificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nurse_qualification")
public class NurseQualification extends BaseEntity {

    @Column(name = "nurse_id", nullable = false)
    private Long nurseId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "certificate_no")
    private String certificateNo;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_authority")
    private String issuingAuthority;

    @Column(name = "level")
    private String level;

    @Column(name = "service_type_scope")
    private String serviceTypeScope;

    @Column(name = "nursing_level_scope")
    private String nursingLevelScope;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private QualificationStatus status;

    @Column(name = "verified")
    private Boolean verified = false;

    @Column(name = "verified_by")
    private String verifiedBy;
}
