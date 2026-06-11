package com.eldercare.controller;

import com.eldercare.common.Result;
import com.eldercare.entity.WorkOrder;
import com.eldercare.service.WorkOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workorders")
public class WorkOrderController {
    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public Result<List<WorkOrder>> findAll() {
        return Result.success(workOrderService.findAll());
    }

    @GetMapping("/{id}")
    public Result<WorkOrder> findById(@PathVariable Long id) {
        return workOrderService.findById(id)
                .map(Result::success)
                .orElse(Result.error("工单不存在"));
    }

    @GetMapping("/demand/{demandId}")
    public Result<List<WorkOrder>> findByDemandId(@PathVariable Long demandId) {
        return Result.success(workOrderService.findByDemandId(demandId));
    }

    @GetMapping("/caregiver/{caregiverId}")
    public Result<List<WorkOrder>> findByCaregiverId(@PathVariable Long caregiverId) {
        return Result.success(workOrderService.findByCaregiverId(caregiverId));
    }

    @GetMapping("/elder/{elderId}")
    public Result<List<WorkOrder>> findByElderId(@PathVariable Long elderId) {
        return Result.success(workOrderService.findByElderId(elderId));
    }

    @GetMapping("/status/{status}")
    public Result<List<WorkOrder>> findByOrderStatus(@PathVariable String status) {
        return Result.success(workOrderService.findByOrderStatus(status));
    }

    @PostMapping("/dispatch")
    public Result<WorkOrder> dispatch(@RequestBody Map<String, Object> params) {
        try {
            Long demandId = Long.valueOf(params.get("demandId").toString());
            Long caregiverId = Long.valueOf(params.get("caregiverId").toString());
            String dispatcher = params.getOrDefault("dispatcher", "").toString();
            String remark = params.getOrDefault("dispatchRemark", "").toString();
            return Result.success(workOrderService.dispatch(demandId, caregiverId, dispatcher, remark));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<WorkOrder> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String status = params.get("orderStatus");
            return Result.success(workOrderService.updateOrderStatus(id, status));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workOrderService.delete(id);
        return Result.success();
    }
}
