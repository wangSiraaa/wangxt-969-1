package com.eldercare.service;

import com.eldercare.common.enums.OrderStatus;
import com.eldercare.dto.SupplementOrderDTO;
import com.eldercare.entity.ServiceDemand;
import com.eldercare.entity.SupplementOrder;
import com.eldercare.entity.WorkOrder;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplementOrderService {

    private final SupplementOrderRepository supplementOrderRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ServiceDemandRepository demandRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public SupplementOrder applySupplement(SupplementOrderDTO dto) {
        WorkOrder originalOrder = dto.getOriginalWorkOrderId() != null
                ? workOrderRepository.findById(dto.getOriginalWorkOrderId()).orElse(null) : null;
        ServiceDemand demand = dto.getDemandId() != null
                ? demandRepository.findById(dto.getDemandId()).orElse(null) : null;

        SupplementOrder order = new SupplementOrder();
        order.setSupplementCode("SUP" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        order.setOriginalWorkOrderId(dto.getOriginalWorkOrderId());
        order.setOriginalOrderCode(originalOrder != null ? originalOrder.getOrderCode() : null);
        order.setDemandId(dto.getDemandId());
        order.setDemandCode(demand != null ? demand.getDemandCode() : null);
        order.setNurseId(dto.getNurseId());
        order.setElderId(demand != null ? demand.getElderId()
                : (originalOrder != null ? originalOrder.getElderId() : null));
        order.setElderName(demand != null ? demand.getElderName()
                : (originalOrder != null ? originalOrder.getElderName() : null));
        order.setSupplementReason(dto.getSupplementReason());
        order.setSupplementDescription(dto.getSupplementDescription());
        order.setApprovalStatus("PENDING");
        order.setAppliedBy(dto.getAppliedBy());
        order.setAppliedAt(LocalDateTime.now());
        order.setRemark(dto.getRemark());
        order = supplementOrderRepository.save(order);

        auditLogService.logWorkOrder(order.getId(), order.getSupplementCode(),
                "APPLY_SUPPLEMENT", dto.getAppliedBy(), null, order.toString());
        log.info("补单申请创建: supplementCode={}, reason={}", order.getSupplementCode(), dto.getSupplementReason());
        return order;
    }

    @Transactional
    public SupplementOrder approveSupplement(Long id, boolean approved, String approver, String approvalRemark) {
        SupplementOrder order = supplementOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("补单申请不存在: " + id));

        String before = order.toString();
        order.setApprovalStatus(approved ? "APPROVED" : "REJECTED");
        order.setApprovedBy(approver);
        order.setApprovedAt(LocalDateTime.now());
        order.setApprovalRemark(approvalRemark);

        if (approved && order.getDemandId() != null) {
            demandRepository.findById(order.getDemandId()).ifPresent(demand -> {
                demand.setStatus("PENDING_DISPATCH");
                demandRepository.save(demand);
            });
        }

        order = supplementOrderRepository.save(order);
        auditLogService.logWorkOrder(order.getId(), order.getSupplementCode(),
                approved ? "APPROVE_SUPPLEMENT" : "REJECT_SUPPLEMENT", approver, before, order.toString());
        log.info("补单审批结果: supplementCode={}, approved={}", order.getSupplementCode(), approved);
        return order;
    }

    public Optional<SupplementOrder> findById(Long id) {
        return supplementOrderRepository.findById(id);
    }

    public List<SupplementOrder> findByApprovalStatus(String status) {
        return supplementOrderRepository.findByApprovalStatus(status);
    }

    public List<SupplementOrder> findByOriginalWorkOrder(Long workOrderId) {
        return supplementOrderRepository.findByOriginalWorkOrderId(workOrderId);
    }
}
