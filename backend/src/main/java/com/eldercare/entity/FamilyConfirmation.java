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
@Table(name = "family_confirmation")
public class FamilyConfirmation extends BaseEntity {

    @Column(name = "work_order_id", nullable = false)
    private Long workOrderId;

    @Column(name = "order_code")
    private String orderCode;

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "elder_name")
    private String elderName;

    @Column(name = "nurse_id")
    private Long nurseId;

    @Column(name = "nurse_name")
    private String nurseName;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_relation")
    private String contactRelation;

    @Column(name = "family_contact_id")
    private Long familyContactId;

    @Column(name = "confirmed")
    private Boolean confirmed;

    @Column(name = "confirmed_by")
    private String confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirm_remark", columnDefinition = "TEXT")
    private String confirmRemark;

    @Column(name = "rejected")
    private Boolean rejected;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    private String status;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;
}
