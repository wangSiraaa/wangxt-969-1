package com.eldercare.repository;

import com.eldercare.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Optional<CheckIn> findByWorkOrderId(Long workOrderId);

    List<CheckIn> findByNurseId(Long nurseId);

    @Query("SELECT c FROM CheckIn c WHERE c.nurseId = :nurseId AND c.checkInTime BETWEEN :start AND :end")
    List<CheckIn> findByNurseIdAndCheckInTimeBetween(
            @Param("nurseId") Long nurseId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(c) FROM CheckIn c WHERE c.nurseId = :nurseId AND FUNCTION('DATE', c.checkInTime) = :date")
    Integer countByNurseIdAndDate(@Param("nurseId") Long nurseId, @Param("date") LocalDate date);

    @Query("SELECT c FROM CheckIn c WHERE c.checkOutTime IS NULL AND c.checkInTime IS NOT NULL AND c.status = 'CHECKED_IN'")
    List<CheckIn> findPendingCheckOut();
}
