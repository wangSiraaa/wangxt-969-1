package com.eldercare.service;

import com.eldercare.entity.Caregiver;
import com.eldercare.repository.CaregiverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CaregiverService {
    private final CaregiverRepository caregiverRepository;

    public CaregiverService(CaregiverRepository caregiverRepository) {
        this.caregiverRepository = caregiverRepository;
    }

    public List<Caregiver> findAll() {
        return caregiverRepository.findAll();
    }

    public List<Caregiver> findActiveCaregivers() {
        return caregiverRepository.findByActiveTrue();
    }

    public Optional<Caregiver> findById(Long id) {
        return caregiverRepository.findById(id);
    }

    public List<Caregiver> searchByName(String name) {
        return caregiverRepository.findByNameContaining(name);
    }

    @Transactional
    public Caregiver save(Caregiver caregiver) {
        return caregiverRepository.save(caregiver);
    }

    @Transactional
    public Caregiver update(Long id, Caregiver caregiver) {
        return caregiverRepository.findById(id).map(existing -> {
            existing.setName(caregiver.getName());
            existing.setIdCard(caregiver.getIdCard());
            existing.setPhone(caregiver.getPhone());
            existing.setAddress(caregiver.getAddress());
            existing.setQualifications(caregiver.getQualifications());
            existing.setSkillLevel(caregiver.getSkillLevel());
            existing.setServiceTypes(caregiver.getServiceTypes());
            existing.setActive(caregiver.getActive());
            return caregiverRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("护理员信息不存在"));
    }

    @Transactional
    public void delete(Long id) {
        caregiverRepository.deleteById(id);
    }

    public boolean isQualificationMatched(Long caregiverId, String requiredQualifications) {
        if (requiredQualifications == null || requiredQualifications.isEmpty()) {
            return true;
        }
        Caregiver caregiver = caregiverRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("护理员不存在"));

        List<String> requiredList = Arrays.asList(requiredQualifications.split("[,，、;；]"))
                .stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        String caregiverQual = caregiver.getQualifications() != null ? caregiver.getQualifications() : "";
        String caregiverServiceTypes = caregiver.getServiceTypes() != null ? caregiver.getServiceTypes() : "";
        String allQual = caregiverQual + "," + caregiverServiceTypes;

        return requiredList.stream().allMatch(req ->
                allQual.contains(req) || allQual.toLowerCase().contains(req.toLowerCase())
        );
    }

    public List<Caregiver> findMatchedCaregivers(String requiredQualifications, String serviceType) {
        List<Caregiver> activeCaregivers = caregiverRepository.findByActiveTrue();

        if ((requiredQualifications == null || requiredQualifications.isEmpty())
                && (serviceType == null || serviceType.isEmpty())) {
            return activeCaregivers;
        }

        return activeCaregivers.stream()
                .filter(cg -> {
                    boolean typeMatch = true;
                    if (serviceType != null && !serviceType.isEmpty()) {
                        String types = cg.getServiceTypes() != null ? cg.getServiceTypes() : "";
                        typeMatch = types.contains(serviceType);
                    }
                    boolean qualMatch = true;
                    if (requiredQualifications != null && !requiredQualifications.isEmpty()) {
                        qualMatch = isQualificationMatched(cg.getId(), requiredQualifications);
                    }
                    return typeMatch && qualMatch;
                })
                .collect(Collectors.toList());
    }
}
