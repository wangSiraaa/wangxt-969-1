package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.dto.PageResult;
import com.eldercare.entity.SettlementSource;
import com.eldercare.service.SettlementSourceService;
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

@Tag(name = "结算来源追踪接口")
@RestController
@RequestMapping("/settlement-sources")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SettlementSourceController {

    private final SettlementSourceService sourceService;

    @PostMapping
    @Operation(summary = "创建结算来源")
    public ApiResponse<SettlementSource> createSource(@RequestBody SettlementSource source) {
        try {
            SettlementSource result = sourceService.createSource(source);
            return ApiResponse.success("创建成功", result);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/generate/{settlementId}")
    @Operation(summary = "生成结算来源明细")
    public ApiResponse<List<SettlementSource>> generateSources(@PathVariable Long settlementId) {
        try {
            List<SettlementSource> sources = sourceService.generateSettlementSources(settlementId);
            return ApiResponse.success("生成成功", sources);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/abnormal")
    @Operation(summary = "添加异常结算来源")
    public ApiResponse<SettlementSource> addAbnormalSource(
            @RequestParam Long settlementId,
            @RequestParam(required = false) Long abnormalEventId,
            @RequestParam String abnormalType,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {
        try {
            SettlementSource source = sourceService.addAbnormalSource(
                    settlementId, abnormalEventId, abnormalType, amount, description);
            return ApiResponse.success("添加成功", source);
        } catch (Exception e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/settlement/{settlementId}")
    @Operation(summary = "按结算单查询来源明细")
    public ApiResponse<List<SettlementSource>> getBySettlement(@PathVariable Long settlementId) {
        return ApiResponse.success(sourceService.getSourcesBySettlement(settlementId));
    }

    @GetMapping("/settlement/code/{settlementCode}")
    @Operation(summary = "按结算编码查询来源明细")
    public ApiResponse<List<SettlementSource>> getBySettlementCode(@PathVariable String settlementCode) {
        return ApiResponse.success(sourceService.getSourcesBySettlementCode(settlementCode));
    }

    @GetMapping("/work-order/{workOrderId}")
    @Operation(summary = "按工单查询结算来源")
    public ApiResponse<List<SettlementSource>> getByWorkOrder(@PathVariable Long workOrderId) {
        return ApiResponse.success(sourceService.getSourcesByWorkOrder(workOrderId));
    }

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "按老人查询结算来源")
    public ApiResponse<List<SettlementSource>> getByElder(@PathVariable Long elderId) {
        return ApiResponse.success(sourceService.getSourcesByElder(elderId));
    }

    @GetMapping("/elder/{elderId}/page")
    @Operation(summary = "分页按老人查询结算来源")
    public ApiResponse<PageResult<SettlementSource>> getByElderPage(
            @PathVariable Long elderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "serviceDate"));
        Page<SettlementSource> result = sourceService.getSourcesByElder(elderId, pageable);
        PageResult<SettlementSource> pr = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pr);
    }

    @GetMapping("/nurse/{nurseId}")
    @Operation(summary = "按护理员查询结算来源")
    public ApiResponse<List<SettlementSource>> getByNurse(@PathVariable Long nurseId) {
        return ApiResponse.success(sourceService.getSourcesByNurse(nurseId));
    }

    @GetMapping("/nurse/{nurseId}/page")
    @Operation(summary = "分页按护理员查询结算来源")
    public ApiResponse<PageResult<SettlementSource>> getByNursePage(
            @PathVariable Long nurseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "serviceDate"));
        Page<SettlementSource> result = sourceService.getSourcesByNurse(nurseId, pageable);
        PageResult<SettlementSource> pr = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pr);
    }

    @GetMapping("/service-package/{servicePackageId}")
    @Operation(summary = "按服务包查询结算来源")
    public ApiResponse<List<SettlementSource>> getByServicePackage(@PathVariable Long servicePackageId) {
        return ApiResponse.success(sourceService.getSourcesByServicePackage(servicePackageId));
    }

    @GetMapping("/abnormal-type/{abnormalType}")
    @Operation(summary = "按异常类型查询结算来源")
    public ApiResponse<List<SettlementSource>> getByAbnormalType(@PathVariable String abnormalType) {
        return ApiResponse.success(sourceService.getSourcesByAbnormalType(abnormalType));
    }

    @GetMapping("/abnormal-type/{abnormalType}/page")
    @Operation(summary = "分页按异常类型查询结算来源")
    public ApiResponse<PageResult<SettlementSource>> getByAbnormalTypePage(
            @PathVariable String abnormalType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "serviceDate"));
        Page<SettlementSource> result = sourceService.getSourcesByAbnormalType(abnormalType, pageable);
        PageResult<SettlementSource> pr = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pr);
    }

    @GetMapping("/service-item/{serviceItemCode}")
    @Operation(summary = "按服务项目查询结算来源")
    public ApiResponse<List<SettlementSource>> getByServiceItem(@PathVariable String serviceItemCode) {
        return ApiResponse.success(sourceService.getSourcesByServiceItem(serviceItemCode));
    }

    @GetMapping("/date-range")
    @Operation(summary = "按日期范围查询结算来源")
    public ApiResponse<List<SettlementSource>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(sourceService.getSourcesByDateRange(startDate, endDate));
    }

    @GetMapping("/source-type/{sourceType}")
    @Operation(summary = "按来源类型查询结算来源")
    public ApiResponse<List<SettlementSource>> getBySourceType(@PathVariable String sourceType) {
        return ApiResponse.success(sourceService.getSourcesBySourceType(sourceType));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询结算来源详情")
    public ApiResponse<SettlementSource> getById(@PathVariable Long id) {
        return sourceService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "结算来源不存在"));
    }

    @GetMapping("/elder/{elderId}/total")
    @Operation(summary = "统计老人总结算金额")
    public ApiResponse<BigDecimal> getElderTotal(@PathVariable Long elderId) {
        return ApiResponse.success(sourceService.calculateTotalByElder(elderId));
    }

    @GetMapping("/nurse/{nurseId}/total")
    @Operation(summary = "统计护理员总结算金额")
    public ApiResponse<BigDecimal> getNurseTotal(@PathVariable Long nurseId) {
        return ApiResponse.success(sourceService.calculateTotalByNurse(nurseId));
    }
}
