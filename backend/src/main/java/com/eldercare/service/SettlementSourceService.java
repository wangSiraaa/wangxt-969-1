package com.eldercare.service;

import com.eldercare.entity.Settlement;
import com.eldercare.entity.SettlementSource;
import com.eldercare.entity.WorkOrder;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementSourceService {

    private final SettlementSourceRepository sourceRepository;
    private final SettlementRepository settlementRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final ElderRepository elderRepository;
    private final NurseRepository nurseRepository;

    @Transactional
    public SettlementSource createSource(SettlementSource source) {
        if (source.getSourceCode() == null || source.getSourceCode().isEmpty()) {
            source.setSourceCode("SRC" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        }
        source = sourceRepository.save(source);
        log.info("创建结算来源: sourceCode={}, settlementId={}, type={}",
                source.getSourceCode(), source.getSettlementId(), source.getSourceType());
        return source;
    }

    @Transactional
    public List<SettlementSource> generateSettlementSources(Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("结算单不存在: " + settlementId));

        List<SettlementSource> sources = new ArrayList<>();

        if (settlement.getBaseAmount() != null && settlement.getBaseAmount().compareTo(BigDecimal.ZERO) > 0) {
            SettlementSource baseSource = new SettlementSource();
            baseSource.setSourceCode("SRC" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
            baseSource.setSettlementId(settlementId);
            baseSource.setSettlementCode(settlement.getSettlementCode());
            baseSource.setWorkOrderId(settlement.getWorkOrderId());
            baseSource.setOrderCode(settlement.getOrderCode());
            baseSource.setElderId(settlement.getElderId());
            baseSource.setElderName(settlement.getElderName());
            baseSource.setNurseId(settlement.getNurseId());
            baseSource.setNurseName(settlement.getNurseName());
            baseSource.setServicePackageId(settlement.getServicePackageId());
            baseSource.setServicePackageName(settlement.getServicePackageName());
            baseSource.setServiceDate(settlement.getServiceDate());
            baseSource.setSourceType("BASE_SERVICE");
            baseSource.setServiceItemCode("BASE_" + settlement.getServicePackageId());
            baseSource.setServiceItemName("基础服务费");
            baseSource.setQuantity(BigDecimal.ONE);
            baseSource.setUnit("次");
            baseSource.setUnitPrice(settlement.getBaseAmount());
            baseSource.setAmount(settlement.getBaseAmount());
            baseSource.setPackageDeducted(true);
            sources.add(baseSource);
        }

        if (settlement.getExtraAmount() != null && settlement.getExtraAmount().compareTo(BigDecimal.ZERO) > 0) {
            SettlementSource extraSource = new SettlementSource();
            extraSource.setSourceCode("SRC" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
            extraSource.setSettlementId(settlementId);
            extraSource.setSettlementCode(settlement.getSettlementCode());
            extraSource.setWorkOrderId(settlement.getWorkOrderId());
            extraSource.setOrderCode(settlement.getOrderCode());
            extraSource.setElderId(settlement.getElderId());
            extraSource.setElderName(settlement.getElderName());
            extraSource.setNurseId(settlement.getNurseId());
            extraSource.setNurseName(settlement.getNurseName());
            extraSource.setServicePackageId(settlement.getServicePackageId());
            extraSource.setServicePackageName(settlement.getServicePackageName());
            extraSource.setServiceDate(settlement.getServiceDate());
            extraSource.setSourceType("EXTRA_CHARGE");
            extraSource.setServiceItemCode("EXTRA");
            extraSource.setServiceItemName("额外费用");
            extraSource.setIsExtraCharge(true);
            extraSource.setQuantity(BigDecimal.ONE);
            extraSource.setUnit("次");
            extraSource.setUnitPrice(settlement.getExtraAmount());
            extraSource.setAmount(settlement.getExtraAmount());
            sources.add(extraSource);
        }

        if (settlement.getDeductAmount() != null && settlement.getDeductAmount().compareTo(BigDecimal.ZERO) > 0) {
            SettlementSource deductSource = new SettlementSource();
            deductSource.setSourceCode("SRC" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
            deductSource.setSettlementId(settlementId);
            deductSource.setSettlementCode(settlement.getSettlementCode());
            deductSource.setWorkOrderId(settlement.getWorkOrderId());
            deductSource.setOrderCode(settlement.getOrderCode());
            deductSource.setElderId(settlement.getElderId());
            deductSource.setElderName(settlement.getElderName());
            deductSource.setNurseId(settlement.getNurseId());
            deductSource.setNurseName(settlement.getNurseName());
            deductSource.setServicePackageId(settlement.getServicePackageId());
            deductSource.setServicePackageName(settlement.getServicePackageName());
            deductSource.setServiceDate(settlement.getServiceDate());
            deductSource.setSourceType("DEDUCTION");
            deductSource.setServiceItemCode("DEDUCT");
            deductSource.setServiceItemName("扣款");
            deductSource.setIsDeduction(true);
            deductSource.setQuantity(BigDecimal.ONE);
            deductSource.setUnit("次");
            deductSource.setUnitPrice(settlement.getDeductAmount().negate());
            deductSource.setAmount(settlement.getDeductAmount().negate());
            sources.add(deductSource);
        }

        if (settlement.getInsuranceAmount() != null && settlement.getInsuranceAmount().compareTo(BigDecimal.ZERO) > 0) {
            SettlementSource insuranceSource = new SettlementSource();
            insuranceSource.setSourceCode("SRC" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
            insuranceSource.setSettlementId(settlementId);
            insuranceSource.setSettlementCode(settlement.getSettlementCode());
            insuranceSource.setWorkOrderId(settlement.getWorkOrderId());
            insuranceSource.setOrderCode(settlement.getOrderCode());
            insuranceSource.setElderId(settlement.getElderId());
            insuranceSource.setElderName(settlement.getElderName());
            insuranceSource.setNurseId(settlement.getNurseId());
            insuranceSource.setNurseName(settlement.getNurseName());
            insuranceSource.setServicePackageId(settlement.getServicePackageId());
            insuranceSource.setServicePackageName(settlement.getServicePackageName());
            insuranceSource.setServiceDate(settlement.getServiceDate());
            insuranceSource.setSourceType("INSURANCE");
            insuranceSource.setServiceItemCode("INSURANCE");
            insuranceSource.setServiceItemName("保险报销");
            insuranceSource.setQuantity(BigDecimal.ONE);
            insuranceSource.setUnit("次");
            insuranceSource.setUnitPrice(settlement.getInsuranceAmount());
            insuranceSource.setAmount(settlement.getInsuranceAmount());
            sources.add(insuranceSource);
        }

        for (SettlementSource source : sources) {
            sourceRepository.save(source);
        }

        log.info("生成结算来源明细: settlementId={}, count={}", settlementId, sources.size());
        return sources;
    }

    @Transactional
    public SettlementSource addAbnormalSource(Long settlementId, Long abnormalEventId,
                                               String abnormalType, BigDecimal amount,
                                               String description) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("结算单不存在: " + settlementId));

        SettlementSource source = new SettlementSource();
        source.setSourceCode("SRC" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(4));
        source.setSettlementId(settlementId);
        source.setSettlementCode(settlement.getSettlementCode());
        source.setWorkOrderId(settlement.getWorkOrderId());
        source.setOrderCode(settlement.getOrderCode());
        source.setElderId(settlement.getElderId());
        source.setElderName(settlement.getElderName());
        source.setNurseId(settlement.getNurseId());
        source.setNurseName(settlement.getNurseName());
        source.setServicePackageId(settlement.getServicePackageId());
        source.setServicePackageName(settlement.getServicePackageName());
        source.setServiceDate(settlement.getServiceDate());
        source.setSourceType("ABNORMAL");
        source.setAbnormalType(abnormalType);
        source.setAbnormalEventId(abnormalEventId);
        source.setServiceItemCode("ABN_" + abnormalType);
        source.setServiceItemName(description != null ? description : "异常调整");
        source.setQuantity(BigDecimal.ONE);
        source.setUnit("次");
        source.setUnitPrice(amount);
        source.setAmount(amount);
        source.setRemark(description);
        source = sourceRepository.save(source);

        log.info("添加异常结算来源: sourceCode={}, settlementId={}, abnormalType={}, amount={}",
                source.getSourceCode(), settlementId, abnormalType, amount);
        return source;
    }

    public List<SettlementSource> getSourcesBySettlement(Long settlementId) {
        return sourceRepository.findBySettlementIdOrderByCreatedAtDesc(settlementId);
    }

    public List<SettlementSource> getSourcesBySettlementCode(String settlementCode) {
        return sourceRepository.findBySettlementCodeOrderByCreatedAtDesc(settlementCode);
    }

    public List<SettlementSource> getSourcesByWorkOrder(Long workOrderId) {
        return sourceRepository.findByWorkOrderIdOrderByCreatedAtDesc(workOrderId);
    }

    public List<SettlementSource> getSourcesByElder(Long elderId) {
        return sourceRepository.findByElderIdOrderByServiceDateDesc(elderId);
    }

    public Page<SettlementSource> getSourcesByElder(Long elderId, Pageable pageable) {
        return sourceRepository.findByElderId(elderId, pageable);
    }

    public List<SettlementSource> getSourcesByNurse(Long nurseId) {
        return sourceRepository.findByNurseIdOrderByServiceDateDesc(nurseId);
    }

    public Page<SettlementSource> getSourcesByNurse(Long nurseId, Pageable pageable) {
        return sourceRepository.findByNurseId(nurseId, pageable);
    }

    public List<SettlementSource> getSourcesByServicePackage(Long servicePackageId) {
        return sourceRepository.findByServicePackageIdOrderByServiceDateDesc(servicePackageId);
    }

    public List<SettlementSource> getSourcesByAbnormalType(String abnormalType) {
        return sourceRepository.findByAbnormalTypeOrderByServiceDateDesc(abnormalType);
    }

    public Page<SettlementSource> getSourcesByAbnormalType(String abnormalType, Pageable pageable) {
        return sourceRepository.findByAbnormalType(abnormalType, pageable);
    }

    public List<SettlementSource> getSourcesByServiceItem(String serviceItemCode) {
        return sourceRepository.findByServiceItemCodeOrderByServiceDateDesc(serviceItemCode);
    }

    public List<SettlementSource> getSourcesByDateRange(LocalDate startDate, LocalDate endDate) {
        return sourceRepository.findByServiceDateBetweenOrderByServiceDateDesc(startDate, endDate);
    }

    public List<SettlementSource> getSourcesBySourceType(String sourceType) {
        return sourceRepository.findBySourceTypeOrderByServiceDateDesc(sourceType);
    }

    public Optional<SettlementSource> findById(Long id) {
        return sourceRepository.findById(id);
    }

    public BigDecimal calculateTotalByElder(Long elderId) {
        List<SettlementSource> sources = sourceRepository.findByElderIdOrderByServiceDateDesc(elderId);
        return sources.stream()
                .map(s -> s.getAmount() != null ? s.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalByNurse(Long nurseId) {
        List<SettlementSource> sources = sourceRepository.findByNurseIdOrderByServiceDateDesc(nurseId);
        return sources.stream()
                .map(s -> s.getAmount() != null ? s.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
