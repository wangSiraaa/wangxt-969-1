package com.eldercare.service;

import com.eldercare.common.enums.AbnormalType;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.common.exception.BusinessException;
import com.eldercare.dto.DispatchCreateDTO;
import com.eldercare.dto.DispatchValidationResult;
import com.eldercare.dto.NurseCandidateVO;
import com.eldercare.entity.*;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final WorkOrderRepository workOrderRepository;
    private final ServiceDemandRepository demandRepository;
    private final ElderRepository elderRepository;
    private final NurseRepository nurseRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final DispatchValidationService validationService;
    private final AbnormalEventService abnormalEventService;
    private final AuditLogService auditLogService;

    @Transactional
    public WorkOrder dispatchWorkOrder(DispatchCreateDTO dto) {
        ServiceDemand demand = demandRepository.findById(dto.getDemandId())
                .orElseThrow(() -> new IllegalArgumentException("需求不存在: " + dto.getDemandId()));
        Elder elder = elderRepository.findById(demand.getElderId())
                .orElseThrow(() -> new IllegalArgumentException("老人不存在"));
        Nurse nurse = nurseRepository.findById(dto.getNurseId())
                .orElseThrow(() -> new IllegalArgumentException("护理员不存在"));

        LocalDate scheduleDate = demand.getRequestedDate();
        LocalTime startTime = demand.getRequestedStartTime();
        LocalTime endTime = demand.getRequestedEndTime();

        DispatchValidationResult validation = validationService.validateDispatch(
                demand.getElderId(), dto.getNurseId(),
                demand.getServicePackageId(),
                scheduleDate, startTime, endTime);
        validation.setElderName(elder.getName());
        validation.setNurseName(nurse.getName());

        if (!validation.isValid()) {
            StringBuilder errorMsg = new StringBuilder("派单校验失败：\n");
            for (int i = 0; i < validation.getErrorMessages().size(); i++) {
                errorMsg.append(i + 1).append(". ").append(validation.getErrorMessages().get(i)).append("\n");
            }

            for (AbnormalType type : validation.getAbnormalTypes()) {
                String severity = type == AbnormalType.ELDER_PAUSED
                        || type == AbnormalType.QUALIFICATION_MISMATCH
                        || type == AbnormalType.RISK_LEVEL_MISMATCH ? "CRITICAL" : "MEDIUM";
                abnormalEventService.createAbnormal(null, type,
                        "派单前置校验拦截: " + validation.getErrorMessages().get(0),
                        severity, true);
            }

            throw new BusinessException(errorMsg.toString(), validation.getErrorMessages(), 400);
        }

        Optional<WorkOrder> existingOrder = workOrderRepository.findByDemandId(dto.getDemandId());
        WorkOrder workOrder;
        boolean isReassign = false;
        String before = null;

        if (existingOrder.isPresent()) {
            workOrder = existingOrder.get();
            OrderStatus currentStatus = workOrder.getStatus();
            if (currentStatus != OrderStatus.CANCELLED
                    && currentStatus != OrderStatus.ABNORMAL
                    && currentStatus != OrderStatus.DISPATCHED
                    && currentStatus != OrderStatus.NURSE_ACCEPTED) {
                throw new IllegalStateException(
                        "工单当前状态【" + currentStatus + "】不允许改派。"
                                + "允许改派的状态：DISPATCHED/NURSE_ACCEPTED/CANCELLED/ABNORMAL");
            }
            before = workOrder.toString();
            isReassign = currentStatus == OrderStatus.DISPATCHED
                    || currentStatus == OrderStatus.NURSE_ACCEPTED
                    || currentStatus == OrderStatus.ABNORMAL;
        } else {
            workOrder = new WorkOrder();
            workOrder.setOrderCode("WO" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
            workOrder.setDemandId(demand.getId());
            workOrder.setDemandCode(demand.getDemandCode());
        }

        workOrder.setElderId(demand.getElderId());
        workOrder.setElderName(elder.getName());
        workOrder.setNurseId(dto.getNurseId());
        workOrder.setNurseName(nurse.getName());
        workOrder.setServicePackageId(demand.getServicePackageId());
        workOrder.setServicePackageName(demand.getServicePackageName());
        workOrder.setServiceType(demand.getServiceType());
        workOrder.setScheduledDate(scheduleDate);
        workOrder.setScheduledStartTime(startTime);
        workOrder.setScheduledEndTime(endTime);
        workOrder.setAddress(demand.getAddress());
        workOrder.setLongitude(demand.getLongitude());
        workOrder.setLatitude(demand.getLatitude());
        workOrder.setRiskLevel(demand.getRiskLevel());
        workOrder.setStatus(OrderStatus.DISPATCHED);
        workOrder.setDispatchedAt(LocalDateTime.now());
        workOrder.setDispatchedBy(dto.getDispatchedBy() != null ? dto.getDispatchedBy() : "DISPATCHER");
        workOrder.setRemark(dto.getRemark());

        workOrder = workOrderRepository.save(workOrder);

        demand.setStatus("DISPATCHED");
        demandRepository.save(demand);

        if (isReassign) {
            abnormalEventService.createAbnormal(workOrder.getId(), AbnormalType.TEMP_REASSIGN,
                    "工单已临时改派。原护理员信息在版本记录中。改派原因："
                            + (dto.getRemark() != null ? dto.getRemark() : "未填写"),
                    "MEDIUM", false);
            auditLogService.logWorkOrder(workOrder.getId(), workOrder.getOrderCode(),
                    "REASSIGN_ORDER", workOrder.getDispatchedBy(), before, workOrder.toString());
            log.info("工单改派成功: orderCode={}, 原护理员->{}, 新护理员={}",
                    workOrder.getOrderCode(),
                    before != null && before.contains("nurseName") ? "已变更" : "N/A",
                    nurse.getName());
        } else {
            auditLogService.logWorkOrder(workOrder.getId(), workOrder.getOrderCode(),
                    "DISPATCH_ORDER", workOrder.getDispatchedBy(), null, workOrder.toString());
            log.info("工单派发成功: orderCode={}, elder={}, nurse={}",
                    workOrder.getOrderCode(), elder.getName(), nurse.getName());
        }

        return workOrder;
    }

    @Transactional
    public WorkOrder acceptOrder(Long orderId, Long nurseId) {
        WorkOrder order = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + orderId));

        if (order.getStatus() != OrderStatus.DISPATCHED) {
            throw new IllegalStateException("只有已派发的工单才能接单");
        }

        if (!order.getNurseId().equals(nurseId)) {
            throw new SecurityException("该工单不属于此护理员");
        }

        String before = order.toString();
        order.setStatus(OrderStatus.NURSE_ACCEPTED);
        order.setAcceptedAt(LocalDateTime.now());
        order = workOrderRepository.save(order);

        auditLogService.logWorkOrder(order.getId(), order.getOrderCode(),
                "ACCEPT_ORDER", order.getNurseName(), before, order.toString());
        log.info("护理员接单: orderCode={}, nurse={}", order.getOrderCode(), order.getNurseName());
        return order;
    }

    @Transactional
    public WorkOrder cancelOrder(Long orderId, String reason, String operator) {
        WorkOrder order = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + orderId));

        if (order.getStatus() == OrderStatus.CHECKED_IN
                || order.getStatus() == OrderStatus.IN_PROGRESS
                || order.getStatus() == OrderStatus.SERVICE_COMPLETED
                || order.getStatus() == OrderStatus.SETTLED) {
            throw new IllegalStateException("当前状态不允许取消工单: " + order.getStatus());
        }

        String before = order.toString();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        order = workOrderRepository.save(order);

        if (order.getDemandId() != null) {
            demandRepository.findById(order.getDemandId()).ifPresent(demand -> {
                demand.setStatus("CANCELLED");
                demand.setCancelledAt(LocalDateTime.now());
                demand.setCancelReason(reason);
                demandRepository.save(demand);
            });
        }

        auditLogService.logWorkOrder(order.getId(), order.getOrderCode(),
                "CANCEL_ORDER", operator, before, order.toString());
        log.info("工单已取消: orderCode={}, reason={}", order.getOrderCode(), reason);
        return order;
    }

    public List<NurseCandidateVO> getCandidateNurses(Long demandId) {
        ServiceDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("需求不存在: " + demandId));
        return validationService.findCandidateNurses(
                demand.getElderId(),
                demand.getServicePackageId(),
                demand.getRequestedDate(),
                demand.getRequestedStartTime(),
                demand.getRequestedEndTime());
    }

    public DispatchValidationResult previewValidation(Long demandId, Long nurseId) {
        ServiceDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new IllegalArgumentException("需求不存在: " + demandId));
        return validationService.validateDispatch(
                demand.getElderId(), nurseId,
                demand.getServicePackageId(),
                demand.getRequestedDate(),
                demand.getRequestedStartTime(),
                demand.getRequestedEndTime());
    }

    public Optional<WorkOrder> findById(Long id) {
        return workOrderRepository.findById(id);
    }

    public Optional<WorkOrder> findByCode(String code) {
        return workOrderRepository.findByOrderCode(code);
    }

    public Page<WorkOrder> findAll(Pageable pageable) {
        return workOrderRepository.findAll(pageable);
    }

    public Page<WorkOrder> findByStatus(OrderStatus status, Pageable pageable) {
        return workOrderRepository.findByStatus(status, pageable);
    }

    public List<WorkOrder> findByNurseId(Long nurseId) {
        return workOrderRepository.findByNurseId(nurseId);
    }

    public Page<WorkOrder> findByNurseId(Long nurseId, Pageable pageable) {
        return workOrderRepository.findByNurseId(nurseId, pageable);
    }

    public List<WorkOrder> findByElderId(Long elderId) {
        return workOrderRepository.findByElderId(elderId);
    }

    public List<WorkOrder> findNurseDailyOrders(Long nurseId, LocalDate date) {
        return workOrderRepository.findByNurseIdAndScheduledDate(nurseId, date);
    }

    public long countByStatus(OrderStatus status) {
        return workOrderRepository.countByStatus(status);
    }

    public List<WorkOrder> findNurseActiveOrders(Long nurseId) {
        return workOrderRepository.findByNurseIdAndStatus(nurseId, OrderStatus.NURSE_ACCEPTED);
    }
}
