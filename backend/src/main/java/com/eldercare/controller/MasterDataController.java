package com.eldercare.controller;

import com.eldercare.common.dto.ApiResponse;
import com.eldercare.common.dto.PageResult;
import com.eldercare.dto.SupplementOrderDTO;
import com.eldercare.entity.*;
import com.eldercare.service.MasterDataService;
import com.eldercare.service.SupplementOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "基础数据与补单审批接口")
@RestController
@RequestMapping("/master-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MasterDataController {

    private final MasterDataService masterDataService;
    private final SupplementOrderService supplementOrderService;

    @GetMapping("/elders")
    @Operation(summary = "分页查询老人档案")
    public ApiResponse<PageResult<Elder>> listElders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Elder> result = masterDataService.findAllElders(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.success(new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size));
    }

    @GetMapping("/elders/active")
    public ApiResponse<List<Elder>> listActiveElders() {
        return ApiResponse.success(masterDataService.findActiveElders());
    }

    @GetMapping("/elders/{id}")
    public ApiResponse<Elder> getElder(@PathVariable Long id) {
        return masterDataService.findElderById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "老人档案不存在"));
    }

    @GetMapping("/elders/{id}/emergency-contacts")
    @Operation(summary = "查询老人紧急联系人")
    public ApiResponse<List<EmergencyContact>> getEmergencyContacts(@PathVariable Long id) {
        return ApiResponse.success(masterDataService.findEmergencyContacts(id));
    }

    @GetMapping("/nurses")
    @Operation(summary = "分页查询护理员档案")
    public ApiResponse<PageResult<Nurse>> listNurses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Nurse> result = masterDataService.findAllNurses(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ApiResponse.success(new PageResult<>(
                result.getContent(), result.getTotalElements(), result.getTotalPages(), page, size));
    }

    @GetMapping("/nurses/active")
    public ApiResponse<List<Nurse>> listActiveNurses() {
        return ApiResponse.success(masterDataService.findActiveNurses());
    }

    @GetMapping("/nurses/{id}")
    public ApiResponse<Nurse> getNurse(@PathVariable Long id) {
        return masterDataService.findNurseById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "护理员档案不存在"));
    }

    @GetMapping("/nurses/{id}/qualifications")
    @Operation(summary = "查询护理员资质列表")
    public ApiResponse<List<NurseQualification>> getQualifications(@PathVariable Long id) {
        return ApiResponse.success(masterDataService.findNurseQualifications(id));
    }

    @GetMapping("/nursing-levels")
    @Operation(summary = "查询护理等级字典")
    public ApiResponse<List<NursingLevel>> listNursingLevels() {
        return ApiResponse.success(masterDataService.findAllNursingLevels());
    }

    @GetMapping("/service-packages")
    @Operation(summary = "查询服务包字典")
    public ApiResponse<List<ServicePackage>> listServicePackages() {
        return ApiResponse.success(masterDataService.findAllServicePackages());
    }

    @GetMapping("/service-packages/{id}")
    public ApiResponse<ServicePackage> getServicePackage(@PathVariable Long id) {
        return masterDataService.findServicePackageById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "服务包不存在"));
    }

    @GetMapping("/contraindications")
    @Operation(summary = "查询禁忌事项字典")
    public ApiResponse<List<Contraindication>> listContraindications() {
        return ApiResponse.success(masterDataService.findAllContraindications());
    }

    @PostMapping("/supplement-orders")
    @Operation(summary = "提交补单申请")
    public ApiResponse<SupplementOrder> applySupplement(@RequestBody SupplementOrderDTO dto) {
        return ApiResponse.success("补单申请已提交，等待审批",
                supplementOrderService.applySupplement(dto));
    }

    @PostMapping("/supplement-orders/{id}/approve")
    @Operation(summary = "审批补单")
    public ApiResponse<SupplementOrder> approveSupplement(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false, defaultValue = "MANAGER") String approver,
            @RequestParam(required = false) String approvalRemark) {
        String msg = approved ? "补单审批通过，需求已恢复为待派单状态" : "补单申请已拒绝";
        return ApiResponse.success(msg,
                supplementOrderService.approveSupplement(id, approved, approver, approvalRemark));
    }

    @GetMapping("/supplement-orders/{id}")
    public ApiResponse<SupplementOrder> getSupplement(@PathVariable Long id) {
        return supplementOrderService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "补单申请不存在"));
    }

    @GetMapping("/supplement-orders")
    @Operation(summary = "查询补单申请列表（按状态过滤）")
    public ApiResponse<List<SupplementOrder>> listSupplements(
            @RequestParam(required = false) String approvalStatus) {
        List<SupplementOrder> list = approvalStatus != null
                ? supplementOrderService.findByApprovalStatus(approvalStatus)
                : supplementOrderService.findByApprovalStatus("PENDING");
        return ApiResponse.success(list);
    }
}
