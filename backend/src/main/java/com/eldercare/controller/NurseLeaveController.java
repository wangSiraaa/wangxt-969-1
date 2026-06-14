package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.dto.PageResult;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.entity.NurseLeave;
import com.eldercare.entity.WorkOrder;
import com.eldercare.repository.NurseLeaveRepository;
import com.eldercare.repository.WorkOrderRepository;
import com.eldercare.service.PackageBalanceService;
import com.eldercare.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@Tag(name = "护理员请假接口")
@RestController
@RequestMapping("/nurse-leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NurseLeaveController {

    private final NurseLeaveRepository leaveRepository;
    private final WorkOrderRepository workOrderRepository;
    private final PackageBalanceService packageBalanceService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "申请请假")
    @Transactional
    public ApiResponse<NurseLeave> createLeave(
            @RequestParam Long nurseId,
            @RequestParam String nurseName,
            @RequestParam String leaveType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam String reason,
            @RequestParam(required = false, defaultValue = "NURSE") String applicant,
            @RequestParam(required = false) String remark) {
        NurseLeave leave = new NurseLeave();
        leave.setLeaveCode("NLV" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        leave.setNurseId(nurseId);
        leave.setNurseName(nurseName);
        leave.setLeaveType(leaveType);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setStartTime(startTime);
        leave.setEndTime(endTime);
        leave.setTotalDays((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);
        if (startTime != null && endTime != null) {
            long hours = ChronoUnit.HOURS.between(startTime, endTime);
            leave.setTotalHours((int) hours);
        }
        leave.setReason(reason);
        leave.setStatus("PENDING_APPROVAL");
        leave.setRemark(remark);
        leave = leaveRepository.save(leave);

        log.info("护理员请假申请: leaveCode={}, nurse={}, type={}, start={}, end={}",
                leave.getLeaveCode(), nurseName, leaveType, startDate, endDate);
        return ApiResponse.success("请假申请已提交，等待审批", leave);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审批请假")
    @Transactional
    public ApiResponse<NurseLeave> approveLeave(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "ADMIN") String approver,
            @RequestParam(required = false) String remark) {
        NurseLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在: " + id));

        if (!"PENDING_APPROVAL".equals(leave.getStatus())) {
            throw new IllegalStateException("当前状态不允许审批: " + leave.getStatus());
        }

        String before = leave.toString();
        leave.setStatus("APPROVED");
        leave.setApprovedBy(approver);
        leave.setApprovedAt(java.time.LocalDateTime.now());
        if (remark != null) {
            leave.setRemark(remark);
        }
        leave = leaveRepository.save(leave);

        try {
            List<WorkOrder> affectedOrders = findAffectedOrders(leave);
            for (WorkOrder order : affectedOrders) {
                try {
                    packageBalanceService.handleNurseLeave(order.getId(),
                            leave.getNurseId(),
                            leave.getStartDate().atStartOfDay(),
                            leave.getEndDate().atTime(LocalTime.MAX),
                            leave.getReason(), approver);
                } catch (Exception e) {
                    log.warn("处理请假关联工单失败: orderId={}, error={}", order.getId(), e.getMessage());
                }
            }
            leave.setReassignmentCount(affectedOrders.size());
            leaveRepository.save(leave);
        } catch (Exception e) {
            log.warn("处理请假关联工单异常: {}", e.getMessage());
        }

        auditLogService.logNurseLeave(leave.getId(), leave.getLeaveCode(),
                "APPROVE", approver, before, leave.toString());

        log.info("请假审批通过: leaveCode={}, approver={}, affectedOrders={}",
                leave.getLeaveCode(), approver, leave.getReassignmentCount());
        return ApiResponse.success("请假已批准", leave);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "驳回请假")
    @Transactional
    public ApiResponse<NurseLeave> rejectLeave(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "ADMIN") String approver,
            @RequestParam String rejectReason) {
        NurseLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在: " + id));

        if (!"PENDING_APPROVAL".equals(leave.getStatus())) {
            throw new IllegalStateException("当前状态不允许审批: " + leave.getStatus());
        }

        String before = leave.toString();
        leave.setStatus("REJECTED");
        leave.setApprovedBy(approver);
        leave.setApprovedAt(java.time.LocalDateTime.now());
        leave.setRemark(rejectReason);
        leave = leaveRepository.save(leave);

        auditLogService.logNurseLeave(leave.getId(), leave.getLeaveCode(),
                "REJECT", approver, before, leave.toString());

        log.info("请假被驳回: leaveCode={}, approver={}, reason={}",
                leave.getLeaveCode(), approver, rejectReason);
        return ApiResponse.success("请假已驳回", leave);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消请假")
    @Transactional
    public ApiResponse<NurseLeave> cancelLeave(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "NURSE") String operator) {
        NurseLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在: " + id));

        if ("CANCELLED".equals(leave.getStatus()) || "REJECTED".equals(leave.getStatus())) {
            throw new IllegalStateException("当前状态不允许取消: " + leave.getStatus());
        }

        String before = leave.toString();
        leave.setStatus("CANCELLED");
        leave = leaveRepository.save(leave);

        auditLogService.logNurseLeave(leave.getId(), leave.getLeaveCode(),
                "CANCEL", operator, before, leave.toString());

        log.info("请假已取消: leaveCode={}, operator={}", leave.getLeaveCode(), operator);
        return ApiResponse.success("请假已取消", leave);
    }

    @GetMapping
    @Operation(summary = "分页查询请假记录")
    public ApiResponse<PageResult<NurseLeave>> list(
            @RequestParam(required = false) Long nurseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<NurseLeave> result;
        if (nurseId != null) {
            result = leaveRepository.findByNurseId(nurseId, pageable);
        } else {
            result = leaveRepository.findAll(pageable);
        }
        PageResult<NurseLeave> pr = new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size);
        return ApiResponse.success(pr);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询请假详情")
    public ApiResponse<NurseLeave> getById(@PathVariable Long id) {
        return leaveRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "请假记录不存在"));
    }

    @GetMapping("/nurse/{nurseId}")
    @Operation(summary = "查询护理员的请假记录")
    public ApiResponse<List<NurseLeave>> getByNurse(@PathVariable Long nurseId) {
        return ApiResponse.success(leaveRepository.findByNurseIdOrderByStartDateDesc(nurseId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "按状态查询请假记录")
    public ApiResponse<List<NurseLeave>> getByStatus(@PathVariable String status) {
        return ApiResponse.success(leaveRepository.findByStatusOrderByStartDateDesc(status));
    }

    @GetMapping("/date-range")
    @Operation(summary = "按日期范围查询请假记录")
    public ApiResponse<List<NurseLeave>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(leaveRepository.findByStartDateBetweenOrderByStartDateDesc(startDate, endDate));
    }

    @GetMapping("/{id}/affected-orders")
    @Operation(summary = "查询请假影响的工单")
    public ApiResponse<List<WorkOrder>> getAffectedOrders(@PathVariable Long id) {
        NurseLeave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("请假记录不存在: " + id));
        return ApiResponse.success(findAffectedOrders(leave));
    }

    private List<WorkOrder> findAffectedOrders(NurseLeave leave) {
        List<WorkOrder> result = new ArrayList<>();
        LocalDate date = leave.getStartDate();
        while (!date.isAfter(leave.getEndDate())) {
            List<WorkOrder> orders = workOrderRepository.findByNurseIdAndScheduledDate(
                    leave.getNurseId(), date);
            for (WorkOrder order : orders) {
                if (order.getStatus() != OrderStatus.CANCELLED
                        && order.getStatus() != OrderStatus.ABNORMAL) {
                    result.add(order);
                }
            }
            date = date.plusDays(1);
        }
        return result;
    }
}
