package com.eldercare.service;

import com.eldercare.entity.Elder;
import com.eldercare.entity.ServiceDemand;
import com.eldercare.enums.DemandStatus;
import com.eldercare.enums.ElderStatus;
import com.eldercare.repository.ElderRepository;
import com.eldercare.repository.ServiceDemandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceDemandService {
    private final ServiceDemandRepository demandRepository;
    private final ElderRepository elderRepository;

    public ServiceDemandService(ServiceDemandRepository demandRepository, ElderRepository elderRepository) {
        this.demandRepository = demandRepository;
        this.elderRepository = elderRepository;
    }

    public List<ServiceDemand> findAll() {
        List<ServiceDemand> list = demandRepository.findAll();
        list.forEach(this::fillElderName);
        return list;
    }

    public Optional<ServiceDemand> findById(Long id) {
        Optional<ServiceDemand> opt = demandRepository.findById(id);
        opt.ifPresent(this::fillElderName);
        return opt;
    }

    public List<ServiceDemand> findByStatus(DemandStatus status) {
        List<ServiceDemand> list = demandRepository.findByStatus(status);
        list.forEach(this::fillElderName);
        return list;
    }

    public List<ServiceDemand> findByElderId(Long elderId) {
        List<ServiceDemand> list = demandRepository.findByElderId(elderId);
        list.forEach(this::fillElderName);
        return list;
    }

    public List<ServiceDemand> findByStatusIn(List<DemandStatus> statuses) {
        List<ServiceDemand> list = demandRepository.findByStatusIn(statuses);
        list.forEach(this::fillElderName);
        return list;
    }

    private void fillElderName(ServiceDemand demand) {
        if (demand.getElderId() != null) {
            elderRepository.findById(demand.getElderId())
                    .ifPresent(elder -> demand.setElderName(elder.getName()));
        }
    }

    @Transactional
    public ServiceDemand save(ServiceDemand demand) {
        Elder elder = elderRepository.findById(demand.getElderId())
                .orElseThrow(() -> new RuntimeException("老人信息不存在"));

        if (ElderStatus.SUSPENDED.equals(elder.getStatus())) {
            throw new RuntimeException("该老人服务已暂停，无法提交需求");
        }

        if (demand.getStatus() == null) {
            demand.setStatus(DemandStatus.PENDING_DISPATCH);
        }

        if (demand.getExpectedTime() == null) {
            demand.setExpectedTime(LocalDateTime.now().plusDays(1));
        }

        if (demand.getEstimatedAmount() == null) {
            demand.setEstimatedAmount(BigDecimal.ZERO);
        }

        ServiceDemand saved = demandRepository.save(demand);
        fillElderName(saved);
        return saved;
    }

    @Transactional
    public ServiceDemand update(Long id, ServiceDemand demand) {
        return demandRepository.findById(id).map(existing -> {
            existing.setServiceType(demand.getServiceType());
            existing.setServiceContent(demand.getServiceContent());
            existing.setExpectedTime(demand.getExpectedTime());
            existing.setSpecialRequirement(demand.getSpecialRequirement());
            existing.setEstimatedAmount(demand.getEstimatedAmount());
            existing.setRequiredQualifications(demand.getRequiredQualifications());
            existing.setApplicant(demand.getApplicant());
            existing.setApplicantPhone(demand.getApplicantPhone());
            existing.setRelation(demand.getRelation());
            ServiceDemand saved = demandRepository.save(existing);
            fillElderName(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("服务需求不存在"));
    }

    @Transactional
    public ServiceDemand updateStatus(Long id, DemandStatus status) {
        return demandRepository.findById(id).map(demand -> {
            demand.setStatus(status);
            ServiceDemand saved = demandRepository.save(demand);
            fillElderName(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("服务需求不存在"));
    }

    @Transactional
    public ServiceDemand cancel(Long id, String reason) {
        return demandRepository.findById(id).map(demand -> {
            demand.setStatus(DemandStatus.CANCELLED);
            demand.setCancelReason(reason);
            ServiceDemand saved = demandRepository.save(demand);
            fillElderName(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("服务需求不存在"));
    }

    @Transactional
    public void delete(Long id) {
        demandRepository.deleteById(id);
    }
}
