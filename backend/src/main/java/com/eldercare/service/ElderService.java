package com.eldercare.service;

import com.eldercare.entity.Elder;
import com.eldercare.enums.ElderStatus;
import com.eldercare.repository.ElderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ElderService {
    private final ElderRepository elderRepository;

    public ElderService(ElderRepository elderRepository) {
        this.elderRepository = elderRepository;
    }

    public List<Elder> findAll() {
        return elderRepository.findAll();
    }

    public Optional<Elder> findById(Long id) {
        return elderRepository.findById(id);
    }

    public List<Elder> findByStatus(ElderStatus status) {
        return elderRepository.findByStatus(status);
    }

    public List<Elder> searchByName(String name) {
        return elderRepository.findByNameContaining(name);
    }

    @Transactional
    public Elder save(Elder elder) {
        return elderRepository.save(elder);
    }

    @Transactional
    public Elder update(Long id, Elder elder) {
        return elderRepository.findById(id).map(existing -> {
            existing.setName(elder.getName());
            existing.setIdCard(elder.getIdCard());
            existing.setPhone(elder.getPhone());
            existing.setAddress(elder.getAddress());
            existing.setEmergencyContact(elder.getEmergencyContact());
            existing.setEmergencyPhone(elder.getEmergencyPhone());
            existing.setHealthCondition(elder.getHealthCondition());
            return elderRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("老人信息不存在"));
    }

    @Transactional
    public Elder suspendService(Long id, String reason) {
        return elderRepository.findById(id).map(elder -> {
            elder.setStatus(ElderStatus.SUSPENDED);
            elder.setSuspendReason(reason);
            elder.setSuspendTime(LocalDateTime.now());
            return elderRepository.save(elder);
        }).orElseThrow(() -> new RuntimeException("老人信息不存在"));
    }

    @Transactional
    public Elder resumeService(Long id) {
        return elderRepository.findById(id).map(elder -> {
            elder.setStatus(ElderStatus.ACTIVE);
            elder.setSuspendReason(null);
            elder.setSuspendTime(null);
            return elderRepository.save(elder);
        }).orElseThrow(() -> new RuntimeException("老人信息不存在"));
    }

    @Transactional
    public void delete(Long id) {
        elderRepository.deleteById(id);
    }

    public boolean isSuspended(Long elderId) {
        return elderRepository.findById(elderId)
                .map(elder -> ElderStatus.SUSPENDED.equals(elder.getStatus()))
                .orElse(false);
    }
}
