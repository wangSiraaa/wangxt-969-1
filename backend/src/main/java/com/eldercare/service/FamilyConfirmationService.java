package com.eldercare.service;

import com.eldercare.common.enums.AbnormalType;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.dto.FamilyConfirmDTO;
import com.eldercare.entity.FamilyConfirmation;
import com.eldercare.entity.WorkOrder;
import com.eldercare.repository.FamilyConfirmationRepository;
import com.eldercare.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyConfirmationService {

    private final FamilyConfirmationRepository confirmationRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AbnormalEventService abnormalEventService;
    private final AuditLogService auditLogService;

    @Transactional
    public FamilyConfirmation confirmService(FamilyConfirmDTO dto) {
        WorkOrder order = workOrderRepository.findById(dto.getWorkOrderId())
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + dto.getWorkOrderId()));

        if (order.getStatus() != OrderStatus.PENDING_FAMILY_CONFIRM
                && order.getStatus() != OrderStatus.FAMILY_REJECTED
                && order.getStatus() != OrderStatus.SERVICE_COMPLETED) {
            throw new IllegalStateException(
                    "当前工单状态【" + order.getStatus() + "】不允许家属确认。"
                            + "允许确认的状态：SERVICE_COMPLETED / PENDING_FAMILY_CONFIRM / FAMILY_REJECTED");
        }

        FamilyConfirmation confirmation = confirmationRepository.findByWorkOrderId(order.getId())
                .orElseGet(() -> {
                    FamilyConfirmation fc = new FamilyConfirmation();
                    fc.setWorkOrderId(order.getId());
                    fc.setOrderCode(order.getOrderCode());
                    fc.setElderId(order.getElderId());
                    fc.setElderName(order.getElderName());
                    fc.setNurseId(order.getNurseId());
                    fc.setNurseName(order.getNurseName());
                    return fc;
                });

        String before = confirmation.toString();
        confirmation.setFamilyContactId(dto.getFamilyContactId());
        confirmation.setConfirmed(dto.getConfirmed());
        confirmation.setConfirmedBy(dto.getConfirmedBy());
        confirmation.setConfirmedAt(LocalDateTime.now());
        confirmation.setConfirmRemark(dto.getConfirmRemark());
        confirmation.setRejectReason(dto.getRejectReason());

        String orderBefore = order.toString();
        if (Boolean.TRUE.equals(dto.getConfirmed())) {
            confirmation.setStatus("CONFIRMED");
            order.setStatus(OrderStatus.FAMILY_CONFIRMED);
        } else {
            confirmation.setStatus("REJECTED");
            order.setStatus(OrderStatus.FAMILY_REJECTED);
            abnormalEventService.createAbnormal(order.getId(), AbnormalType.FAMILY_REJECTED,
                    "家属拒绝确认服务。拒绝原因："
                            + (dto.getRejectReason() != null ? dto.getRejectReason() : "未填写"),
                    "HIGH", false);
        }

        confirmation = confirmationRepository.save(confirmation);
        workOrderRepository.save(order);

        auditLogService.logWorkOrder(order.getId(), order.getOrderCode(),
                Boolean.TRUE.equals(dto.getConfirmed()) ? "FAMILY_CONFIRMED" : "FAMILY_REJECTED",
                dto.getConfirmedBy(), orderBefore, order.toString());

        log.info("家属确认完成: orderCode={}, confirmed={}, by={}",
                order.getOrderCode(), dto.getConfirmed(), dto.getConfirmedBy());
        return confirmation;
    }

    public Optional<FamilyConfirmation> findByWorkOrder(Long workOrderId) {
        return confirmationRepository.findByWorkOrderId(workOrderId);
    }

    public Optional<FamilyConfirmation> findById(Long id) {
        return confirmationRepository.findById(id);
    }
}
