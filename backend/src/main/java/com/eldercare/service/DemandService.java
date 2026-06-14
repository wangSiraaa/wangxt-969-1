package com.eldercare.service;

import com.eldercare.common.enums.ElderStatus;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.common.enums.RiskLevel;
import com.eldercare.dto.DemandCreateDTO;
import com.eldercare.entity.*;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemandService {

    private final ServiceDemandRepository demandRepository;
    private final DemandVersionRepository demandVersionRepository;
    private final ElderRepository elderRepository;
    private final NursingLevelRepository nursingLevelRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public ServiceDemand createDemand(DemandCreateDTO dto) {
        Elder elder = elderRepository.findById(dto.getElderId())
                .orElseThrow(() -> new IllegalArgumentException("老人不存在: " + dto.getElderId()));

        if (elder.getStatus() == ElderStatus.INACTIVE || elder.getStatus() == ElderStatus.DECEASED) {
            throw new IllegalStateException("老人状态不允许创建需求: " + elder.getStatus());
        }

        ServiceDemand demand = new ServiceDemand();
        demand.setDemandCode("DEM" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        demand.setElderId(dto.getElderId());
        demand.setElderName(elder.getName());
        demand.setServicePackageId(dto.getServicePackageId());
        demand.setServiceType(dto.getServiceType());
        demand.setNursingLevelCode(dto.getNursingLevelCode());
        demand.setRequestedDate(dto.getRequestedDate());
        demand.setRequestedStartTime(dto.getRequestedStartTime());
        demand.setRequestedEndTime(dto.getRequestedEndTime());
        demand.setAddress(dto.getAddress() != null ? dto.getAddress() : elder.getAddress());
        demand.setLongitude(dto.getLongitude() != null ? dto.getLongitude() : elder.getLongitude());
        demand.setLatitude(dto.getLatitude() != null ? dto.getLatitude() : elder.getLatitude());
        demand.setDescription(dto.getDescription());
        demand.setSpecialRequirements(dto.getSpecialRequirements());
        demand.setContraindicationIds(dto.getContraindicationIds() != null
                ? dto.getContraindicationIds() : elder.getContraindicationIds());
        demand.setRiskLevel(dto.getRiskLevel() != null ? dto.getRiskLevel() : elder.getRiskLevel());
        demand.setCurrentVersion(1);
        demand.setStatus("PENDING_DISPATCH");
        demand.setSubmittedAt(LocalDateTime.now());
        demand.setSubmittedBy(dto.getSubmittedBy() != null ? dto.getSubmittedBy() : "FAMILY");
        demand.setRemark(dto.getRemark());

        if (dto.getServicePackageId() != null) {
            Optional<ServicePackage> spOpt = servicePackageRepository.findById(dto.getServicePackageId());
            if (spOpt.isPresent()) {
                ServicePackage sp = spOpt.get();
                demand.setServicePackageName(sp.getName());
                if (demand.getServiceType() == null) {
                    demand.setServiceType(sp.getServiceType());
                }
                if (demand.getRiskLevel() == null) {
                    demand.setRiskLevel(sp.getRiskLevel());
                }
            }
        }

        if (elder.getNursingLevelId() != null && demand.getNursingLevelCode() == null) {
            Optional<NursingLevel> nlOpt = nursingLevelRepository.findById(elder.getNursingLevelId());
            if (nlOpt.isPresent()) {
                demand.setNursingLevelCode(nlOpt.get().getCode());
            }
        }

        demand = demandRepository.save(demand);
        saveDemandVersion(demand);
        auditLogService.logDemand(demand.getId(), demand.getDemandCode(),
                "CREATE_DEMAND", demand.getSubmittedBy(), null, demand.toString());
        log.info("服务需求创建成功: demandCode={}, elder={}", demand.getDemandCode(), elder.getName());
        return demand;
    }

    @Transactional
    public ServiceDemand updateDemand(Long demandId, DemandCreateDTO dto) {
        ServiceDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("需求不存在: " + demandId));

        String before = demand.toString();

        Optional<WorkOrder> existingOrder = workOrderRepository.findByDemandId(demandId);
        if (existingOrder.isPresent()
                && existingOrder.get().getStatus() != OrderStatus.CANCELLED
                && existingOrder.get().getStatus() != OrderStatus.ABNORMAL) {
            throw new IllegalStateException("需求已派单生成工单，不允许修改。如需修改请先取消工单或通过补单流程。");
        }

        if (dto.getRequestedDate() != null) demand.setRequestedDate(dto.getRequestedDate());
        if (dto.getRequestedStartTime() != null) demand.setRequestedStartTime(dto.getRequestedStartTime());
        if (dto.getRequestedEndTime() != null) demand.setRequestedEndTime(dto.getRequestedEndTime());
        if (dto.getServicePackageId() != null) {
            demand.setServicePackageId(dto.getServicePackageId());
            Optional<ServicePackage> spOpt = servicePackageRepository.findById(dto.getServicePackageId());
            if (spOpt.isPresent()) {
                demand.setServicePackageName(spOpt.get().getName());
            }
        }
        if (dto.getAddress() != null) demand.setAddress(dto.getAddress());
        if (dto.getDescription() != null) demand.setDescription(dto.getDescription());
        if (dto.getSpecialRequirements() != null) demand.setSpecialRequirements(dto.getSpecialRequirements());
        if (dto.getRiskLevel() != null) demand.setRiskLevel(dto.getRiskLevel());
        if (dto.getRemark() != null) demand.setRemark(dto.getRemark());

        demand.setCurrentVersion(demand.getCurrentVersion() + 1);
        demand = demandRepository.save(demand);
        saveDemandVersion(demand);
        auditLogService.logDemand(demand.getId(), demand.getDemandCode(),
                "UPDATE_DEMAND", dto.getSubmittedBy() != null ? dto.getSubmittedBy() : "SYSTEM",
                before, demand.toString());
        log.info("服务需求更新成功: demandCode={}, version={}", demand.getDemandCode(), demand.getCurrentVersion());
        return demand;
    }

    @Transactional
    public ServiceDemand cancelDemand(Long demandId, String reason, String operator) {
        ServiceDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("需求不存在: " + demandId));

        String before = demand.toString();
        demand.setStatus("CANCELLED");
        demand.setCancelledAt(LocalDateTime.now());
        demand.setCancelReason(reason);
        demand = demandRepository.save(demand);

        workOrderRepository.findByDemandId(demandId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING_DISPATCH
                    || order.getStatus() == OrderStatus.DISPATCHED
                    || order.getStatus() == OrderStatus.NURSE_ACCEPTED) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelledAt(LocalDateTime.now());
                order.setCancelReason("需求取消: " + reason);
                workOrderRepository.save(order);
                auditLogService.logWorkOrder(order.getId(), order.getOrderCode(),
                        "CANCEL_ORDER", operator, null, order.toString());
            }
        });

        auditLogService.logDemand(demand.getId(), demand.getDemandCode(),
                "CANCEL_DEMAND", operator, before, demand.toString());
        log.info("服务需求已取消: demandCode={}, reason={}", demand.getDemandCode(), reason);
        return demand;
    }

    private void saveDemandVersion(ServiceDemand demand) {
        DemandVersion version = new DemandVersion();
        version.setDemandId(demand.getId());
        version.setDemandCode(demand.getDemandCode());
        version.setVersion(demand.getCurrentVersion());
        version.setElderId(demand.getElderId());
        version.setServicePackageId(demand.getServicePackageId());
        version.setServicePackageName(demand.getServicePackageName());
        version.setServiceType(demand.getServiceType());
        version.setRequestedDate(demand.getRequestedDate());
        version.setRequestedStartTime(demand.getRequestedStartTime());
        version.setRequestedEndTime(demand.getRequestedEndTime());
        version.setAddress(demand.getAddress());
        version.setLongitude(demand.getLongitude());
        version.setLatitude(demand.getLatitude());
        version.setDescription(demand.getDescription());
        version.setSpecialRequirements(demand.getSpecialRequirements());
        version.setContraindicationIds(demand.getContraindicationIds());
        version.setRiskLevel(demand.getRiskLevel());
        version.setVersionData(demand.toString());
        demandVersionRepository.save(version);
    }

    public Optional<ServiceDemand> findById(Long id) {
        return demandRepository.findById(id);
    }

    public Optional<ServiceDemand> findByCode(String code) {
        return demandRepository.findByDemandCode(code);
    }

    public List<ServiceDemand> findByElder(Long elderId) {
        return demandRepository.findByElderIdOrderBySubmittedAtDesc(elderId);
    }

    public List<ServiceDemand> findPendingDispatch() {
        return demandRepository.findByStatusOrderBySubmittedAtAsc("PENDING_DISPATCH");
    }

    public List<ServiceDemand> findByStatus(String status) {
        return demandRepository.findByStatusOrderBySubmittedAtDesc(status);
    }

    public Page<ServiceDemand> findAll(Pageable pageable) {
        return demandRepository.findAllByOrderBySubmittedAtDesc(pageable);
    }

    public List<DemandVersion> getDemandVersions(Long demandId) {
        return demandVersionRepository.findByDemandIdOrderByVersionDesc(demandId);
    }

    public long countByStatus(String status) {
        return demandRepository.countByStatus(status);
    }

    public long countAll() {
        return demandRepository.count();
    }
}
