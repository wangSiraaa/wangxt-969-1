package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.dto.PageResult;
import com.eldercare.dto.CompensationDTO;
import com.eldercare.dto.InsuranceClaimDTO;
import com.eldercare.dto.SettlementCreateDTO;
import com.eldercare.entity.Compensation;
import com.eldercare.entity.InsuranceClaim;
import com.eldercare.entity.Settlement;
import com.eldercare.service.AuditLogService;
import com.eldercare.service.SettlementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Tag(name = "结算接口")
@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SettlementController {

    private final SettlementService settlementService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "创建结算单（未签到/未家属确认会被拦截）")
    public ApiResponse<Settlement> createSettlement(@RequestBody SettlementCreateDTO dto) {
        try {
            Settlement s = settlementService.createSettlement(dto);
            return ApiResponse.success("结算单创建成功，等待最终结算确认", s);
        } catch (com.eldercare.common.exception.BusinessException e) {
            return ApiResponse.validationError(e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过结算")
    public ApiResponse<Settlement> approve(@PathVariable Long id,
                                           @RequestParam(required = false, defaultValue = "FINANCE") String approver,
                                           @RequestParam(required = false) String remark) {
        return ApiResponse.success("结算完成", settlementService.approveSettlement(id, approver, remark));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询结算详情")
    public ApiResponse<Settlement> getById(@PathVariable Long id) {
        return settlementService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "结算单不存在"));
    }

    @GetMapping("/work-order/{workOrderId}")
    @Operation(summary = "按工单查询结算")
    public ApiResponse<Settlement> getByWorkOrder(@PathVariable Long workOrderId) {
        return settlementService.findByWorkOrder(workOrderId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "该工单暂无结算记录（可能尚未完成家属确认）"));
    }

    @GetMapping
    @Operation(summary = "分页查询结算单列表")
    public ApiResponse<PageResult<Settlement>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "settledAt"));
        Page<Settlement> result = status != null && !status.isEmpty()
                ? settlementService.findByStatus(status, pageable)
                : settlementService.findAll(pageable);
        PageResult<Settlement> pr = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pr);
    }

    @PostMapping("/compensation")
    @Operation(summary = "创建补偿记录（异常处置后补偿）")
    public ApiResponse<Compensation> createCompensation(@RequestBody CompensationDTO dto) {
        return ApiResponse.success("补偿记录已创建，等待审批",
                settlementService.createCompensation(dto));
    }

    @PostMapping("/insurance-claim")
    @Operation(summary = "提交保险报销申请")
    public ApiResponse<InsuranceClaim> createInsuranceClaim(@RequestBody InsuranceClaimDTO dto) {
        return ApiResponse.success("保险报销申请已提交，等待理赔审核",
                settlementService.createInsuranceClaim(dto));
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(summary = "查询结算审计日志")
    public ApiResponse<?> getAuditLogs(@PathVariable Long id) {
        return ApiResponse.success(auditLogService.findByBusiness("SETTLEMENT", id));
    }
}
