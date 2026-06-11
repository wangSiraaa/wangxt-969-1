package com.eldercare.service;

import com.eldercare.entity.CheckInRecord;
import com.eldercare.entity.WorkOrder;
import com.eldercare.repository.CaregiverRepository;
import com.eldercare.repository.CheckInRecordRepository;
import com.eldercare.repository.ElderRepository;
import com.eldercare.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CheckInService {
    private final CheckInRecordRepository checkInRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ElderRepository elderRepository;
    private final CaregiverRepository caregiverRepository;

    public CheckInService(CheckInRecordRepository checkInRepository,
                          WorkOrderRepository workOrderRepository,
                          ElderRepository elderRepository,
                          CaregiverRepository caregiverRepository) {
        this.checkInRepository = checkInRepository;
        this.workOrderRepository = workOrderRepository;
        this.elderRepository = elderRepository;
        this.caregiverRepository = caregiverRepository;
    }

    public List<CheckInRecord> findAll() {
        List<CheckInRecord> list = checkInRepository.findAll();
        list.forEach(this::fillTransientFields);
        return list;
    }

    public Optional<CheckInRecord> findById(Long id) {
        Optional<CheckInRecord> opt = checkInRepository.findById(id);
        opt.ifPresent(this::fillTransientFields);
        return opt;
    }

    public List<CheckInRecord> findByWorkOrderId(Long workOrderId) {
        List<CheckInRecord> list = checkInRepository.findByWorkOrderId(workOrderId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public List<CheckInRecord> findByCaregiverId(Long caregiverId) {
        List<CheckInRecord> list = checkInRepository.findByCaregiverId(caregiverId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public List<CheckInRecord> findByElderId(Long elderId) {
        List<CheckInRecord> list = checkInRepository.findByElderId(elderId);
        list.forEach(this::fillTransientFields);
        return list;
    }

    public Optional<CheckInRecord> findLatestByWorkOrderId(Long workOrderId) {
        Optional<CheckInRecord> opt = checkInRepository.findTopByWorkOrderIdOrderByCheckInTimeDesc(workOrderId);
        opt.ifPresent(this::fillTransientFields);
        return opt;
    }

    public boolean hasCheckedIn(Long workOrderId) {
        return checkInRepository.existsByWorkOrderId(workOrderId);
    }

    private void fillTransientFields(CheckInRecord record) {
        if (record.getElderId() != null) {
            elderRepository.findById(record.getElderId())
                    .ifPresent(elder -> record.setElderName(elder.getName()));
        }
        if (record.getCaregiverId() != null) {
            caregiverRepository.findById(record.getCaregiverId())
                    .ifPresent(cg -> record.setCaregiverName(cg.getName()));
        }
    }

    @Transactional
    public CheckInRecord checkIn(Long workOrderId, String location, String signImageUrl) {
        WorkOrder order = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        if (!"待签到".equals(order.getOrderStatus()) && !"服务中".equals(order.getOrderStatus())) {
            throw new RuntimeException("当前工单状态为" + order.getOrderStatus() + "，无法签到");
        }

        CheckInRecord record = new CheckInRecord();
        record.setWorkOrderId(workOrderId);
        record.setCaregiverId(order.getCaregiverId());
        record.setElderId(order.getElderId());
        record.setCheckInTime(LocalDateTime.now());
        record.setCheckInLocation(location);
        record.setSignImageUrl(signImageUrl);

        CheckInRecord saved = checkInRepository.save(record);

        order.setOrderStatus("服务中");
        workOrderRepository.save(order);

        fillTransientFields(saved);
        return saved;
    }

    @Transactional
    public CheckInRecord checkOut(Long recordId, String serviceRecord, String elderCondition, String caregiverRemark) {
        return checkInRepository.findById(recordId).map(record -> {
            if (record.getCheckOutTime() != null) {
                throw new RuntimeException("该签到记录已签退");
            }

            record.setCheckOutTime(LocalDateTime.now());
            record.setServiceRecord(serviceRecord);
            record.setElderCondition(elderCondition);
            record.setCaregiverRemark(caregiverRemark);

            if (record.getCheckInTime() != null && record.getCheckOutTime() != null) {
                Duration duration = Duration.between(record.getCheckInTime(), record.getCheckOutTime());
                double hours = duration.toMinutes() / 60.0;
                record.setServiceHours(BigDecimal.valueOf(Math.round(hours * 100.0) / 100.0));
            }

            CheckInRecord saved = checkInRepository.save(record);

            workOrderRepository.findById(record.getWorkOrderId()).ifPresent(order -> {
                order.setOrderStatus("已完成");
                workOrderRepository.save(order);
            });

            fillTransientFields(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("签到记录不存在"));
    }

    @Transactional
    public CheckInRecord updateRecord(Long id, CheckInRecord record) {
        return checkInRepository.findById(id).map(existing -> {
            existing.setServiceRecord(record.getServiceRecord());
            existing.setElderCondition(record.getElderCondition());
            existing.setCaregiverRemark(record.getCaregiverRemark());
            existing.setCheckInLocation(record.getCheckInLocation());
            CheckInRecord saved = checkInRepository.save(existing);
            fillTransientFields(saved);
            return saved;
        }).orElseThrow(() -> new RuntimeException("签到记录不存在"));
    }

    @Transactional
    public void delete(Long id) {
        checkInRepository.deleteById(id);
    }
}
