package com.eldercare.controller;

import com.eldercare.common.Result;
import com.eldercare.entity.Settlement;
import com.eldercare.enums.SettlementStatus;
import com.eldercare.service.SettlementService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/settlements")
public class SettlementController {
    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public Result<List<Settlement>> findAll() {
        return Result.success(settlementService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Settlement> findById(@PathVariable Long id) {
        return settlementService.findById(id)
                .map(Result::success)
                .orElse(Result.error("结算单不存在"));
    }

    @GetMapping("/status/{status}")
    public Result<List<Settlement>> findByStatus(@PathVariable SettlementStatus status) {
        return Result.success(settlementService.findByStatus(status));
    }

    @GetMapping("/workorder/{workOrderId}")
    public Result<List<Settlement>> findByWorkOrderId(@PathVariable Long workOrderId) {
        return Result.success(settlementService.findByWorkOrderId(workOrderId));
    }

    @GetMapping("/elder/{elderId}")
    public Result<List<Settlement>> findByElderId(@PathVariable Long elderId) {
        return Result.success(settlementService.findByElderId(elderId));
    }

    @GetMapping("/caregiver/{caregiverId}")
    public Result<List<Settlement>> findByCaregiverId(@PathVariable Long caregiverId) {
        return Result.success(settlementService.findByCaregiverId(caregiverId));
    }

    @PostMapping("/create")
    public Result<Settlement> createSettlement(@RequestBody Map<String, Object> params) {
        try {
            Long workOrderId = Long.valueOf(params.get("workOrderId").toString());
            BigDecimal hourlyRate = params.containsKey("hourlyRate") && params.get("hourlyRate") != null
                    ? new BigDecimal(params.get("hourlyRate").toString()) : null;
            BigDecimal extraAmount = params.containsKey("extraAmount") && params.get("extraAmount") != null
                    ? new BigDecimal(params.get("extraAmount").toString()) : null;
            BigDecimal discountAmount = params.containsKey("discountAmount") && params.get("discountAmount") != null
                    ? new BigDecimal(params.get("discountAmount").toString()) : null;
            String extraRemark = params.getOrDefault("extraRemark", "").toString();
            String discountRemark = params.getOrDefault("discountRemark", "").toString();
            String settleRemark = params.getOrDefault("settleRemark",
                    params.getOrDefault("settlementRemark", "")).toString();

            return Result.success(settlementService.createSettlement(
                    workOrderId, hourlyRate, extraAmount, discountAmount,
                    extraRemark, discountRemark, settleRemark
            ));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm")
    public Result<Settlement> confirm(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        try {
            String operator = params != null ? params.getOrDefault("operator", "系统管理员") : "系统管理员";
            return Result.success(settlementService.confirmSettlement(id, operator));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<Settlement> cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        try {
            String reason = params != null ? params.getOrDefault("reason", "") : "";
            return Result.success(settlementService.cancelSettlement(id, reason));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        settlementService.delete(id);
        return Result.success();
    }
}
