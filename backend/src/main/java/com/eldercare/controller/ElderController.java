package com.eldercare.controller;

import com.eldercare.common.Result;
import com.eldercare.entity.Elder;
import com.eldercare.enums.ElderStatus;
import com.eldercare.service.ElderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/elders")
public class ElderController {
    private final ElderService elderService;

    public ElderController(ElderService elderService) {
        this.elderService = elderService;
    }

    @GetMapping
    public Result<List<Elder>> findAll() {
        return Result.success(elderService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Elder> findById(@PathVariable Long id) {
        return elderService.findById(id)
                .map(Result::success)
                .orElse(Result.error("老人信息不存在"));
    }

    @GetMapping("/status/{status}")
    public Result<List<Elder>> findByStatus(@PathVariable ElderStatus status) {
        return Result.success(elderService.findByStatus(status));
    }

    @GetMapping("/search")
    public Result<List<Elder>> search(@RequestParam String name) {
        return Result.success(elderService.searchByName(name));
    }

    @PostMapping
    public Result<Elder> save(@RequestBody Elder elder) {
        try {
            return Result.success(elderService.save(elder));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Elder> update(@PathVariable Long id, @RequestBody Elder elder) {
        try {
            return Result.success(elderService.update(id, elder));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/suspend")
    public Result<Elder> suspend(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            String reason = params.getOrDefault("reason", "");
            return Result.success(elderService.suspendService(id, reason));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/resume")
    public Result<Elder> resume(@PathVariable Long id) {
        try {
            return Result.success(elderService.resumeService(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        elderService.delete(id);
        return Result.success();
    }
}
