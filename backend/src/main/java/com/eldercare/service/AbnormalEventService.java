package com.eldercare.service;

import com.eldercare.common.enums.AbnormalStatus;
import com.eldercare.common.enums.AbnormalType;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.dto.AbnormalHandleDTO;
import com.eldercare.entity.AbnormalEvent;
import com.eldercare.entity.WorkOrder;
import com.eldercare.repository.AbnormalEventRepository;
import com.eldercare.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbnormalEventService {

    private final AbnormalEventRepository abnormalEventRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AuditLogService auditLogService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AbnormalEvent createAbnormal(Long workOrderId, AbnormalType type,
                                        String description, String severity, boolean autoDetected) {
        return doCreateAbnormal(workOrderId, null, null, null, null, type, description, severity, autoDetected);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AbnormalEvent createAbnormalWithInfo(Long workOrderId, Long elderId, String elderName,
                                                 Long nurseId, String nurseName,
                                                 AbnormalType type, String description,
                                                 String severity, boolean autoDetected) {
        return doCreateAbnormal(workOrderId, elderId, elderName, nurseId, nurseName,
                type, description, severity, autoDetected);
    }

    private AbnormalEvent doCreateAbnormal(Long workOrderId, Long elderId, String elderName,
                                            Long nurseId, String nurseName,
                                            AbnormalType type, String description,
                                            String severity, boolean autoDetected) {
        WorkOrder order = null;
        if (workOrderId != null) {
            Optional<WorkOrder> orderOpt = workOrderRepository.findById(workOrderId);
            if (orderOpt.isPresent()) {
                order = orderOpt.get();
                order.setStatus(OrderStatus.ABNORMAL);
                workOrderRepository.save(order);
            }
        }

        AbnormalEvent event = new AbnormalEvent();
        event.setEventCode("ABN" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        event.setWorkOrderId(workOrderId);
        event.setOrderCode(order != null ? order.getOrderCode() : null);
        event.setElderId(order != null ? order.getElderId() : elderId);
        event.setElderName(order != null ? order.getElderName() : elderName);
        event.setNurseId(order != null ? order.getNurseId() : nurseId);
        event.setNurseName(order != null ? order.getNurseName() : nurseName);
        event.setAbnormalType(type);
        event.setStatus(AbnormalStatus.PENDING);
        event.setDescription(description);
        event.setOccurredAt(LocalDateTime.now());
        event.setDetectedAt(LocalDateTime.now());
        event.setDetectedBy(autoDetected ? "SYSTEM" : "MANUAL");
        event.setSeverity(severity);
        event.setAutoDetected(autoDetected);

        event = abnormalEventRepository.save(event);
        try {
            auditLogService.logAbnormal(event.getId(), event.getEventCode(),
                    "CREATE_ABNORMAL", "SYSTEM", null, event.toString());
        } catch (Exception e) {
            log.warn("记录审计日志失败(不影响异常事件): {}", e.getMessage());
        }
        log.warn("异常事件已创建: eventCode={}, type={}, workOrderId={}, elder={}, nurse={}, description={}",
                event.getEventCode(), type, workOrderId, event.getElderName(), event.getNurseName(), description);
        return event;
    }

    @Transactional
    public AbnormalEvent handleAbnormal(AbnormalHandleDTO dto) {
        AbnormalEvent event = abnormalEventRepository.findById(dto.getAbnormalEventId())
                .orElseThrow(() -> new IllegalArgumentException("异常事件不存在: " + dto.getAbnormalEventId()));

        String before = event.toString();
        event.setHandlerId(dto.getHandlerId());
        event.setHandlerName(dto.getHandlerName());
        event.setHandlingNotes(dto.getHandlingNotes());
        event.setResolution(dto.getResolution());
        event.setStatus(AbnormalStatus.PROCESSING);

        if (event.getWorkOrderId() != null && dto.getTargetStatus() != null) {
            Optional<WorkOrder> orderOpt = workOrderRepository.findById(event.getWorkOrderId());
            if (orderOpt.isPresent()) {
                WorkOrder order = orderOpt.get();
                try {
                    order.setStatus(OrderStatus.valueOf(dto.getTargetStatus()));
                    workOrderRepository.save(order);
                } catch (Exception e) {
                    log.warn("目标状态无效: {}", dto.getTargetStatus());
                }
            }
        }

        event = abnormalEventRepository.save(event);
        auditLogService.logAbnormal(event.getId(), event.getEventCode(),
                "HANDLE_ABNORMAL", dto.getHandlerName(), before, event.toString());
        return event;
    }

    @Transactional
    public AbnormalEvent resolveAbnormal(Long abnormalId, Long handlerId, String handlerName,
                                         String resolution, String handlingNotes) {
        AbnormalEvent event = abnormalEventRepository.findById(abnormalId)
                .orElseThrow(() -> new IllegalArgumentException("异常事件不存在: " + abnormalId));

        String before = event.toString();
        event.setHandlerId(handlerId);
        event.setHandlerName(handlerName);
        event.setHandlingNotes(handlingNotes);
        event.setResolution(resolution);
        event.setStatus(AbnormalStatus.RESOLVED);
        event.setResolvedAt(LocalDateTime.now());

        if (event.getWorkOrderId() != null) {
            Optional<WorkOrder> orderOpt = workOrderRepository.findById(event.getWorkOrderId());
            if (orderOpt.isPresent()) {
                WorkOrder order = orderOpt.get();
                if (order.getStatus() == OrderStatus.ABNORMAL) {
                    order.setStatus(OrderStatus.DISPATCHED);
                }
                workOrderRepository.save(order);
            }
        }

        event = abnormalEventRepository.save(event);
        auditLogService.logAbnormal(event.getId(), event.getEventCode(),
                "RESOLVE_ABNORMAL", handlerName, before, event.toString());
        log.info("异常事件已解决: eventCode={}, resolution={}", event.getEventCode(), resolution);
        return event;
    }

    public List<AbnormalEvent> findPending() {
        return abnormalEventRepository.findByStatusOrderByDetectedAtDesc(AbnormalStatus.PENDING);
    }

    public List<AbnormalEvent> findByType(AbnormalType type) {
        return abnormalEventRepository.findByAbnormalTypeOrderByDetectedAtDesc(type);
    }

    public List<AbnormalEvent> findByWorkOrder(Long workOrderId) {
        return abnormalEventRepository.findByWorkOrderIdOrderByDetectedAtDesc(workOrderId);
    }

    public List<AbnormalEvent> findAll() {
        return abnormalEventRepository.findAllByOrderByDetectedAtDesc();
    }

    public Optional<AbnormalEvent> findById(Long id) {
        return abnormalEventRepository.findById(id);
    }

    @Transactional
    public void detectAndCreateTimeoutAbnormals() {
        LocalDateTime now = LocalDateTime.now();
        List<WorkOrder> timeoutOrders = workOrderRepository
                .findTimeoutOrders(LocalDateTime.now().toLocalDate(), now);
        for (WorkOrder order : timeoutOrders) {
            List<AbnormalEvent> existing = abnormalEventRepository
                    .findByWorkOrderIdAndAbnormalType(order.getId(), AbnormalType.SERVICE_TIMEOUT);
            if (existing.isEmpty()) {
                createAbnormal(order.getId(), AbnormalType.SERVICE_TIMEOUT,
                        "服务已超时未完成，计划结束时间：" + order.getScheduledEndTime(),
                        "HIGH", true);
            }
        }

        LocalDateTime threshold = now.minusHours(24);
        List<WorkOrder> pendingConfirmOrders = workOrderRepository
                .findPendingFamilyConfirmTimeout(threshold);
        for (WorkOrder order : pendingConfirmOrders) {
            List<AbnormalEvent> existing = abnormalEventRepository
                    .findByWorkOrderIdAndAbnormalType(order.getId(), AbnormalType.FAMILY_REJECTED);
            if (existing.isEmpty()) {
                createAbnormal(order.getId(), AbnormalType.FAMILY_REJECTED,
                        "家属确认超时超过24小时未处理，完成时间：" + order.getCompletedAt(),
                        "MEDIUM", true);
            }
        }
    }
}
