package com.eldercare.repository;

import com.eldercare.entity.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaregiverRepository extends JpaRepository<Caregiver, Long> {
    List<Caregiver> findByActiveTrue();
    List<Caregiver> findByNameContaining(String name);
}
