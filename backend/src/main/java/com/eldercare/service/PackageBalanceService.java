package com.eldercare.service;

import com.eldercare.common.enums.AbnormalType;
import com.eldercare.common.exception.BusinessException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageBalanceService {

    private final ElderServicePackageRepository elderPackageRepository;
    private final PackageBalanceLogRepository balanceLogRepository;
    private final ElderRepository elderRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final WorkOrderRepository workOrderRepository;
    private final CheckInRepository checkInRepository;
    private final AbnormalEventService abnormalEventService;
    private final AuditLogService auditLogService;

    @Transactional
    public ElderServicePackage purchasePackage(Long elderId, Long servicePackageId,
                                              Integer totalSessions, Integer totalMinutes,
                                              BigDecimal totalAmount, LocalDate effectiveDate,
                                              LocalDate expiryDate, String purchaseChannel,
                                              String operatorName, String remark) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new IllegalArgumentException("老人不存在: " + elderId));
        ServicePackage servicePackage = servicePackageRepository.findById(servicePackageId)
                .orElseThrow(() -> new IllegalArgumentException("服务包不存在: " + servicePackageId));

        ElderServicePackage elderPkg = new ElderServicePackage();
        elderPkg.setAccountCode("ESP" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        elderPkg.setElderId(elderId);
        elderPkg.setElderName(elder.getName());
        elderPkg.setServicePackageId(servicePackageId);
        elderPkg.setServicePackageName(servicePackage.getName());
        elderPkg.setPackageType("LONG_TERM");

        if (totalSessions != null) {
            elderPkg.setTotalSessions(totalSessions);
            elderPkg.setUsedSessions(0);
            elderPkg.setRemainingSessions(totalSessions);
        }
        if (totalMinutes != null) {
            elderPkg.setTotalMinutes(totalMinutes);
            elderPkg.setUsedMinutes(0);
            elderPkg.setRemainingMinutes(totalMinutes);
        }
        if (totalAmount != null) {
            elderPkg.setTotalAmount(totalAmount);
            elderPkg.setUsedAmount(BigDecimal.ZERO);
            elderPkg.setRemainingAmount(totalAmount);
        }

        elderPkg.setEffectiveDate(effectiveDate != null ? effectiveDate : LocalDate.now());
        elderPkg.setExpiryDate(expiryDate);
        elderPkg.setPurchaseDate(LocalDateTime.now());
        elderPkg.setPurchaseChannel(purchaseChannel != null ? purchaseChannel : "OFFLINE");
        elderPkg.setStatus("ACTIVE");
        elderPkg.setRemark(remark);

        elderPkg = elderPackageRepository.save(elderPkg);

        PackageBalanceLog balanceLog = createBalanceLog(elderPkg, "PURCHASE", "购买套餐",
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, operatorName);
        balanceLogRepository.save(balanceLog);

        auditLogService.logElderPackage(elderPkg.getId(), elderPkg.getAccountCode(),
                "PURCHASE_PACKAGE", operatorName, null, elderPkg.toString());

        log.info("老人购买套餐成功: accountCode={}, elder={}, package={}",
                elderPkg.getAccountCode(), elder.getName(), servicePackage.getName());
        return elderPkg;
    }

    @Transactional
    public PackageBalanceLog deductBalance(Long workOrderId, Integer deductMinutes,
                                          BigDecimal deductAmount, String operatorName) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + workOrderId));

        ElderServicePackage activePackage = getActivePackage(order.getElderId());
        if (activePackage == null) {
            throw new BusinessException("老人【" + order.getElderName() + "】没有可用的服务包账户", 400);
        }

        int minutesToDeduct = deductMinutes != null ? deductMinutes : 0;
        BigDecimal amountToDeduct = deductAmount != null ? deductAmount : BigDecimal.ZERO;

        if (minutesToDeduct > 0 && activePackage.getRemainingMinutes() != null
                && activePackage.getRemainingMinutes() < minutesToDeduct) {
            throw new BusinessException("服务包剩余时长不足，剩余："
                    + activePackage.getRemainingMinutes() + "分钟，需要：" + minutesToDeduct + "分钟", 400);
        }
        if (amountToDeduct.compareTo(BigDecimal.ZERO) > 0 && activePackage.getRemainingAmount() != null
                && activePackage.getRemainingAmount().compareTo(amountToDeduct) < 0) {
            throw new BusinessException("服务包剩余金额不足", 400);
        }

        Integer beforeSessions = activePackage.getUsedSessions();
        Integer afterSessions = beforeSessions + 1;
        Integer beforeMinutes = activePackage.getRemainingMinutes();
        Integer afterMinutes = beforeMinutes != null ? beforeMinutes - minutesToDeduct : null;
        BigDecimal beforeAmount = activePackage.getRemainingAmount();
        BigDecimal afterAmount = beforeAmount != null ? beforeAmount.subtract(amountToDeduct) : null;

        activePackage.setUsedSessions(afterSessions);
        if (activePackage.getTotalSessions() != null) {
            activePackage.setRemainingSessions(activePackage.getTotalSessions() - afterSessions);
        }
        if (minutesToDeduct > 0 && activePackage.getTotalMinutes() != null) {
            activePackage.setUsedMinutes(activePackage.getUsedMinutes() + minutesToDeduct);
            activePackage.setRemainingMinutes(afterMinutes);
        }
        if (amountToDeduct.compareTo(BigDecimal.ZERO) > 0 && activePackage.getTotalAmount() != null) {
            activePackage.setUsedAmount(activePackage.getUsedAmount().add(amountToDeduct));
            activePackage.setRemainingAmount(afterAmount);
        }

        elderPackageRepository.save(activePackage);

        PackageBalanceLog balanceLog = createBalanceLog(activePackage, "DEDUCT", "服务完成扣减",
                workOrderId, order.getOrderCode(),
                null, null,
                beforeSessions, -1, afterSessions,
                beforeMinutes, -minutesToDeduct, afterMinutes,
                beforeAmount, amountToDeduct.negate(), afterAmount,
                operatorName);
        balanceLog = balanceLogRepository.save(balanceLog);

        log.info("服务包扣减成功: accountCode={}, orderCode={}, minutes={}, amount={}",
                activePackage.getAccountCode(), order.getOrderCode(), minutesToDeduct, amountToDeduct);
        return balanceLog;
    }

    @Transactional
    public PackageBalanceLog addTemporaryItem(Long workOrderId, Integer addMinutes,
                                               BigDecimal addAmount, String itemName,
                                               String operatorName) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + workOrderId));

        ElderServicePackage activePackage = getActivePackage(order.getElderId());
        if (activePackage == null) {
            throw new BusinessException("老人【" + order.getElderName() + "】没有可用的服务包账户", 400);
        }

        int minutesToAdd = addMinutes != null ? addMinutes : 0;
        BigDecimal amountToAdd = addAmount != null ? addAmount : BigDecimal.ZERO;

        Integer beforeMinutes = activePackage.getTotalMinutes();
        Integer afterMinutes = beforeMinutes != null ? beforeMinutes + minutesToAdd : null;
        BigDecimal beforeAmount = activePackage.getTotalAmount();
        BigDecimal afterAmount = beforeAmount != null ? beforeAmount.add(amountToAdd) : null;

        if (minutesToAdd > 0 && activePackage.getTotalMinutes() != null) {
            activePackage.setTotalMinutes(afterMinutes);
            activePackage.setRemainingMinutes(activePackage.getRemainingMinutes() + minutesToAdd);
        }
        if (amountToAdd.compareTo(BigDecimal.ZERO) > 0 && activePackage.getTotalAmount() != null) {
            activePackage.setTotalAmount(afterAmount);
            activePackage.setRemainingAmount(activePackage.getRemainingAmount().add(amountToAdd));
        }

        elderPackageRepository.save(activePackage);

        PackageBalanceLog balanceLog = createBalanceLog(activePackage, "TEMP_ADD",
                "临时加项：" + itemName,
                workOrderId, order.getOrderCode(),
                null, null,
                null, null, null,
                beforeMinutes, minutesToAdd, afterMinutes,
                beforeAmount, amountToAdd, afterAmount,
                operatorName);
        balanceLog = balanceLogRepository.save(balanceLog);

        abnormalEventService.createAbnormal(workOrderId, AbnormalType.TEMP_ADD_ITEM,
                "临时加项：" + itemName + "，增加时长：" + minutesToAdd + "分钟，增加金额：" + amountToAdd + "元",
                "LOW", false);

        log.info("临时加项成功: accountCode={}, orderCode={}, item={}, minutes={}, amount={}",
                activePackage.getAccountCode(), order.getOrderCode(), itemName, minutesToAdd, amountToAdd);
        return balanceLog;
    }

    @Transactional
    public ElderServicePackage pausePackage(Long elderPackageId, LocalDateTime pauseEndTime,
                                            String pauseReason, String operatorName) {
        ElderServicePackage pkg = elderPackageRepository.findById(elderPackageId)
                .orElseThrow(() -> new IllegalArgumentException("服务包账户不存在: " + elderPackageId));

        if (!"ACTIVE".equals(pkg.getStatus())) {
            throw new IllegalStateException("服务包当前状态不允许暂停: " + pkg.getStatus());
        }

        String before = pkg.toString();
        pkg.setStatus("PAUSED");
        pkg.setPauseStartTime(LocalDateTime.now());
        pkg.setPauseEndTime(pauseEndTime);
        pkg.setPauseReason(pauseReason);

        if (pauseEndTime != null) {
            long days = ChronoUnit.DAYS.between(pkg.getPauseStartTime().toLocalDate(), pauseEndTime.toLocalDate());
            pkg.setPausedDays(pkg.getPausedDays() + (int) days);
            if (pkg.getExpiryDate() != null) {
                pkg.setExpiryDate(pkg.getExpiryDate().plusDays(days));
            }
        }

        pkg = elderPackageRepository.save(pkg);

        abnormalEventService.createAbnormalWithInfo(null, pkg.getElderId(), pkg.getElderName(),
                null, null, AbnormalType.ELDER_PAUSED,
                "服务包暂停，原因：" + pauseReason,
                "MEDIUM", false);

        auditLogService.logElderPackage(pkg.getId(), pkg.getAccountCode(),
                "PAUSE_PACKAGE", operatorName, before, pkg.toString());

        log.info("服务包暂停成功: accountCode={}, reason={}", pkg.getAccountCode(), pauseReason);
        return pkg;
    }

    @Transactional
    public ElderServicePackage resumePackage(Long elderPackageId, String operatorName) {
        ElderServicePackage pkg = elderPackageRepository.findById(elderPackageId)
                .orElseThrow(() -> new IllegalArgumentException("服务包账户不存在: " + elderPackageId));

        if (!"PAUSED".equals(pkg.getStatus())) {
            throw new IllegalStateException("服务包当前状态不允许恢复: " + pkg.getStatus());
        }

        String before = pkg.toString();
        pkg.setStatus("ACTIVE");
        pkg.setPauseStartTime(null);
        pkg.setPauseEndTime(null);
        pkg.setPauseReason(null);
        pkg = elderPackageRepository.save(pkg);

        auditLogService.logElderPackage(pkg.getId(), pkg.getAccountCode(),
                "RESUME_PACKAGE", operatorName, before, pkg.toString());

        log.info("服务包恢复成功: accountCode={}", pkg.getAccountCode());
        return pkg;
    }

    @Transactional
    public PackageBalanceLog handleReassignment(Long workOrderId, String operatorName) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + workOrderId));

        ElderServicePackage activePackage = getActivePackage(order.getElderId());
        if (activePackage == null) {
            log.warn("改派时未找到服务包，不进行余额操作");
            return null;
        }

        PackageBalanceLog balanceLog = createBalanceLog(activePackage, "REASSIGN", "护理员改派",
                workOrderId, order.getOrderCode(),
                null, null,
                null, null, null,
                null, null, null,
                null, null, null,
                operatorName);
        balanceLog = balanceLogRepository.save(balanceLog);

        log.info("记录改派日志: accountCode={}, orderCode={}",
                activePackage.getAccountCode(), order.getOrderCode());
        return balanceLog;
    }

    @Transactional
    public PackageBalanceLog handleNurseLeave(Long workOrderId, Long nurseId,
                                               LocalDateTime leaveStartTime,
                                               LocalDateTime leaveEndTime,
                                               String leaveReason, String operatorName) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + workOrderId));

        ElderServicePackage activePackage = getActivePackage(order.getElderId());
        if (activePackage == null) {
            log.warn("护理员请假时未找到服务包，不进行余额操作");
            return null;
        }

        order.setStatus(com.eldercare.common.enums.OrderStatus.PENDING_DISPATCH);
        workOrderRepository.save(order);

        PackageBalanceLog balanceLog = createBalanceLog(activePackage, "NURSE_LEAVE",
                "护理员请假改派",
                workOrderId, order.getOrderCode(),
                null, null,
                null, null, null,
                null, null, null,
                null, null, null,
                operatorName);
        balanceLog = balanceLogRepository.save(balanceLog);

        abnormalEventService.createAbnormal(workOrderId, AbnormalType.NURSE_LEAVE,
                "护理员请假，原因：" + leaveReason + "，请假时间：" + leaveStartTime + " 至 " + leaveEndTime,
                "MEDIUM", false);

        log.info("护理员请假改派记录: orderCode={}, nurseId={}", order.getOrderCode(), nurseId);
        return balanceLog;
    }

    @Transactional
    public PackageBalanceLog handleFamilyRejection(Long workOrderId, String rejectReason,
                                                  String operatorName) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + workOrderId));

        ElderServicePackage activePackage = getActivePackage(order.getElderId());
        if (activePackage == null) {
            throw new BusinessException("老人没有可用的服务包账户", 400);
        }

        CheckIn checkIn = checkInRepository.findByWorkOrderId(workOrderId).orElse(null);

        int refundMinutes = 0;
        BigDecimal refundAmount = BigDecimal.ZERO;

        if (checkIn != null && checkIn.getServiceDurationMinutes() != null) {
            refundMinutes = checkIn.getServiceDurationMinutes();
        }

        Integer beforeSessions = activePackage.getUsedSessions();
        Integer afterSessions = beforeSessions > 0 ? beforeSessions - 1 : 0;
        Integer beforeMinutes = activePackage.getRemainingMinutes();
        Integer afterMinutes = beforeMinutes != null ? beforeMinutes + refundMinutes : null;
        BigDecimal beforeAmount = activePackage.getRemainingAmount();
        BigDecimal afterAmount = beforeAmount != null ? beforeAmount.add(refundAmount) : null;

        if (beforeSessions > 0) {
            activePackage.setUsedSessions(afterSessions);
            if (activePackage.getTotalSessions() != null) {
                activePackage.setRemainingSessions(activePackage.getTotalSessions() - afterSessions);
            }
        }
        if (refundMinutes > 0 && activePackage.getTotalMinutes() != null) {
            activePackage.setUsedMinutes(Math.max(0, activePackage.getUsedMinutes() - refundMinutes));
            activePackage.setRemainingMinutes(afterMinutes);
        }

        elderPackageRepository.save(activePackage);

        PackageBalanceLog balanceLog = createBalanceLog(activePackage, "FAMILY_REJECT",
                "家属拒确认退回",
                workOrderId, order.getOrderCode(),
                null, null,
                beforeSessions, 1, afterSessions,
                beforeMinutes, refundMinutes, afterMinutes,
                beforeAmount, refundAmount, afterAmount,
                operatorName);
        balanceLog = balanceLogRepository.save(balanceLog);

        log.info("家属拒确认退回服务包: accountCode={}, orderCode={}, reason={}",
                activePackage.getAccountCode(), order.getOrderCode(), rejectReason);
        return balanceLog;
    }

    public ElderServicePackage getActivePackage(Long elderId) {
        List<ElderServicePackage> packages = elderPackageRepository
                .findByElderIdAndStatusOrderByCreatedAtDesc(elderId, "ACTIVE");
        if (packages.isEmpty()) {
            return null;
        }

        LocalDate today = LocalDate.now();
        for (ElderServicePackage pkg : packages) {
            boolean effective = pkg.getEffectiveDate() == null || !pkg.getEffectiveDate().isBefore(today)
                    || pkg.getEffectiveDate().isEqual(today);
            boolean notExpired = pkg.getExpiryDate() == null || pkg.getExpiryDate().isAfter(today)
                    || pkg.getExpiryDate().isEqual(today);
            if (effective && notExpired) {
                return pkg;
            }
        }
        return null;
    }

    public List<ElderServicePackage> getElderPackages(Long elderId) {
        return elderPackageRepository.findByElderIdOrderByCreatedAtDesc(elderId);
    }

    public Page<ElderServicePackage> getElderPackages(Long elderId, Pageable pageable) {
        return elderPackageRepository.findAll(pageable);
    }

    public Optional<ElderServicePackage> findById(Long id) {
        return elderPackageRepository.findById(id);
    }

    public Optional<ElderServicePackage> findByAccountCode(String accountCode) {
        return elderPackageRepository.findByAccountCode(accountCode);
    }

    public List<PackageBalanceLog> getBalanceLogsByPackage(Long elderPackageId) {
        return balanceLogRepository.findByElderServicePackageIdOrderByOperatedAtDesc(elderPackageId);
    }

    public Page<PackageBalanceLog> getBalanceLogsByElder(Long elderId, Pageable pageable) {
        return balanceLogRepository.findByElderId(elderId, pageable);
    }

    public List<PackageBalanceLog> getBalanceLogsByWorkOrder(Long workOrderId) {
        return balanceLogRepository.findByWorkOrderId(workOrderId);
    }

    private PackageBalanceLog createBalanceLog(ElderServicePackage pkg, String changeType,
                                               String changeReason,
                                               Long workOrderId, String orderCode,
                                               Long abnormalEventId, String eventCode,
                                               Integer beforeSessions, Integer changeSessions, Integer afterSessions,
                                               Integer beforeMinutes, Integer changeMinutes, Integer afterMinutes,
                                               BigDecimal beforeAmount, BigDecimal changeAmount, BigDecimal afterAmount,
                                               String operatorName) {
        PackageBalanceLog log = new PackageBalanceLog();
        log.setLogCode("PBL" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        log.setElderServicePackageId(pkg.getId());
        log.setAccountCode(pkg.getAccountCode());
        log.setElderId(pkg.getElderId());
        log.setElderName(pkg.getElderName());
        log.setServicePackageId(pkg.getServicePackageId());
        log.setServicePackageName(pkg.getServicePackageName());
        log.setChangeType(changeType);
        log.setChangeReason(changeReason);
        log.setWorkOrderId(workOrderId);
        log.setOrderCode(orderCode);
        log.setAbnormalEventId(abnormalEventId);
        log.setEventCode(eventCode);
        log.setBeforeSessions(beforeSessions);
        log.setChangeSessions(changeSessions);
        log.setAfterSessions(afterSessions);
        log.setBeforeMinutes(beforeMinutes);
        log.setChangeMinutes(changeMinutes);
        log.setAfterMinutes(afterMinutes);
        log.setBeforeAmount(beforeAmount);
        log.setChangeAmount(changeAmount);
        log.setAfterAmount(afterAmount);
        log.setOperatorName(operatorName);
        log.setOperatedAt(LocalDateTime.now());
        return log;
    }
}
