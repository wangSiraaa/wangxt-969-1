package com.eldercare.service;

import com.eldercare.common.enums.AbnormalType;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.common.exception.BusinessException;
import com.eldercare.dto.CheckInDTO;
import com.eldercare.dto.CheckOutDTO;
import com.eldercare.entity.*;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final WorkOrderRepository workOrderRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final AbnormalEventService abnormalEventService;
    private final AuditLogService auditLogService;

    private static final double MAX_DISTANCE_KM = 3.0;
    private static final int TOLERANCE_MINUTES_EARLY = 30;
    private static final int TOLERANCE_MINUTES_LATE = 30;

    @Transactional
    public CheckIn checkIn(CheckInDTO dto) {
        WorkOrder order = workOrderRepository.findById(dto.getWorkOrderId())
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + dto.getWorkOrderId()));

        OrderStatus status = order.getStatus();
        if (status != OrderStatus.DISPATCHED && status != OrderStatus.NURSE_ACCEPTED) {
            throw new IllegalStateException(
                    "当前工单状态【" + status + "】不允许签到。"
                            + "允许签到的状态：DISPATCHED / NURSE_ACCEPTED");
        }

        if (!order.getNurseId().equals(dto.getNurseId())) {
            throw new SecurityException("此工单不属于该护理员");
        }

        LocalDateTime now = dto.getCheckInTime() != null ? dto.getCheckInTime() : LocalDateTime.now();
        LocalDateTime scheduledStart = LocalDateTime.of(order.getScheduledDate(), order.getScheduledStartTime());
        Duration diff = Duration.between(scheduledStart, now);
        long diffMinutes = diff.toMinutes();

        if (diffMinutes < -TOLERANCE_MINUTES_EARLY) {
            throw new BusinessException("签到太早，距预约开始时间超过" + TOLERANCE_MINUTES_EARLY + "分钟。请在预约前" + TOLERANCE_MINUTES_EARLY + "分钟内签到。", 400);
        }

        if (diffMinutes > TOLERANCE_MINUTES_LATE) {
            abnormalEventService.createAbnormal(order.getId(), AbnormalType.SERVICE_TIMEOUT,
                    "护理员签到迟到 " + diffMinutes + " 分钟。预约时间：" + scheduledStart
                            + "，实际签到：" + now, "MEDIUM", true);
        }

        boolean locationValid = validateLocation(
                order.getLongitude(), order.getLatitude(),
                dto.getCheckInLongitude(), dto.getCheckInLatitude());
        if (!locationValid) {
            log.warn("签到位置偏差超过 {} 公里，orderId={}", MAX_DISTANCE_KM, order.getId());
        }

        CheckIn checkIn = checkInRepository.findByWorkOrderId(order.getId()).orElseGet(() -> {
            CheckIn ci = new CheckIn();
            ci.setWorkOrderId(order.getId());
            ci.setOrderCode(order.getOrderCode());
            ci.setNurseId(order.getNurseId());
            ci.setNurseName(order.getNurseName());
            ci.setElderId(order.getElderId());
            ci.setElderName(order.getElderName());
            return ci;
        });

        checkIn.setCheckInTime(now);
        checkIn.setCheckInLongitude(dto.getCheckInLongitude());
        checkIn.setCheckInLatitude(dto.getCheckInLatitude());
        checkIn.setCheckInAddress(dto.getCheckInAddress());
        checkIn.setCheckInPhotoUrl(dto.getCheckInPhotoUrl() != null
                ? dto.getCheckInPhotoUrl() : "placeholder_checkin_" + order.getOrderCode() + ".jpg");
        checkIn.setStatus("CHECKED_IN");

        checkIn = checkInRepository.save(checkIn);

        String before = order.toString();
        order.setStatus(OrderStatus.CHECKED_IN);
        order.setStartedAt(now);
        workOrderRepository.save(order);

        auditLogService.logCheckIn(checkIn.getId(), order.getOrderCode(),
                "CHECK_IN", order.getNurseName(), null, checkIn.toString());
        auditLogService.logWorkOrder(order.getId(), order.getOrderCode(),
                "STATUS_CHECKED_IN", order.getNurseName(), before, order.toString());

        log.info("护理员签到成功: orderCode={}, nurse={}, checkInTime={}, late={}min",
                order.getOrderCode(), order.getNurseName(), now,
                diffMinutes > 0 ? diffMinutes : 0);
        return checkIn;
    }

    @Transactional
    public ServiceRecord checkOut(CheckOutDTO dto) {
        WorkOrder order = workOrderRepository.findById(dto.getWorkOrderId())
                .orElseThrow(() -> new IllegalArgumentException("工单不存在: " + dto.getWorkOrderId()));

        if (order.getStatus() != OrderStatus.CHECKED_IN && order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "当前工单状态【" + order.getStatus() + "】不允许签退。"
                            + "请先签到后再签退。");
        }

        CheckIn checkIn = checkInRepository.findByWorkOrderId(order.getId())
                .orElseThrow(() -> new IllegalStateException("签到记录不存在，请先签到"));

        if (checkIn.getCheckInTime() == null) {
            throw new IllegalStateException("签到时间为空，无法签退");
        }

        LocalDateTime now = dto.getCheckOutTime() != null ? dto.getCheckOutTime() : LocalDateTime.now();
        int durationMinutes = (int) Duration.between(checkIn.getCheckInTime(), now).toMinutes();
        if (durationMinutes <= 0) {
            throw new BusinessException("签退时间必须晚于签到时间", 400);
        }

        LocalDateTime scheduledEnd = LocalDateTime.of(order.getScheduledDate(), order.getScheduledEndTime());
        if (now.isAfter(scheduledEnd.plusMinutes(TOLERANCE_MINUTES_LATE))) {
            long overdue = Duration.between(scheduledEnd, now).toMinutes();
            abnormalEventService.createAbnormal(order.getId(), AbnormalType.SERVICE_TIMEOUT,
                    "服务超时 " + overdue + " 分钟。计划结束时间：" + scheduledEnd
                            + "，实际签退：" + now, "MEDIUM", true);
        }

        String before = checkIn.toString();
        checkIn.setCheckOutTime(now);
        checkIn.setCheckOutLongitude(dto.getCheckOutLongitude());
        checkIn.setCheckOutLatitude(dto.getCheckOutLatitude());
        checkIn.setCheckOutAddress(dto.getCheckOutAddress());
        checkIn.setCheckOutPhotoUrl(dto.getCheckOutPhotoUrl() != null
                ? dto.getCheckOutPhotoUrl() : "placeholder_checkout_" + order.getOrderCode() + ".jpg");
        checkIn.setServiceDurationMinutes(durationMinutes);
        checkIn.setStatus("CHECKED_OUT");
        checkInRepository.save(checkIn);
        auditLogService.logCheckIn(checkIn.getId(), order.getOrderCode(),
                "CHECK_OUT", order.getNurseName(), before, checkIn.toString());

        ServiceRecord record = serviceRecordRepository.findByWorkOrderId(order.getId()).orElseGet(() -> {
            ServiceRecord sr = new ServiceRecord();
            sr.setWorkOrderId(order.getId());
            sr.setOrderCode(order.getOrderCode());
            sr.setNurseId(order.getNurseId());
            sr.setElderId(order.getElderId());
            return sr;
        });
        record.setServiceContent(dto.getServiceContent());
        record.setHealthObservation(dto.getHealthObservation());
        record.setMedicationAdministered(dto.getMedicationAdministered());
        record.setVitalSigns(dto.getVitalSigns());
        record.setServicePhotos(dto.getServicePhotos() != null
                ? dto.getServicePhotos() : "placeholder_service_photos_" + order.getOrderCode() + ".jpg");
        record.setAbnormalSituation(dto.getAbnormalSituation());
        record.setRiskEventOccurred(dto.getRiskEventOccurred() != null ? dto.getRiskEventOccurred() : false);
        record.setRiskEventDetail(dto.getRiskEventDetail());
        record.setRecordedAt(now);
        record = serviceRecordRepository.save(record);

        if (Boolean.TRUE.equals(dto.getRiskEventOccurred())) {
            abnormalEventService.createAbnormal(order.getId(), AbnormalType.OTHER,
                    "服务期间发生风险事件: " + (dto.getRiskEventDetail() != null
                            ? dto.getRiskEventDetail() : "未详细描述"),
                    "CRITICAL", false);
        }

        String orderBefore = order.toString();
        order.setStatus(OrderStatus.PENDING_FAMILY_CONFIRM);
        order.setCompletedAt(now);
        workOrderRepository.save(order);
        auditLogService.logWorkOrder(order.getId(), order.getOrderCode(),
                "SERVICE_COMPLETED", order.getNurseName(), orderBefore, order.toString());

        log.info("服务完成签退: orderCode={}, nurse={}, duration={}min, riskEvent={}",
                order.getOrderCode(), order.getNurseName(), durationMinutes, record.getRiskEventOccurred());
        return record;
    }

    private boolean validateLocation(BigDecimal orderLng, BigDecimal orderLat,
                                     BigDecimal nurseLng, BigDecimal nurseLat) {
        if (orderLng == null || orderLat == null || nurseLng == null || nurseLat == null) {
            return true;
        }

        double earthRadius = 6371.0;
        double lat1Rad = Math.toRadians(orderLat.doubleValue());
        double lat2Rad = Math.toRadians(nurseLat.doubleValue());
        double deltaLat = Math.toRadians(nurseLat.doubleValue() - orderLat.doubleValue());
        double deltaLng = Math.toRadians(nurseLng.doubleValue() - orderLng.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        return distance <= MAX_DISTANCE_KM;
    }

    public Optional<CheckIn> findByWorkOrder(Long workOrderId) {
        return checkInRepository.findByWorkOrderId(workOrderId);
    }

    public Optional<CheckIn> findById(Long id) {
        return checkInRepository.findById(id);
    }

    public Optional<ServiceRecord> findServiceRecordByWorkOrder(Long workOrderId) {
        return serviceRecordRepository.findByWorkOrderId(workOrderId);
    }

    public void detectNoCheckOutAbnormals() {
        checkInRepository.findPendingCheckOut().forEach(checkIn -> {
            if (checkIn.getCheckInTime() != null
                    && Duration.between(checkIn.getCheckInTime(), LocalDateTime.now()).toHours() > 24) {
                abnormalEventService.createAbnormal(checkIn.getWorkOrderId(), AbnormalType.NO_CHECKOUT,
                        "护理员签到后超过24小时未签退。签到时间: " + checkIn.getCheckInTime(),
                        "HIGH", true);
            }
        });
    }
}
