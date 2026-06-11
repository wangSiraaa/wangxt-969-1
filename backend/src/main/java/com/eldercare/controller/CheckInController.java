package com.eldercare.controller;

import com.eldercare.common.Result;
import com.eldercare.entity.CheckInRecord;
import com.eldercare.service.CheckInService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/checkins")
public class CheckInController {
    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @GetMapping
    public Result<List<CheckInRecord>> findAll() {
        return Result.success(checkInService.findAll());
    }

    @GetMapping("/{id}")
    public Result<CheckInRecord> findById(@PathVariable Long id) {
        return checkInService.findById(id)
                .map(Result::success)
                .orElse(Result.error("签到记录不存在"));
    }

    @GetMapping("/workorder/{workOrderId}")
    public Result<List<CheckInRecord>> findByWorkOrderId(@PathVariable Long workOrderId) {
        return Result.success(checkInService.findByWorkOrderId(workOrderId));
    }

    @GetMapping("/workorder/{workOrderId}/latest")
    public Result<CheckInRecord> findLatestByWorkOrderId(@PathVariable Long workOrderId) {
        return checkInService.findLatestByWorkOrderId(workOrderId)
                .map(Result::success)
                .orElse(Result.error("未找到签到记录"));
    }

    @GetMapping("/caregiver/{caregiverId}")
    public Result<List<CheckInRecord>> findByCaregiverId(@PathVariable Long caregiverId) {
        return Result.success(checkInService.findByCaregiverId(caregiverId));
    }

    @GetMapping("/elder/{elderId}")
    public Result<List<CheckInRecord>> findByElderId(@PathVariable Long elderId) {
        return Result.success(checkInService.findByElderId(elderId));
    }

    @GetMapping("/workorder/{workOrderId}/exists")
    public Result<Boolean> hasCheckedIn(@PathVariable Long workOrderId) {
        return Result.success(checkInService.hasCheckedIn(workOrderId));
    }

    @PostMapping("/checkin")
    public Result<CheckInRecord> checkIn(@RequestBody Map<String, Object> params) {
        try {
            Long workOrderId = Long.valueOf(params.get("workOrderId").toString());
            String location = params.getOrDefault("location",
                    params.getOrDefault("checkInLocation", "")).toString();
            String signImage = params.getOrDefault("signImageUrl", "").toString();
            return Result.success(checkInService.checkIn(workOrderId, location, signImage));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/checkout")
    public Result<CheckInRecord> checkOut(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String serviceRecord = params.getOrDefault("serviceRecord", "");
            String elderCondition = params.getOrDefault("elderCondition", "");
            String caregiverRemark = params.getOrDefault("caregiverRemark", "");
            return Result.success(checkInService.checkOut(id, serviceRecord, elderCondition, caregiverRemark));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<CheckInRecord> update(@PathVariable Long id, @RequestBody CheckInRecord record) {
        try {
            return Result.success(checkInService.updateRecord(id, record));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkInService.delete(id);
        return Result.success();
    }
}
