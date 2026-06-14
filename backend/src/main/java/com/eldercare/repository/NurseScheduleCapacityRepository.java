package com.eldercare.repository;

import com.eldercare.common.enums.DayOfWeek;
import com.eldercare.entity.NurseScheduleCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NurseScheduleCapacityRepository extends JpaRepository<NurseScheduleCapacity, Long> {

    List<NurseScheduleCapacity> findByNurseId(Long nurseId);

    @Query("SELECT n FROM NurseScheduleCapacity n WHERE n.nurseId = :nurseId AND n.dayOfWeek = :dayOfWeek AND n.enabled = true")
    List<NurseScheduleCapacity> findByNurseIdAndDayOfWeek(
            @Param("nurseId") Long nurseId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
