package com.eldercare.controller;

import com.eldercare.common.Result;
import com.eldercare.entity.Caregiver;
import com.eldercare.service.CaregiverService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/caregivers")
public class CaregiverController {
    private final CaregiverService caregiverService;

    public CaregiverController(CaregiverService caregiverService) {
        this.caregiverService = caregiverService;
    }

    @GetMapping
    public Result<List<Caregiver>> findAll() {
        return Result.success(caregiverService.findAll());
    }

    @GetMapping("/active")
    public Result<List<Caregiver>> findActive() {
        return Result.success(caregiverService.findActiveCaregivers());
    }

    @GetMapping("/{id}")
    public Result<Caregiver> findById(@PathVariable Long id) {
        return caregiverService.findById(id)
                .map(Result::success)
                .orElse(Result.error("护理员信息不存在"));
    }

    @GetMapping("/search")
    public Result<List<Caregiver>> search(@RequestParam String name) {
        return Result.success(caregiverService.searchByName(name));
    }

    @GetMapping("/matched")
    public Result<List<Caregiver>> findMatched(
            @RequestParam(required = false) String qualifications,
            @RequestParam(required = false) String serviceType) {
        return Result.success(caregiverService.findMatchedCaregivers(qualifications, serviceType));
    }

    @PostMapping("/check-qualification")
    public Result<Boolean> checkQualification(@RequestBody Map<String, Object> params) {
        Long caregiverId = Long.valueOf(params.get("caregiverId").toString());
        String required = params.getOrDefault("requiredQualifications", "").toString();
        return Result.success(caregiverService.isQualificationMatched(caregiverId, required));
    }

    @PostMapping
    public Result<Caregiver> save(@RequestBody Caregiver caregiver) {
        return Result.success(caregiverService.save(caregiver));
    }

    @PutMapping("/{id}")
    public Result<Caregiver> update(@PathVariable Long id, @RequestBody Caregiver caregiver) {
        try {
            return Result.success(caregiverService.update(id, caregiver));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        caregiverService.delete(id);
        return Result.success();
    }
}
