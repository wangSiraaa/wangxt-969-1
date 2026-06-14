package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.dto.PageResult;
import com.eldercare.entity.SettlementReviewQueue;
import com.eldercare.service.AuditLogService;
import com.eldercare.service.SettlementReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "结算复核接口")
@RestController
@RequestMapping("/settlement-review")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SettlementReviewController {

    private final SettlementReviewService reviewService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "加入复核队列")
    public ApiResponse<SettlementReviewQueue> addToReviewQueue(
            @RequestParam Long settlementId,
            @RequestParam String triggerType,
            @RequestParam(required = false) String abnormalEventType,
            @RequestParam(required = false) Long abnormalEventId,
            @RequestParam(required = false, defaultValue = "SYSTEM") String submittedBy,
            @RequestParam(required = false) String remark) {
        try {
            SettlementReviewQueue review = reviewService.addToReviewQueue(
                    settlementId, triggerType, abnormalEventType,
                    abnormalEventId, submittedBy, remark);
            return ApiResponse.success("已加入复核队列", review);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "复核结算")
    public ApiResponse<SettlementReviewQueue> reviewSettlement(
            @PathVariable Long id,
            @RequestParam(required = false) Long reviewerId,
            @RequestParam(required = false, defaultValue = "REVIEWER") String reviewerName,
            @RequestParam String reviewResult,
            @RequestParam(required = false) String reviewComments,
            @RequestParam(required = false) BigDecimal adjustmentAmount,
            @RequestParam(required = false) String adjustmentReason,
            @RequestParam(required = false) BigDecimal finalSettlementAmount) {
        try {
            SettlementReviewQueue review = reviewService.reviewSettlement(
                    id, reviewerId, reviewerName, reviewResult, reviewComments,
                    adjustmentAmount, adjustmentReason, finalSettlementAmount);
            return ApiResponse.success("复核完成", review);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}/escalate")
    @Operation(summary = "升级复核")
    public ApiResponse<SettlementReviewQueue> escalateReview(
            @PathVariable Long id,
            @RequestParam String escalateReason,
            @RequestParam(required = false, defaultValue = "ADMIN") String operatorName) {
        try {
            SettlementReviewQueue review = reviewService.escalateReview(
                    id, escalateReason, operatorName);
            return ApiResponse.success("已升级复核", review);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "查询待复核列表")
    public ApiResponse<List<SettlementReviewQueue>> getPendingReviews() {
        return ApiResponse.success(reviewService.findPendingReviews());
    }

    @GetMapping("/pending/page")
    @Operation(summary = "分页查询待复核列表")
    public ApiResponse<PageResult<SettlementReviewQueue>> getPendingReviewsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<SettlementReviewQueue> result = reviewService.findPendingReviews(pageable);
        PageResult<SettlementReviewQueue> pr = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pr);
    }

    @GetMapping
    @Operation(summary = "分页查询复核队列")
    public ApiResponse<PageResult<SettlementReviewQueue>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String triggerType,
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) Long nurseId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<SettlementReviewQueue> result;
        if (status != null && !status.isEmpty()) {
            result = reviewService.findPendingReviews(pageable);
        } else {
            result = reviewService.findAll(pageable);
        }
        PageResult<SettlementReviewQueue> pr = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pr);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询复核详情")
    public ApiResponse<SettlementReviewQueue> getById(@PathVariable Long id) {
        return reviewService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "复核记录不存在"));
    }

    @GetMapping("/settlement/{settlementId}")
    @Operation(summary = "按结算单查询复核记录")
    public ApiResponse<SettlementReviewQueue> getBySettlementId(@PathVariable Long settlementId) {
        return reviewService.findBySettlementId(settlementId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "该结算单暂无复核记录"));
    }

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "按老人查询复核记录")
    public ApiResponse<List<SettlementReviewQueue>> getByElder(@PathVariable Long elderId) {
        return ApiResponse.success(reviewService.findByElder(elderId));
    }

    @GetMapping("/nurse/{nurseId}")
    @Operation(summary = "按护理员查询复核记录")
    public ApiResponse<List<SettlementReviewQueue>> getByNurse(@PathVariable Long nurseId) {
        return ApiResponse.success(reviewService.findByNurse(nurseId));
    }

    @GetMapping("/trigger/{triggerType}")
    @Operation(summary = "按触发类型查询复核记录")
    public ApiResponse<List<SettlementReviewQueue>> getByTriggerType(@PathVariable String triggerType) {
        return ApiResponse.success(reviewService.findByTriggerType(triggerType));
    }

    @GetMapping("/count/pending")
    @Operation(summary = "查询待复核数量")
    public ApiResponse<Long> countPending() {
        return ApiResponse.success(reviewService.countPending());
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(summary = "查询复核记录审计日志")
    public ApiResponse<?> getAuditLogs(@PathVariable Long id) {
        return ApiResponse.success(auditLogService.findByBusiness("SETTLEMENT_REVIEW", id));
    }
}
