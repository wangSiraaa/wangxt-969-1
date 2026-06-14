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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementReviewService {

    private final SettlementReviewQueueRepository reviewQueueRepository;
    private final SettlementRepository settlementRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AbnormalEventRepository abnormalEventRepository;
    private final SettlementSourceRepository settlementSourceRepository;
    private final AuditLogService auditLogService;

    private static final List<String> TRIGGER_TYPES = Arrays.asList(
            "CHECKIN_ABNORMAL",
            "FALL_RISK",
            "COMPLAINT",
            "INSURANCE_REPORT",
            "SUPPLEMENT_ORDER"
    );

    @Transactional
    public SettlementReviewQueue addToReviewQueue(Long settlementId, String triggerType,
                                                   String abnormalEventType, Long abnormalEventId,
                                                   String submittedBy, String remark) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("结算单不存在: " + settlementId));

        Optional<SettlementReviewQueue> existing = reviewQueueRepository.findBySettlementId(settlementId);
        if (existing.isPresent()) {
            throw new IllegalStateException("该结算单已在复核队列中: " + existing.get().getReviewCode());
        }

        WorkOrder order = workOrderRepository.findById(settlement.getWorkOrderId()).orElse(null);

        SettlementReviewQueue review = new SettlementReviewQueue();
        review.setReviewCode("SRQ" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        review.setSettlementId(settlementId);
        review.setSettlementCode(settlement.getSettlementCode());
        review.setWorkOrderId(settlement.getWorkOrderId());
        review.setOrderCode(settlement.getOrderCode());
        review.setElderId(settlement.getElderId());
        review.setElderName(settlement.getElderName());
        review.setNurseId(settlement.getNurseId());
        review.setNurseName(settlement.getNurseName());
        review.setServicePackageId(settlement.getServicePackageId());
        review.setServicePackageName(settlement.getServicePackageName());
        review.setServiceDate(settlement.getServiceDate());
        review.setSettlementAmount(settlement.getTotalAmount());
        review.setReviewTriggerType(triggerType);
        review.setAbnormalEventId(abnormalEventId);
        review.setAbnormalEventType(abnormalEventType);
        review.setStatus("PENDING_REVIEW");
        review.setReviewLevel(determineReviewLevel(triggerType, abnormalEventType));
        review.setSubmittedAt(LocalDateTime.now());
        review.setSubmittedBy(submittedBy != null ? submittedBy : "SYSTEM");
        review.setRemark(remark);
        review = reviewQueueRepository.save(review);

        auditLogService.logSettlementReview(review.getId(), review.getReviewCode(),
                "ADD_TO_REVIEW", submittedBy, null, review.toString());

        log.info("结算单加入复核队列: reviewCode={}, settlementCode={}, triggerType={}",
                review.getReviewCode(), settlement.getSettlementCode(), triggerType);
        return review;
    }

    @Transactional
    public SettlementReviewQueue reviewSettlement(Long reviewId, Long reviewerId, String reviewerName,
                                                  String reviewResult, String reviewComments,
                                                  BigDecimal adjustmentAmount, String adjustmentReason,
                                                  BigDecimal finalSettlementAmount) {
        SettlementReviewQueue review = reviewQueueRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("复核记录不存在: " + reviewId));

        if (!"PENDING_REVIEW".equals(review.getStatus())) {
            throw new IllegalStateException("当前状态不允许复核: " + review.getStatus());
        }

        String before = review.toString();
        review.setReviewerId(reviewerId);
        review.setReviewerName(reviewerName);
        review.setReviewedAt(LocalDateTime.now());
        review.setReviewResult(reviewResult);
        review.setReviewComments(reviewComments);

        if ("APPROVE".equals(reviewResult)) {
            review.setStatus("REVIEW_APPROVED");
        } else if ("REJECT".equals(reviewResult)) {
            review.setStatus("REVIEW_REJECTED");
        } else if ("ADJUST".equals(reviewResult)) {
            review.setStatus("REVIEW_ADJUSTED");
            review.setAdjustmentAmount(adjustmentAmount);
            review.setAdjustmentReason(adjustmentReason);
            review.setFinalSettlementAmount(finalSettlementAmount);

            if (finalSettlementAmount != null) {
                final BigDecimal originalAmount = review.getSettlementAmount();
                final String adjReason = adjustmentReason;
                settlementRepository.findById(review.getSettlementId()).ifPresent(settlement -> {
                    String sb = settlement.toString();
                    settlement.setTotalAmount(finalSettlementAmount);
                    settlement.setRemark("复核调整，原金额：" + originalAmount
                            + "，调整原因：" + adjReason);
                    settlementRepository.save(settlement);
                    auditLogService.logSettlement(settlement.getId(), settlement.getSettlementCode(),
                            "REVIEW_ADJUST", reviewerName, sb, settlement.toString());
                });
            }
        }

        review = reviewQueueRepository.save(review);

        auditLogService.logSettlementReview(review.getId(), review.getReviewCode(),
                "REVIEW_SETTLEMENT", reviewerName, before, review.toString());

        log.info("结算复核完成: reviewCode={}, result={}, reviewer={}",
                review.getReviewCode(), reviewResult, reviewerName);
        return review;
    }

    @Transactional
    public SettlementReviewQueue escalateReview(Long reviewId, String escalateReason, String operatorName) {
        SettlementReviewQueue review = reviewQueueRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("复核记录不存在: " + reviewId));

        String before = review.toString();
        review.setReviewLevel(review.getReviewLevel() + 1);
        review.setStatus("PENDING_REVIEW");
        review.setRemark("升级复核，原因：" + escalateReason);
        review = reviewQueueRepository.save(review);

        auditLogService.logSettlementReview(review.getId(), review.getReviewCode(),
                "ESCALATE_REVIEW", operatorName, before, review.toString());

        log.info("复核升级: reviewCode={}, newLevel={}", review.getReviewCode(), review.getReviewLevel());
        return review;
    }

    public boolean needsReview(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId).orElse(null);
        if (settlement == null) return false;

        List<AbnormalEvent> abnormalEvents = abnormalEventRepository
                .findByWorkOrderIdOrderByDetectedAtDesc(settlement.getWorkOrderId());

        for (AbnormalEvent event : abnormalEvents) {
            if (isReviewRequired(event.getAbnormalType())) {
                return true;
            }
        }

        return false;
    }

    private boolean isReviewRequired(AbnormalType type) {
        if (type == null) return false;
        switch (type) {
            case CHECKIN_ABNORMAL:
            case FALL_RISK:
            case COMPLAINT:
            case INSURANCE_REPORT:
            case SUPPLEMENT_ORDER:
            case NO_CHECKIN:
            case NO_CHECKOUT:
            case SERVICE_TIMEOUT:
            case FAMILY_REJECTED:
                return true;
            default:
                return false;
        }
    }

    private int determineReviewLevel(String triggerType, String abnormalEventType) {
        if ("COMPLAINT".equals(triggerType) || "FALL_RISK".equals(triggerType)) {
            return 3;
        }
        if ("INSURANCE_REPORT".equals(triggerType) || "SUPPLEMENT_ORDER".equals(triggerType)) {
            return 2;
        }
        return 1;
    }

    @Transactional
    public void autoAddToReviewQueue(Settlement settlement) {
        if (needsReview(settlement.getId())) {
            List<AbnormalEvent> events = abnormalEventRepository
                    .findByWorkOrderIdOrderByDetectedAtDesc(settlement.getWorkOrderId());

            for (AbnormalEvent event : events) {
                if (isReviewRequired(event.getAbnormalType())) {
                    try {
                        addToReviewQueue(settlement.getId(),
                                event.getAbnormalType().name(),
                                event.getAbnormalType().name(),
                                event.getId(),
                                "SYSTEM",
                                "系统自动加入复核队列，原因：" + event.getDescription());
                        break;
                    } catch (Exception e) {
                        log.warn("自动加入复核队列失败: settlementId={}, eventId={}",
                                settlement.getId(), event.getId(), e);
                    }
                }
            }
        }
    }

    public List<SettlementReviewQueue> findPendingReviews() {
        return reviewQueueRepository.findByStatusOrderBySubmittedAtDesc("PENDING_REVIEW");
    }

    public Page<SettlementReviewQueue> findPendingReviews(Pageable pageable) {
        return reviewQueueRepository.findByStatus("PENDING_REVIEW", pageable);
    }

    public Page<SettlementReviewQueue> findAll(Pageable pageable) {
        return reviewQueueRepository.findAllByOrderBySubmittedAtDesc(pageable);
    }

    public List<SettlementReviewQueue> findByElder(Long elderId) {
        return reviewQueueRepository.findByElderIdOrderBySubmittedAtDesc(elderId);
    }

    public List<SettlementReviewQueue> findByNurse(Long nurseId) {
        return reviewQueueRepository.findByNurseIdOrderBySubmittedAtDesc(nurseId);
    }

    public List<SettlementReviewQueue> findByTriggerType(String triggerType) {
        return reviewQueueRepository.findByReviewTriggerTypeOrderBySubmittedAtDesc(triggerType);
    }

    public Optional<SettlementReviewQueue> findById(Long id) {
        return reviewQueueRepository.findById(id);
    }

    public Optional<SettlementReviewQueue> findBySettlementId(Long settlementId) {
        return reviewQueueRepository.findBySettlementId(settlementId);
    }

    public long countPending() {
        return reviewQueueRepository.countByStatus("PENDING_REVIEW");
    }
}
