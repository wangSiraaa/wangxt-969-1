package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emergency_contact")
public class EmergencyContact extends BaseEntity {

    @Column(name = "elder_id", nullable = false)
    private Long elderId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "relation")
    private String relation;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "alt_phone")
    private String altPhone;

    @Column(name = "address")
    private String address;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "can_confirm_service")
    private Boolean canConfirmService = false;

    @Column(name = "notify_on_abnormal")
    private Boolean notifyOnAbnormal = false;
}
