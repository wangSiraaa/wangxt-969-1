package com.eldercare.repository;

import com.eldercare.entity.NurseLeave;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NurseLeaveRepository extends JpaRepository<NurseLeave, Long> {

    List<NurseLeave> findByNurseIdOrderByStartDateDesc(Long nurseId);

    Page<NurseLeave> findByNurseId(Long nurseId, Pageable pageable);

    List<NurseLeave> findByStatusOrderByStartDateDesc(String status);

    List<NurseLeave> findByNurseIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long nurseId, LocalDate date1, LocalDate date2);

    List<NurseLeave> findByStartDateBetweenOrderByStartDateDesc(LocalDate startDate, LocalDate endDate);
}
