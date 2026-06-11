package com.eldercare.service;

import com.eldercare.entity.Caregiver;
import com.eldercare.entity.Elder;
import com.eldercare.entity.ServiceDemand;
import com.eldercare.entity.WorkOrder;
import com.eldercare.enums.DemandStatus;
import com.eldercare.enums.ElderStatus;
import com.eldercare.repository.CaregiverRepository;
import com.eldercare.repository.ElderRepository;
import com.eldercare.repository.ServiceDemandRepository;
import com.eldercare.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkOrderService {
    private final WorkOrderRepository workOrderRepository;
    private final ServiceDemandRepository demandRepository;
    private final ElderRepository elderRepository;
    private final CaregiverRepository caregiverRepository;
    private final CaregiverService caregiverService;
    private final ElderService elderService;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            ServiceDemandRepository demandRepository,
                            ElderRepository elderRepository,
                            CaregiverRepository caregiverRepository,
                            CaregiverService caregiverService,
                            ElderService elderService) {
        this.workOrderRepository = workOrderRepository;
        this.demandRepository = demandRepository;
        this.elderRepository = elderRepository;
        this.caregiverRepository = caregiverRepository;
        this.caregiverService = caregiverService;
        this.elderService = elderService;
    }

    private final AtomicInteger orderCounter = new AtomicInteger(0);

    public List<WorkOrder> findAll() {
        List<WorkOrder> list = workOrderRepository.findAll();
        list.forEach(this::fillTransientFields);
        return list;
    }

    public Optional<WorkOrder> findById(Long id) {
        Optional<WorkOrder> opt = workOrderRepository.findById(id);
        opt.ifPresent(this::fillTransientFields);
        return opt;
    }

    public List<WorkOrder> findByDemandId(Long demandId) {
        List<WorkOrder> list = workOrderRepository.findByDemandId(demandId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public List<WorkOrder> findByCaregiverId(Long caregiverId) {
        List<WorkOrder> list = workOrderRepository.findByCaregiverId(caregiverId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public List<WorkOrder> findByElderId(Long elderId) {
        List<WorkOrder> list = workOrderRepository.findByElderId(elderId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public List<WorkOrder> findByOrderStatus(String orderStatus) {
        List<WorkOrder> list = workOrderRepository.findByOrderStatus(orderStatus);
        list.forEach(this::fillTransientFields);
        return list;
    }

    private void fillTransientFields(WorkOrder order) {
        if (order.getElderId() != null) {
            elderRepository.findById(order.getElderId())
                    .ifPresent(elder -> order.setElderName(elder.getName()));
        }
        if (order.getCaregiverId() != null) {
            caregiverRepository.findById(order.getCaregiverId())
                    .ifPresent(cg -> order.setCaregiverName(cg.getName()));
        }
    }

    private synchronized String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int seq = orderCounter.incrementAndGet() % 10000;
        return "WO" + datePart + String.format("%04d", seq);
    }

    @Transactional
    public WorkOrder dispatch(Long demandId, Long caregiverId, String dispatcher, String dispatchRemark) {
        ServiceDemand demand = demandRepository.findById(demandId)
                .orElseThrow(() -> new RuntimeException("服务需求不存在"));

        if (!DemandStatus.PENDING_DISPATCH.equals(demand.getStatus())) {
            throw new RuntimeException("当前需求状态为" + demand.getStatus().getDescription() + "，无法派单");
        }

        if (elderService.isSuspended(demand.getElderId())) {
            Elder elder = elderRepository.findById(demand.getElderId()).orElse(null);
            String reason = elder != null ? elder.getSuspendReason() : "";
            throw new RuntimeException("该老人服务已暂停" + (reason != null && !reason.isEmpty() ? "：" + reason : "") + "，无法派单");
        }

        Caregiver caregiver = caregiverRepository.findById(caregiverId)
                .orElseThrow(() -> new RuntimeException("护理员不存在"));

        if (!caregiver.getActive()) {
            throw new RuntimeException("该护理员已停用，无法接单");
        }

        if (!caregiverService.isQualificationMatched(caregiverId, demand.getRequiredQualifications())) {
            throw new RuntimeException("护理员资质不匹配（需求：" + demand.getRequiredQualifications() + "），无法派单");
        }

        WorkOrder order = new WorkOrder();
        order.setOrderNo(generateOrderNo());
        order.setDemandId(demandId);
        order.setElderId(demand.getElderId());
        order.setCaregiverId(caregiverId);
        order.setServiceType(demand.getServiceType());
        order.setServiceContent(demand.getServiceContent());
        order.setScheduledTime(demand.getExpectedTime());
        order.setSpecialRequirement(demand.getSpecialRequirement());
        order.setOrderAmount(demand.getEstimatedAmount());
        order.setDispatcher(dispatcher);
        order.setDispatchRemark(dispatchRemark);
        order.setOrderStatus("待签到");
        order.setDispatchTime(LocalDateTime.now());

        WorkOrder saved = workOrderRepository.save(order);

        demand.setStatus(DemandStatus.DISPATCHED);
        demandRepository.save(demand);

        fillTransientFields(saved);
        return saved;
    }

    @Transactional
    public WorkOrder updateOrderStatus(Long id, String status) {
        return workOrderRepository.findById(id).map(order -> {
            order.setOrderStatus(status);
            WorkOrder saved = workOrderRepository.save(order);

            if ("服务中".equals(status)) {
                demandRepository.findById(order.getDemandId())
                        .ifPresent(d -> {
                            d.setStatus(DemandStatus.IN_SERVICE);
                            demandRepository.save(d);
                        });
            } else if ("已完成".equals(status)) {
                demandRepository.findById(order.getDemandId())
                        .ifPresent(d -> {
                            d.setStatus(DemandStatus.COMPLETED);
                            demandRepository.save(d);
                        });
            }

            fillTransientFields(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("工单不存在"));
    }

    @Transactional
    public void delete(Long id) {
        workOrderRepository.deleteById(id);
    }
}
