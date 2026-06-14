package com.eldercare.service;

import com.eldercare.common.enums.ElderStatus;
import com.eldercare.common.enums.NurseStatus;
import com.eldercare.entity.*;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final ElderRepository elderRepository;
    private final NurseRepository nurseRepository;
    private final NursingLevelRepository nursingLevelRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final ContraindicationRepository contraindicationRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final NurseQualificationRepository nurseQualificationRepository;

    public Page<Elder> findAllElders(Pageable pageable) {
        return elderRepository.findAll(pageable);
    }

    public List<Elder> findActiveElders() {
        return elderRepository.findByStatus(ElderStatus.ACTIVE);
    }

    public Optional<Elder> findElderById(Long id) {
        return elderRepository.findById(id);
    }

    public List<EmergencyContact> findEmergencyContacts(Long elderId) {
        return emergencyContactRepository.findByElderId(elderId);
    }

    public Page<Nurse> findAllNurses(Pageable pageable) {
        return nurseRepository.findAll(pageable);
    }

    public List<Nurse> findActiveNurses() {
        return nurseRepository.findByStatus(NurseStatus.ACTIVE);
    }

    public Optional<Nurse> findNurseById(Long id) {
        return nurseRepository.findById(id);
    }

    public List<NurseQualification> findNurseQualifications(Long nurseId) {
        return nurseQualificationRepository.findByNurseId(nurseId);
    }

    public List<NursingLevel> findAllNursingLevels() {
        return nursingLevelRepository.findByEnabledTrueOrderByIdAsc();
    }

    public Optional<NursingLevel> findNursingLevelById(Long id) {
        return nursingLevelRepository.findById(id);
    }

    public List<ServicePackage> findAllServicePackages() {
        return servicePackageRepository.findByEnabledTrueOrderByIdAsc();
    }

    public Optional<ServicePackage> findServicePackageById(Long id) {
        return servicePackageRepository.findById(id);
    }

    public List<Contraindication> findAllContraindications() {
        return contraindicationRepository.findByEnabledTrueOrderBySeverityDesc();
    }

    public Optional<Contraindication> findContraindicationById(Long id) {
        return contraindicationRepository.findById(id);
    }
}
