package com.eldercare.controller;

import com.eldercare.common.Result;
import com.eldercare.entity.ServiceDemand;
import com.eldercare.enums.DemandStatus;
import com.eldercare.service.ServiceDemandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/demands")
public class ServiceDemandController {
    private final ServiceDemandService demandService;

    public ServiceDemandController(ServiceDemandService demandService) {
        this.demandService = demandService;
    }

    @GetMapping
    public Result<List<ServiceDemand>> findAll() {
        return Result.success(demandService.findAll());
    }

    @GetMapping("/{id}")
    public Result<ServiceDemand> findById(@PathVariable Long id) {
        return demandService.findById(id)
                .map(Result::success)
                .orElse(Result.error("服务需求不存在"));
    }

    @GetMapping("/status/{status}")
    public Result<List<ServiceDemand>> findByStatus(@PathVariable DemandStatus status) {
        return Result.success(demandService.findByStatus(status));
    }

    @GetMapping("/elder/{elderId}")
    public Result<List<ServiceDemand>> findByElderId(@PathVariable Long elderId) {
        return Result.success(demandService.findByElderId(elderId));
    }

    @PostMapping
    public Result<ServiceDemand> save(@RequestBody ServiceDemand demand) {
        try {
            return Result.success(demandService.save(demand));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<ServiceDemand> update(@PathVariable Long id, @RequestBody ServiceDemand demand) {
        try {
            return Result.success(demandService.update(id, demand));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<ServiceDemand> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            DemandStatus status = DemandStatus.valueOf(params.get("status"));
            return Result.success(demandService.updateStatus(id, status));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<ServiceDemand> cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params) {
        try {
            String reason = params != null ? params.getOrDefault("reason", "") : "";
            return Result.success(demandService.cancel(id, reason));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        demandService.delete(id);
        return Result.success();
    }
}
