package com.eldercare.service;

import com.eldercare.entity.AuditLog;
import com.eldercare.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void log(String operation, String module, Long businessId, String businessCode,
                    String operator, String beforeData, String afterData, String remark) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setOperation(operation);
            auditLog.setModule(module);
            auditLog.setBusinessId(businessId);
            auditLog.setBusinessCode(businessCode);
            auditLog.setOperator(operator);
            auditLog.setBeforeData(beforeData);
            auditLog.setAfterData(afterData);
            auditLog.setOperatedAt(LocalDateTime.now());
            auditLog.setIpAddress("system");
            auditLog.setUserAgent("backend");
            auditLog.setRemark(remark);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("保存审计日志失败: operation={}, module={}, businessId={}", operation, module, businessId, e);
        }
    }

    @Async
    @Transactional
    public void logDemand(Long demandId, String demandCode, String operation,
                          String operator, String beforeData, String afterData) {
        log(operation, "SERVICE_DEMAND", demandId, demandCode, operator, beforeData, afterData, null);
    }

    @Async
    @Transactional
    public void logWorkOrder(Long orderId, String orderCode, String operation,
                             String operator, String beforeData, String afterData) {
        log(operation, "WORK_ORDER", orderId, orderCode, operator, beforeData, afterData, null);
    }

    @Async
    @Transactional
    public void logCheckIn(Long checkInId, String orderCode, String operation,
                           String operator, String beforeData, String afterData) {
        log(operation, "CHECK_IN", checkInId, orderCode, operator, beforeData, afterData, null);
    }

    @Async
    @Transactional
    public void logSettlement(Long settlementId, String settlementCode, String operation,
                              String operator, String beforeData, String afterData) {
        log(operation, "SETTLEMENT", settlementId, settlementCode, operator, beforeData, afterData, null);
    }

    @Async
    @Transactional
    public void logAbnormal(Long abnormalId, String eventCode, String operation,
                            String operator, String beforeData, String afterData) {
        log(operation, "ABNORMAL_EVENT", abnormalId, eventCode, operator, beforeData, afterData, null);
    }

    @Async
    @Transactional
    public void logElderPackage(Long packageId, String accountCode, String operation,
                                String operator, String beforeData, String afterData) {
        log(operation, "ELDER_PACKAGE", packageId, accountCode, operator, beforeData, afterData, null);
    }

    @Async
    @Transactional
    public void logSettlementReview(Long reviewId, String reviewCode, String operation,
                                     String operator, String beforeData, String afterData) {
        log(operation, "SETTLEMENT_REVIEW", reviewId, reviewCode, operator, beforeData, afterData, null);
    }

    @Async
    @Transactional
    public void logNurseLeave(Long leaveId, String leaveCode, String operation,
                              String operator, String beforeData, String afterData) {
        log(operation, "NURSE_LEAVE", leaveId, leaveCode, operator, beforeData, afterData, null);
    }

    public List<AuditLog> findByBusiness(String module, Long businessId) {
        return auditLogRepository.findByModuleAndBusinessIdOrderByOperatedAtDesc(module, businessId);
    }

    public List<AuditLog> findByOperator(String operator) {
        return auditLogRepository.findByOperatorOrderByOperatedAtDesc(operator);
    }

    public List<AuditLog> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByOperatedAtBetweenOrderByOperatedAtDesc(start, end);
    }
}
