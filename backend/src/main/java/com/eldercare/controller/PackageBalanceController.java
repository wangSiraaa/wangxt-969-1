package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.dto.PageResult;
import com.eldercare.entity.ElderServicePackage;
import com.eldercare.entity.PackageBalanceLog;
import com.eldercare.service.AuditLogService;
import com.eldercare.service.PackageBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "服务包余额接口")
@RestController
@RequestMapping("/package-balance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PackageBalanceController {

    private final PackageBalanceService packageBalanceService;
    private final AuditLogService auditLogService;

    @PostMapping("/purchase")
    @Operation(summary = "购买服务包")
    public ApiResponse<ElderServicePackage> purchasePackage(
            @RequestParam Long elderId,
            @RequestParam Long servicePackageId,
            @RequestParam(required = false) Integer totalSessions,
            @RequestParam(required = false) Integer totalMinutes,
            @RequestParam(required = false) BigDecimal totalAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam(required = false, defaultValue = "OFFLINE") String purchaseChannel,
            @RequestParam(required = false, defaultValue = "ADMIN") String operatorName,
            @RequestParam(required = false) String remark) {
        try {
            ElderServicePackage pkg = packageBalanceService.purchasePackage(
                    elderId, servicePackageId, totalSessions, totalMinutes,
                    totalAmount, effectiveDate, expiryDate, purchaseChannel,
                    operatorName, remark);
            return ApiResponse.success("服务包购买成功", pkg);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "暂停服务包")
    public ApiResponse<ElderServicePackage> pausePackage(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime pauseEndTime,
            @RequestParam(required = false) String pauseReason,
            @RequestParam(required = false, defaultValue = "ADMIN") String operatorName) {
        try {
            ElderServicePackage pkg = packageBalanceService.pausePackage(
                    id, pauseEndTime, pauseReason, operatorName);
            return ApiResponse.success("服务包暂停成功", pkg);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "恢复服务包")
    public ApiResponse<ElderServicePackage> resumePackage(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "ADMIN") String operatorName) {
        try {
            ElderServicePackage pkg = packageBalanceService.resumePackage(id, operatorName);
            return ApiResponse.success("服务包恢复成功", pkg);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/temp-add")
    @Operation(summary = "临时加项")
    public ApiResponse<PackageBalanceLog> addTemporaryItem(
            @RequestParam Long workOrderId,
            @RequestParam(required = false) Integer addMinutes,
            @RequestParam(required = false) BigDecimal addAmount,
            @RequestParam String itemName,
            @RequestParam(required = false, defaultValue = "ADMIN") String operatorName) {
        try {
            PackageBalanceLog log = packageBalanceService.addTemporaryItem(
                    workOrderId, addMinutes, addAmount, itemName, operatorName);
            return ApiResponse.success("临时加项成功", log);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/family-reject")
    @Operation(summary = "家属拒确认退回服务包")
    public ApiResponse<PackageBalanceLog> handleFamilyRejection(
            @RequestParam Long workOrderId,
            @RequestParam String rejectReason,
            @RequestParam(required = false, defaultValue = "ADMIN") String operatorName) {
        try {
            PackageBalanceLog log = packageBalanceService.handleFamilyRejection(
                    workOrderId, rejectReason, operatorName);
            return ApiResponse.success("家属拒确认退回成功", log);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "查询老人的服务包列表")
    public ApiResponse<List<ElderServicePackage>> getElderPackages(@PathVariable Long elderId) {
        return ApiResponse.success(packageBalanceService.getElderPackages(elderId));
    }

    @GetMapping("/elder/{elderId}/active")
    @Operation(summary = "查询老人的有效服务包")
    public ApiResponse<ElderServicePackage> getActivePackage(@PathVariable Long elderId) {
        ElderServicePackage pkg = packageBalanceService.getActivePackage(elderId);
        if (pkg == null) {
            return ApiResponse.error(404, "该老人暂无有效服务包");
        }
        return ApiResponse.success(pkg);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询服务包详情")
    public ApiResponse<ElderServicePackage> getById(@PathVariable Long id) {
        return packageBalanceService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "服务包不存在"));
    }

    @GetMapping("/account/{accountCode}")
    @Operation(summary = "按账户编码查询服务包")
    public ApiResponse<ElderServicePackage> getByAccountCode(@PathVariable String accountCode) {
        return packageBalanceService.findByAccountCode(accountCode)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "服务包不存在"));
    }

    @GetMapping("/logs")
    @Operation(summary = "分页查询余额变动日志")
    public ApiResponse<PageResult<PackageBalanceLog>> getBalanceLogs(
            @RequestParam(required = false) Long elderId,
            @RequestParam(required = false) Long elderPackageId,
            @RequestParam(required = false) Long workOrderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operatedAt"));
        Page<PackageBalanceLog> result;
        if (elderId != null) {
            result = packageBalanceService.getBalanceLogsByElder(elderId, pageable);
        } else if (elderPackageId != null) {
            List<PackageBalanceLog> list = packageBalanceService.getBalanceLogsByPackage(elderPackageId);
            return ApiResponse.success(new PageResult<>(list, (long) list.size(), 1, page, size));
        } else if (workOrderId != null) {
            List<PackageBalanceLog> list = packageBalanceService.getBalanceLogsByWorkOrder(workOrderId);
            return ApiResponse.success(new PageResult<>(list, (long) list.size(), 1, page, size));
        } else {
            return ApiResponse.error(400, "请指定查询条件（elderId/elderPackageId/workOrderId）");
        }
        PageResult<PackageBalanceLog> pr = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pr);
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(summary = "查询服务包审计日志")
    public ApiResponse<?> getAuditLogs(@PathVariable Long id) {
        return ApiResponse.success(auditLogService.findByBusiness("ELDER_PACKAGE", id));
    }
}
