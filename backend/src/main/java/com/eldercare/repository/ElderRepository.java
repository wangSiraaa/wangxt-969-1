package com.eldercare.repository;

import com.eldercare.entity.Elder;
import com.eldercare.enums.ElderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElderRepository extends JpaRepository<Elder, Long> {
    List<Elder> findByStatus(ElderStatus status);
    List<Elder> findByNameContaining(String name);
}
