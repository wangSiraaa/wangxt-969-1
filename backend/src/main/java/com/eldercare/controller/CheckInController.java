package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.dto.CheckInDTO;
import com.eldercare.dto.CheckOutDTO;
import com.eldercare.entity.CheckIn;
import com.eldercare.entity.ServiceRecord;
import com.eldercare.service.AuditLogService;
import com.eldercare.service.CheckInService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "签到签退接口")
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CheckInController {

    private final CheckInService checkInService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "护理员上门签到（含位置校验、迟到检测）")
    public ApiResponse<CheckIn> checkIn(@Valid @RequestBody CheckInDTO dto) {
        try {
            CheckIn ci = checkInService.checkIn(dto);
            return ApiResponse.success("签到成功，祝您服务顺利", ci);
        } catch (com.eldercare.common.exception.BusinessException e) {
            return ApiResponse.validationError(e.getMessage());
        }
    }

    @PostMapping("/checkout")
    @Operation(summary = "护理员签退并填写服务记录")
    public ApiResponse<ServiceRecord> checkOut(@RequestBody CheckOutDTO dto) {
        try {
            ServiceRecord record = checkInService.checkOut(dto);
            return ApiResponse.success("服务记录已保存，请等待家属确认", record);
        } catch (com.eldercare.common.exception.BusinessException e) {
            return ApiResponse.validationError(e.getMessage());
        }
    }

    @GetMapping("/work-order/{workOrderId}")
    @Operation(summary = "查询工单签到记录")
    public ApiResponse<CheckIn> getByWorkOrder(@PathVariable Long workOrderId) {
        return checkInService.findByWorkOrder(workOrderId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "签到记录不存在"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "按ID查询签到记录")
    public ApiResponse<CheckIn> getById(@PathVariable Long id) {
        return checkInService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "签到记录不存在"));
    }

    @GetMapping("/work-order/{workOrderId}/service-record")
    @Operation(summary = "查询工单服务记录")
    public ApiResponse<ServiceRecord> getServiceRecord(@PathVariable Long workOrderId) {
        return checkInService.findServiceRecordByWorkOrder(workOrderId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "服务记录不存在"));
    }
}
