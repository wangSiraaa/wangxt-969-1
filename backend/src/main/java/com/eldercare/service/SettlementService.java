package com.eldercare.service;

import com.eldercare.entity.CheckInRecord;
import com.eldercare.entity.Settlement;
import com.eldercare.entity.WorkOrder;
import com.eldercare.enums.SettlementStatus;
import com.eldercare.repository.CaregiverRepository;
import com.eldercare.repository.CheckInRecordRepository;
import com.eldercare.repository.ElderRepository;
import com.eldercare.repository.SettlementRepository;
import com.eldercare.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final WorkOrderRepository workOrderRepository;
    private final CheckInRecordRepository checkInRepository;
    private final ElderRepository elderRepository;
    private final CaregiverRepository caregiverRepository;
    private final CheckInService checkInService;

    public SettlementService(SettlementRepository settlementRepository,
                             WorkOrderRepository workOrderRepository,
                             CheckInRecordRepository checkInRepository,
                             ElderRepository elderRepository,
                             CaregiverRepository caregiverRepository,
                             CheckInService checkInService) {
        this.settlementRepository = settlementRepository;
        this.workOrderRepository = workOrderRepository;
        this.checkInRepository = checkInRepository;
        this.elderRepository = elderRepository;
        this.caregiverRepository = caregiverRepository;
        this.checkInService = checkInService;
    }

    private final AtomicInteger settleCounter = new AtomicInteger(0);

    public List<Settlement> findAll() {
        List<Settlement> list = settlementRepository.findAll();
        list.forEach(this::fillTransientFields);
        return list;
    }

    public Optional<Settlement> findById(Long id) {
        Optional<Settlement> opt = settlementRepository.findById(id);
        opt.ifPresent(this::fillTransientFields);
        return opt;
    }

    public List<Settlement> findByStatus(SettlementStatus status) {
        List<Settlement> list = settlementRepository.findByStatus(status);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public List<Settlement> findByWorkOrderId(Long workOrderId) {
        List<Settlement> list = settlementRepository.findByWorkOrderId(workOrderId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public List<Settlement> findByElderId(Long elderId) {
        List<Settlement> list = settlementRepository.findByElderId(elderId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public List<Settlement> findByCaregiverId(Long caregiverId) {
        List<Settlement> list = settlementRepository.findByCaregiverId(caregiverId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    private void fillTransientFields(Settlement settlement) {
        if (settlement.getElderId() != null) {
            elderRepository.findById(settlement.getElderId())
                    .ifPresent(elder -> settlement.setElderName(elder.getName()));
        }
        if (settlement.getCaregiverId() != null) {
            caregiverRepository.findById(settlement.getCaregiverId())
                    .ifPresent(cg -> settlement.setCaregiverName(cg.getName()));
        }
    }

    private synchronized String generateSettlementNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int seq = settleCounter.incrementAndGet() % 10000;
        return "ST" + datePart + String.format("%04d", seq);
    }

    @Transactional
    public Settlement createSettlement(Long workOrderId, BigDecimal hourlyRate,
                                       BigDecimal extraAmount, BigDecimal discountAmount,
                                       String extraRemark, String discountRemark,
                                       String settleRemark) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        if (settlementRepository.existsByWorkOrderId(workOrderId)) {
            throw new RuntimeException("该工单已创建结算单");
        }

        if (!checkInService.hasCheckedIn(workOrderId)) {
            throw new RuntimeException("该工单尚未签到完成，不能结算");
        }

        CheckInRecord checkIn = checkInRepository.findTopByWorkOrderIdOrderByCheckInTimeDesc(workOrderId)
                .orElseThrow(() -> new RuntimeException("未找到签到记录"));

        if (checkIn.getCheckOutTime() == null) {
            throw new RuntimeException("该工单尚未签退完成，不能结算");
        }

        BigDecimal serviceHours = checkIn.getServiceHours() != null ? checkIn.getServiceHours() : BigDecimal.ZERO;
        BigDecimal rate = hourlyRate != null ? hourlyRate : new BigDecimal("50.00");
        BigDecimal baseAmount = serviceHours.multiply(rate);
        BigDecimal extra = extraAmount != null ? extraAmount : BigDecimal.ZERO;
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal totalAmount = baseAmount.add(extra).subtract(discount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        Settlement settlement = new Settlement();
        settlement.setSettlementNo(generateSettlementNo());
        settlement.setWorkOrderId(workOrderId);
        settlement.setDemandId(order.getDemandId());
        settlement.setElderId(order.getElderId());
        settlement.setCaregiverId(order.getCaregiverId());
        settlement.setServiceHours(serviceHours);
        settlement.setHourlyRate(rate);
        settlement.setBaseAmount(baseAmount);
        settlement.setExtraAmount(extra);
        settlement.setDiscountAmount(discount);
        settlement.setTotalAmount(totalAmount);
        settlement.setExtraRemark(extraRemark);
        settlement.setDiscountRemark(discountRemark);
        settlement.setSettleRemark(settleRemark);
        settlement.setStatus(SettlementStatus.PENDING);

        Settlement saved = settlementRepository.save(settlement);
        fillTransientFields(saved);
        return saved;
    }

    @Transactional
    public Settlement confirmSettlement(Long id, String operator) {
        return settlementRepository.findById(id).map(settlement -> {
            if (!SettlementStatus.PENDING.equals(settlement.getStatus())) {
                throw new RuntimeException("当前结算状态为" + settlement.getStatus().getDescription() + "，无法确认结算");
            }

            settlement.setStatus(SettlementStatus.SETTLED);
            settlement.setSettleTime(LocalDateTime.now());
            settlement.setSettleOperator(operator);

            Settlement saved = settlementRepository.save(settlement);
            fillTransientFields(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("结算单不存在"));
    }

    @Transactional
    public Settlement cancelSettlement(Long id, String reason) {
        return settlementRepository.findById(id).map(settlement -> {
            if (SettlementStatus.SETTLED.equals(settlement.getStatus())) {
                throw new RuntimeException("已结算的单据不能取消");
            }
            settlement.setStatus(SettlementStatus.CANCELLED);
            if (settlement.getSettleRemark() != null && !settlement.getSettleRemark().isEmpty()) {
                settlement.setSettleRemark(settlement.getSettleRemark() + " | 取消原因: " + reason);
            } else {
                settlement.setSettleRemark("取消原因: " + reason);
            }
            Settlement saved = settlementRepository.save(settlement);
            fillTransientFields(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("结算单不存在"));
    }

    @Transactional
    public void delete(Long id) {
        settlementRepository.deleteById(id);
    }
}
