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
@Table(name = "contraindication")
public class Contraindication extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "severity")
    private String severity;

    @Column(name = "description")
    private String description;

    @Column(name = "handling_notes", columnDefinition = "TEXT")
    private String handlingNotes;

    @Column(name = "excluded_qualifications")
    private String excludedQualifications;

    @Column(name = "enabled")
    private Boolean enabled = true;
}
