package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.dto.DashboardStatisticsVO;
import com.eldercare.service.AbnormalEventService;
import com.eldercare.service.DashboardService;
import com.eldercare.service.DemandService;
import com.eldercare.service.DispatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "仪表板接口")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DemandService demandService;
    private final DispatchService dispatchService;
    private final AbnormalEventService abnormalEventService;

    @GetMapping("/statistics")
    @Operation(summary = "获取仪表板统计数据")
    public ApiResponse<DashboardStatisticsVO> getStatistics() {
        return ApiResponse.success(dashboardService.getStatistics());
    }

    @GetMapping("/summary")
    @Operation(summary = "获取简要统计概览")
    public ApiResponse<?> getSummary() {
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("totalDemands", demandService.countAll());
        summary.put("pendingDispatch", demandService.countByStatus("PENDING_DISPATCH"));
        summary.put("pendingAbnormals", abnormalEventService.findPending().size());
        summary.put("statistics", dashboardService.getStatistics());
        return ApiResponse.success(summary);
    }

    @PostMapping("/detect-abnormals")
    @Operation(summary = "手动触发异常检测（超时、未签退等）")
    public ApiResponse<String> detectAbnormals() {
        abnormalEventService.detectAndCreateTimeoutAbnormals();
        return ApiResponse.success("已执行异常检测任务", "DONE");
    }
}
