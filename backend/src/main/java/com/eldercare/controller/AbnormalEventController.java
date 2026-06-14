package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.enums.AbnormalType;
import com.eldercare.dto.AbnormalHandleDTO;
import com.eldercare.entity.AbnormalEvent;
import com.eldercare.service.AbnormalEventService;
import com.eldercare.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "异常处置面板接口")
@RestController
@RequestMapping("/abnormal-events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AbnormalEventController {

    private final AbnormalEventService abnormalEventService;
    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "查询全部异常事件（异常处置面板）")
    public ApiResponse<List<AbnormalEvent>> listAll(
            @RequestParam(required = false) AbnormalType type,
            @RequestParam(required = false) String status) {
        List<AbnormalEvent> list;
        if (type != null) {
            list = abnormalEventService.findByType(type);
        } else if ("PENDING".equals(status)) {
            list = abnormalEventService.findPending();
        } else {
            list = abnormalEventService.findAll();
        }
        return ApiResponse.success(list);
    }

    @GetMapping("/pending")
    @Operation(summary = "查询待处理异常队列")
    public ApiResponse<List<AbnormalEvent>> listPending() {
        return ApiResponse.success(abnormalEventService.findPending());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询异常事件详情")
    public ApiResponse<AbnormalEvent> getById(@PathVariable Long id) {
        return abnormalEventService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "异常事件不存在"));
    }

    @GetMapping("/work-order/{workOrderId}")
    @Operation(summary = "查询工单调取的异常事件")
    public ApiResponse<List<AbnormalEvent>> listByWorkOrder(@PathVariable Long workOrderId) {
        return ApiResponse.success(abnormalEventService.findByWorkOrder(workOrderId));
    }

    @PostMapping("/{id}/handle")
    @Operation(summary = "开始处置异常（分配处理人）")
    public ApiResponse<AbnormalEvent> handle(@PathVariable Long id,
                                             @RequestBody AbnormalHandleDTO dto) {
        dto.setAbnormalEventId(id);
        return ApiResponse.success("已开始处置", abnormalEventService.handleAbnormal(dto));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "完成异常处置")
    public ApiResponse<AbnormalEvent> resolve(
            @PathVariable Long id,
            @RequestParam(required = false) Long handlerId,
            @RequestParam(required = false) String handlerName,
            @RequestParam String resolution,
            @RequestParam(required = false) String handlingNotes) {
        if (handlerName == null) handlerName = "OPERATOR";
        return ApiResponse.success("异常已解决",
                abnormalEventService.resolveAbnormal(id, handlerId, handlerName, resolution, handlingNotes));
    }

    @GetMapping("/{id}/audit-logs")
    public ApiResponse<?> getAuditLogs(@PathVariable Long id) {
        return ApiResponse.success(auditLogService.findByBusiness("ABNORMAL_EVENT", id));
    }
}
