package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.dto.PageResult;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.dto.DispatchCreateDTO;
import com.eldercare.dto.DispatchValidationResult;
import com.eldercare.dto.NurseCandidateVO;
import com.eldercare.entity.WorkOrder;
import com.eldercare.service.AuditLogService;
import com.eldercare.service.DispatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "工单调度派单接口")
@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DispatchController {

    private final DispatchService dispatchService;
    private final AuditLogService auditLogService;

    @PostMapping("/dispatch")
    @Operation(summary = "调度员派发工单（核心派单接口，执行六重校验）")
    public ApiResponse<WorkOrder> dispatch(@RequestBody DispatchCreateDTO dto) {
        try {
            WorkOrder order = dispatchService.dispatchWorkOrder(dto);
            return ApiResponse.success("工单派发成功", order);
        } catch (com.eldercare.common.exception.BusinessException e) {
            return ApiResponse.validationError(e.getMessage());
        }
    }

    @GetMapping("/demand/{demandId}/candidates")
    @Operation(summary = "查询可派单的护理员候选人（按匹配度排序）")
    public ApiResponse<List<NurseCandidateVO>> getCandidates(@PathVariable Long demandId) {
        return ApiResponse.success(dispatchService.getCandidateNurses(demandId));
    }

    @GetMapping("/preview-validation")
    @Operation(summary = "预览派单校验结果（不真正派单，调度端预览用）")
    public ApiResponse<DispatchValidationResult> previewValidation(
            @RequestParam Long demandId,
            @RequestParam Long nurseId) {
        return ApiResponse.success(dispatchService.previewValidation(demandId, nurseId));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "护理员接单")
    public ApiResponse<WorkOrder> acceptOrder(@PathVariable Long id,
                                              @RequestParam Long nurseId) {
        return ApiResponse.success("接单成功", dispatchService.acceptOrder(id, nurseId));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消工单")
    public ApiResponse<WorkOrder> cancelOrder(@PathVariable Long id,
                                              @RequestParam(required = false) String reason,
                                              @RequestParam(required = false, defaultValue = "DISPATCHER") String operator) {
        return ApiResponse.success("工单已取消", dispatchService.cancelOrder(id, reason, operator));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询工单详情")
    public ApiResponse<WorkOrder> getOrder(@PathVariable Long id) {
        return dispatchService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "工单不存在"));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "按编码查询工单")
    public ApiResponse<WorkOrder> getOrderByCode(@PathVariable String code) {
        return dispatchService.findByCode(code)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "工单不存在"));
    }

    @GetMapping
    @Operation(summary = "分页查询工单列表")
    public ApiResponse<PageResult<WorkOrder>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long nurseId,
            @RequestParam(required = false) Long elderId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<WorkOrder> result;
        if (status != null) {
            result = dispatchService.findByStatus(status, pageable);
        } else if (nurseId != null) {
            result = dispatchService.findByNurseId(nurseId, pageable);
        } else if (elderId != null) {
            result = new org.springframework.data.domain.PageImpl<>(
                    dispatchService.findByElderId(elderId), pageable, 0);
        } else {
            result = dispatchService.findAll(pageable);
        }
        PageResult<WorkOrder> pageResult = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pageResult);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "按状态查询工单列表")
    public ApiResponse<List<WorkOrder>> listByStatus(@PathVariable OrderStatus status) {
        return ApiResponse.success(dispatchService.findByStatus(
                status, PageRequest.of(0, 1000)).getContent());
    }

    @GetMapping("/nurse/{nurseId}")
    @Operation(summary = "查询护理员全部工单")
    public ApiResponse<List<WorkOrder>> listByNurse(@PathVariable Long nurseId) {
        return ApiResponse.success(dispatchService.findByNurseId(nurseId));
    }

    @GetMapping("/nurse/{nurseId}/active")
    @Operation(summary = "查询护理员待执行工单")
    public ApiResponse<List<WorkOrder>> listNurseActive(@PathVariable Long nurseId) {
        return ApiResponse.success(dispatchService.findNurseActiveOrders(nurseId));
    }

    @GetMapping("/nurse/{nurseId}/daily")
    @Operation(summary = "查询护理员当日工单排班")
    public ApiResponse<List<WorkOrder>> listNurseDaily(
            @PathVariable Long nurseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(dispatchService.findNurseDailyOrders(nurseId, date));
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(summary = "查询工单审计日志")
    public ApiResponse<?> getAuditLogs(@PathVariable Long id) {
        return ApiResponse.success(auditLogService.findByBusiness("WORK_ORDER", id));
    }
}
