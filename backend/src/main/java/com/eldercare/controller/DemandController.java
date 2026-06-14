package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.dto.PageQuery;
import com.eldercare.common.dto.PageResult;
import com.eldercare.dto.DemandCreateDTO;
import com.eldercare.entity.DemandVersion;
import com.eldercare.entity.ServiceDemand;
import com.eldercare.service.AuditLogService;
import com.eldercare.service.DemandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "服务需求接口")
@RestController
@RequestMapping("/demands")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DemandController {

    private final DemandService demandService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "老人/家属提交服务需求")
    public ApiResponse<ServiceDemand> createDemand(@Valid @RequestBody DemandCreateDTO dto) {
        ServiceDemand demand = demandService.createDemand(dto);
        return ApiResponse.success("需求提交成功，等待调度派单", demand);
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改服务需求（未派单前）")
    public ApiResponse<ServiceDemand> updateDemand(@PathVariable Long id,
                                                   @RequestBody DemandCreateDTO dto) {
        return ApiResponse.success("需求更新成功", demandService.updateDemand(id, dto));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消服务需求")
    public ApiResponse<ServiceDemand> cancelDemand(@PathVariable Long id,
                                                   @RequestParam(required = false) String reason,
                                                   @RequestParam(required = false, defaultValue = "FAMILY") String operator) {
        return ApiResponse.success("需求已取消", demandService.cancelDemand(id, reason, operator));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询需求详情")
    public ApiResponse<ServiceDemand> getDemand(@PathVariable Long id) {
        return demandService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "需求不存在"));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "按编码查询需求")
    public ApiResponse<ServiceDemand> getDemandByCode(@PathVariable String code) {
        return demandService.findByCode(code)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "需求不存在"));
    }

    @GetMapping
    @Operation(summary = "分页查询需求列表")
    public ApiResponse<PageResult<ServiceDemand>> listDemands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long elderId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt"));
        Page<ServiceDemand> result;
        if (status != null && !status.isEmpty()) {
            result = new org.springframework.data.domain.PageImpl<>(demandService.findByStatus(status), pageable, 0);
        } else if (elderId != null) {
            result = new org.springframework.data.domain.PageImpl<>(demandService.findByElder(elderId), pageable, 0);
        } else {
            result = demandService.findAll(pageable);
        }
        PageResult<ServiceDemand> pageResult = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pageResult);
    }

    @GetMapping("/pending-dispatch")
    @Operation(summary = "查询待派单需求列表（调度端用）")
    public ApiResponse<List<ServiceDemand>> listPendingDispatch() {
        return ApiResponse.success(demandService.findPendingDispatch());
    }

    @GetMapping("/elder/{elderId}")
    @Operation(summary = "查询老人历史需求")
    public ApiResponse<List<ServiceDemand>> listByElder(@PathVariable Long elderId) {
        return ApiResponse.success(demandService.findByElder(elderId));
    }

    @GetMapping("/{id}/versions")
    @Operation(summary = "查询需求历史版本")
    public ApiResponse<List<DemandVersion>> getVersions(@PathVariable Long id) {
        return ApiResponse.success(demandService.getDemandVersions(id));
    }

    @GetMapping("/{id}/audit-logs")
    @Operation(summary = "查询需求审计日志")
    public ApiResponse<?> getAuditLogs(@PathVariable Long id) {
        return ApiResponse.success(auditLogService.findByBusiness("SERVICE_DEMAND", id));
    }
}
