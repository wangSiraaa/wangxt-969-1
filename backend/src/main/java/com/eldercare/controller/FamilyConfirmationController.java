package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.dto.FamilyConfirmDTO;
import com.eldercare.entity.FamilyConfirmation;
import com.eldercare.service.FamilyConfirmationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "家属确认接口")
@RestController
@RequestMapping("/family-confirmation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FamilyConfirmationController {

    private final FamilyConfirmationService confirmationService;

    @PostMapping
    @Operation(summary = "家属确认/拒绝确认服务")
    public ApiResponse<FamilyConfirmation> confirm(@RequestBody FamilyConfirmDTO dto) {
        FamilyConfirmation fc = confirmationService.confirmService(dto);
        String msg = Boolean.TRUE.equals(dto.getConfirmed())
                ? "家属已确认服务，感谢您的反馈"
                : "已记录拒绝确认，将进入异常处置流程";
        return ApiResponse.success(msg, fc);
    }

    @GetMapping("/work-order/{workOrderId}")
    @Operation(summary = "查询工单家属确认情况")
    public ApiResponse<FamilyConfirmation> getByWorkOrder(@PathVariable Long workOrderId) {
        return confirmationService.findByWorkOrder(workOrderId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "确认记录不存在（可能尚未确认）"));
    }

    @GetMapping("/{id}")
    public ApiResponse<FamilyConfirmation> getById(@PathVariable Long id) {
        return confirmationService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "记录不存在"));
    }
}
