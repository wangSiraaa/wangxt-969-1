package com.eldercare.service;

import com.eldercare.common.enums.OrderStatus;
import com.eldercare.common.exception.BusinessException;
import com.eldercare.dto.CompensationDTO;
import com.eldercare.dto.InsuranceClaimDTO;
import com.eldercare.dto.SettlementCreateDTO;
import com.eldercare.entity.*;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final CheckInRepository checkInRepository;
    private final FamilyConfirmationRepository confirmationRepository;
    private final ElderRepository elderRepository;
    private final CompensationRepository compensationRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Settlement createSettlement(SettlementCreateDTO dto) {
        WorkOrder order = workOrderRepository.findById(dto.getWorkOrderId())
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + dto.getWorkOrderId()));

        if (order.getStatus() != OrderStatus.FAMILY_CONFIRMED) {
            Optional<FamilyConfirmation> confirmOpt = confirmationRepository.findByWorkOrderId(order.getId());
            if (order.getStatus() == OrderStatus.PENDING_FAMILY_CONFIRM) {
                throw new BusinessException(
                        "服务尚未经家属确认，不能结算。请先完成家属确认流程后再申请结算。",
                        400);
            }
            if (order.getStatus() != OrderStatus.FAMILY_CONFIRMED) {
                throw new BusinessException(
                        "当前工单状态【" + order.getStatus() + "】不允许结算。"
                                + "必须是已完成家属确认的工单才能进入结算。当前需完成：签到→签退→家属确认。",
                        400);
            }
        }

        CheckIn checkIn = checkInRepository.findByWorkOrderId(order.getId())
                .orElseThrow(() -> new BusinessException(
                        "未找到签到记录，服务未签到不能结算。该工单的服务流程不完整。", 400));

        if (checkIn.getCheckInTime() == null) {
            throw new BusinessException("服务未签到，不能结算。实际签到时间为空。", 400);
        }
        if (checkIn.getCheckOutTime() == null) {
            throw new BusinessException("服务未签退，不能结算。实际签退时间为空。", 400);
        }

        Optional<Settlement> existed = settlementRepository.findByWorkOrderId(dto.getWorkOrderId());
        if (existed.isPresent()) {
            throw new IllegalStateException("该工单已有结算记录: " + existed.get().getSettlementCode());
        }

        ServicePackage servicePackage = order.getServicePackageId() != null
                ? servicePackageRepository.findById(order.getServicePackageId()).orElse(null) : null;
        Elder elder = elderRepository.findById(order.getElderId()).orElse(null);

        BigDecimal baseAmount = BigDecimal.ZERO;
        int actualMinutes = checkIn.getServiceDurationMinutes() != null
                ? checkIn.getServiceDurationMinutes() : 0;
        int standardMinutes = servicePackage != null && servicePackage.getStandardDurationMinutes() != null
                ? servicePackage.getStandardDurationMinutes() : 0;

        if (servicePackage != null) {
            if (servicePackage.getBasePrice() != null) {
                baseAmount = servicePackage.getBasePrice();
            }
            if (servicePackage.getHourlyRate() != null && actualMinutes > standardMinutes && standardMinutes > 0) {
                int extraMinutes = actualMinutes - standardMinutes;
                BigDecimal extra = servicePackage.getHourlyRate()
                        .multiply(BigDecimal.valueOf(extraMinutes))
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                baseAmount = baseAmount.add(extra);
            }
        } else {
            long hours = ChronoUnit.HOURS.between(checkIn.getCheckInTime(), checkIn.getCheckOutTime());
            if (hours <= 0) hours = 1;
            baseAmount = BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(hours));
        }

        BigDecimal extraAmount = dto.getExtraAmount() != null ? dto.getExtraAmount() : BigDecimal.ZERO;
        BigDecimal deductAmount = dto.getDeductAmount() != null ? dto.getDeductAmount() : BigDecimal.ZERO;
        BigDecimal totalBeforeIns = baseAmount.add(extraAmount).subtract(deductAmount);
        if (totalBeforeIns.compareTo(BigDecimal.ZERO) < 0) {
            totalBeforeIns = BigDecimal.ZERO;
        }

        boolean insuranceCovered = dto.getInsuranceCovered() != null ? dto.getInsuranceCovered()
                : (elder != null && elder.getInsuranceCoverage() != null
                && elder.getInsuranceCoverage().compareTo(BigDecimal.ZERO) > 0);
        BigDecimal insuranceAmount = BigDecimal.ZERO;
        BigDecimal selfPayAmount;

        if (insuranceCovered) {
            BigDecimal coveragePercent = elder != null && elder.getInsuranceCoverage() != null
                    ? elder.getInsuranceCoverage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    : BigDecimal.valueOf(0.7);
            insuranceAmount = totalBeforeIns.multiply(coveragePercent)
                    .setScale(2, RoundingMode.HALF_UP);
            selfPayAmount = totalBeforeIns.subtract(insuranceAmount);
        } else {
            selfPayAmount = totalBeforeIns;
        }

        Settlement settlement = new Settlement();
        settlement.setSettlementCode("SET" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        settlement.setWorkOrderId(order.getId());
        settlement.setOrderCode(order.getOrderCode());
        settlement.setElderId(order.getElderId());
        settlement.setElderName(order.getElderName());
        settlement.setNurseId(order.getNurseId());
        settlement.setNurseName(order.getNurseName());
        settlement.setServicePackageId(order.getServicePackageId());
        settlement.setServicePackageName(order.getServicePackageName());
        settlement.setServiceDate(order.getScheduledDate());
        settlement.setServiceDurationMinutes(actualMinutes);
        settlement.setBaseAmount(baseAmount);
        settlement.setExtraAmount(extraAmount);
        settlement.setDeductAmount(deductAmount);
        settlement.setTotalAmount(totalBeforeIns);
        settlement.setInsuranceCovered(insuranceCovered);
        settlement.setInsuranceAmount(insuranceAmount);
        settlement.setSelfPayAmount(selfPayAmount);
        settlement.setFamilyConfirmed(true);
        settlement.setFamilyConfirmedAt(LocalDateTime.now());
        settlement.setStatus("PENDING_SETTLEMENT");
        settlement.setSettledBy(dto.getSettledBy() != null ? dto.getSettledBy() : "SYSTEM");
        settlement.setRemark(dto.getRemark());
        settlement = settlementRepository.save(settlement);

        String before = order.toString();
        order.setStatus(OrderStatus.PENDING_SETTLEMENT);
        workOrderRepository.save(order);

        auditLogService.logSettlement(settlement.getId(), settlement.getSettlementCode(),
                "CREATE_SETTLEMENT", settlement.getSettledBy(), null, settlement.toString());
        auditLogService.logWorkOrder(order.getId(), order.getOrderCode(),
                "PENDING_SETTLEMENT", settlement.getSettledBy(), before, order.toString());

        log.info("结算单创建成功: settlementCode={}, orderCode={}, total={}, selfPay={}, insurance={}",
                settlement.getSettlementCode(), order.getOrderCode(),
                totalBeforeIns, selfPayAmount, insuranceAmount);
        return settlement;
    }

    @Transactional
    public Settlement approveSettlement(Long settlementId, String approver, String remark) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("结算单不存在: " + settlementId));

        String before = settlement.toString();
        settlement.setStatus("SETTLED");
        settlement.setSettledAt(LocalDateTime.now());
        if (remark != null) {
            settlement.setRemark(remark);
        }
        settlement = settlementRepository.save(settlement);

        workOrderRepository.findById(settlement.getWorkOrderId()).ifPresent(order -> {
            String ob = order.toString();
            order.setStatus(OrderStatus.SETTLED);
            workOrderRepository.save(order);
            auditLogService.logWorkOrder(order.getId(), order.getOrderCode(),
                    "ORDER_SETTLED", approver, ob, order.toString());
        });

        auditLogService.logSettlement(settlement.getId(), settlement.getSettlementCode(),
                "APPROVE_SETTLEMENT", approver, before, settlement.toString());

        log.info("结算完成: settlementCode={}, totalAmount={}",
                settlement.getSettlementCode(), settlement.getTotalAmount());
        return settlement;
    }

    @Transactional
    public Compensation createCompensation(CompensationDTO dto) {
        WorkOrder order = workOrderRepository.findById(dto.getWorkOrderId()).orElse(null);

        Compensation comp = new Compensation();
        comp.setCompensationCode("CMP" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        comp.setWorkOrderId(dto.getWorkOrderId());
        comp.setOrderCode(order != null ? order.getOrderCode() : null);
        comp.setAbnormalEventId(dto.getAbnormalEventId());
        comp.setElderId(dto.getElderId() != null ? dto.getElderId()
                : (order != null ? order.getElderId() : null));
        comp.setElderName(order != null ? order.getElderName() : null);
        comp.setCompensationType(dto.getCompensationType());
        comp.setCompensationAmount(dto.getCompensationAmount());
        comp.setCompensationDescription(dto.getCompensationDescription());
        comp.setStatus("PENDING_APPROVAL");
        comp.setAppliedBy(dto.getAppliedBy());
        comp.setApprovedBy(dto.getApprovedBy());
        comp.setRemark(dto.getRemark());
        comp = compensationRepository.save(comp);

        log.info("补偿记录创建: code={}, type={}, amount={}", comp.getCompensationCode(),
                comp.getCompensationType(), comp.getCompensationAmount());
        return comp;
    }

    @Transactional
    public InsuranceClaim createInsuranceClaim(InsuranceClaimDTO dto) {
        Settlement settlement = settlementRepository.findById(dto.getSettlementId()).orElse(null);
        Elder elder = dto.getElderId() != null ? elderRepository.findById(dto.getElderId()).orElse(null) : null;

        InsuranceClaim claim = new InsuranceClaim();
        claim.setClaimCode("INS" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        claim.setSettlementId(dto.getSettlementId());
        claim.setSettlementCode(settlement != null ? settlement.getSettlementCode() : null);
        claim.setWorkOrderId(dto.getWorkOrderId() != null ? dto.getWorkOrderId()
                : (settlement != null ? settlement.getWorkOrderId() : null));
        claim.setOrderCode(settlement != null ? settlement.getOrderCode() : null);
        claim.setElderId(dto.getElderId());
        claim.setElderName(elder != null ? elder.getName() : null);
        claim.setInsurantNo(dto.getInsurantNo() != null ? dto.getInsurantNo()
                : (elder != null ? elder.getInsurantNo() : null));
        claim.setInsuranceType(dto.getInsuranceType() != null ? dto.getInsuranceType()
                : (elder != null ? elder.getInsuranceType() : null));
        claim.setClaimAmount(dto.getClaimAmount());
        claim.setClaimDescription(dto.getClaimDescription());
        claim.setClaimMaterials(dto.getClaimMaterials());
        claim.setStatus("PENDING");
        claim.setAppliedBy(dto.getAppliedBy());
        claim.setAppliedAt(LocalDateTime.now());
        claim.setRemark(dto.getRemark());
        claim = insuranceClaimRepository.save(claim);

        log.info("保险报销申请创建: code={}, amount={}", claim.getClaimCode(), claim.getClaimAmount());
        return claim;
    }

    public Optional<Settlement> findByWorkOrder(Long workOrderId) {
        return settlementRepository.findByWorkOrderId(workOrderId);
    }

    public Optional<Settlement> findById(Long id) {
        return settlementRepository.findById(id);
    }

    public Page<Settlement> findAll(Pageable pageable) {
        return settlementRepository.findAllByOrderBySettledAtDesc(pageable);
    }

    public Page<Settlement> findByStatus(String status, Pageable pageable) {
        return settlementRepository.findByStatusOrderBySettledAtDesc(status, pageable);
    }

    public BigDecimal calculateTodaySettlementAmount() {
        LocalDate today = LocalDate.now();
        List<Settlement> list = settlementRepository
                .findByServiceDateAndStatus(today, "SETTLED");
        return list.stream()
                .map(s -> s.getSelfPayAmount() != null ? s.getSelfPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateMonthSettlementAmount() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        List<Settlement> list = settlementRepository
                .findByServiceDateBetweenAndStatus(start, end, "SETTLED");
        return list.stream()
                .map(s -> s.getSelfPayAmount() != null ? s.getSelfPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countTodayCompletedOrders() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return workOrderRepository.findByStatusesAndCompletedAtBetween(
                Arrays.asList(OrderStatus.FAMILY_CONFIRMED, OrderStatus.PENDING_SETTLEMENT, OrderStatus.SETTLED),
                start, end).size();
    }
}
