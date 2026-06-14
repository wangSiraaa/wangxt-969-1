package com.eldercare.entity;

import com.eldercare.common.entity.BaseEntity;
import com.eldercare.common.enums.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nurse_schedule_capacity")
public class NurseScheduleCapacity extends BaseEntity {

    @Column(name = "nurse_id", nullable = false)
    private Long nurseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "max_orders")
    private Integer maxOrders;

    @Column(name = "max_hours")
    private Integer maxHours;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "remark")
    private String remark;
}
