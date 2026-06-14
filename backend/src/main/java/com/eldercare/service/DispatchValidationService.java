package com.eldercare.service;

import com.eldercare.common.enums.*;
import com.eldercare.dto.DispatchValidationResult;
import com.eldercare.dto.NurseCandidateVO;
import com.eldercare.entity.*;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchValidationService {

    private final ElderRepository elderRepository;
    private final NurseRepository nurseRepository;
    private final NurseQualificationRepository nurseQualificationRepository;
    private final NursingLevelRepository nursingLevelRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final WorkOrderRepository workOrderRepository;
    private final CheckInRepository checkInRepository;
    private final NurseScheduleCapacityRepository scheduleCapacityRepository;

    public DispatchValidationResult validateDispatch(Long elderId, Long nurseId,
                                                     Long servicePackageId,
                                                     LocalDate scheduledDate,
                                                     LocalTime startTime,
                                                     LocalTime endTime) {
        DispatchValidationResult result = DispatchValidationResult.builder()
                .valid(true)
                .errorMessages(new ArrayList<>())
                .abnormalTypes(new ArrayList<>())
                .build();

        Elder elder = elderRepository.findById(elderId).orElse(null);
        Nurse nurse = nurseRepository.findById(nurseId).orElse(null);
        ServicePackage servicePackage = servicePackageId != null
                ? servicePackageRepository.findById(servicePackageId).orElse(null) : null;

        if (elder == null) {
            result.addError("老人档案不存在", AbnormalType.OTHER);
            return result;
        }
        if (nurse == null) {
            result.addError("护理员档案不存在", AbnormalType.OTHER);
            return result;
        }

        result.setElderName(elder.getName());
        result.setNurseName(nurse.getName());

        validateElderPauseStatus(elder, result, scheduledDate);

        if (nurse.getStatus() != NurseStatus.ACTIVE) {
            result.addError("护理员状态异常：" + nurse.getStatus(), AbnormalType.OTHER);
        }

        validateQualification(elder, nurse, servicePackage, result);

        validateServiceArea(elder, nurse, result);

        validateRiskLevel(elder, nurse, result);

        validateTimeConflict(nurse, scheduledDate, startTime, endTime, result);

        validateScheduleCapacity(nurse, scheduledDate, startTime, endTime, result);

        validateContinuousHours(nurse, scheduledDate, startTime, endTime, result);

        return result;
    }

    private void validateElderPauseStatus(Elder elder, DispatchValidationResult result, LocalDate date) {
        if (elder.getStatus() == ElderStatus.PAUSED) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = elder.getPauseStartTime();
            LocalDateTime end = elder.getPauseEndTime();

            LocalDateTime scheduleDateTime = LocalDateTime.of(date, LocalTime.MIN);

            boolean isPausedNow = (start == null || !now.isBefore(start))
                    && (end == null || now.isBefore(end));
            boolean isPausedOnSchedule = (start == null || !scheduleDateTime.isBefore(start))
                    && (end == null || scheduleDateTime.isBefore(end));

            if (isPausedNow || isPausedOnSchedule) {
                result.addError("老人【" + elder.getName() + "】当前处于暂停服务状态，原因："
                        + (elder.getPauseReason() != null ? elder.getPauseReason() : "未填写")
                        + (end != null ? "，暂停至：" + end.toLocalDate() : ""), AbnormalType.ELDER_PAUSED);
            }
        }
    }

    private void validateQualification(Elder elder, Nurse nurse, ServicePackage servicePackage,
                                       DispatchValidationResult result) {
        Set<String> nurseQualifications = getNurseValidQualifications(nurse.getId());

        NursingLevel requiredLevel = null;
        if (elder.getNursingLevelId() != null) {
            requiredLevel = nursingLevelRepository.findById(elder.getNursingLevelId()).orElse(null);
        }

        List<String> requiredQualifications = new ArrayList<>();
        if (requiredLevel != null && requiredLevel.getRequiredQualifications() != null) {
            requiredQualifications.addAll(Arrays.asList(
                    requiredLevel.getRequiredQualifications().split(",")));
        }
        if (servicePackage != null && servicePackage.getRequiredQualifications() != null) {
            for (String q : servicePackage.getRequiredQualifications().split(",")) {
                if (!requiredQualifications.contains(q)) {
                    requiredQualifications.add(q);
                }
            }
        }

        for (String required : requiredQualifications) {
            required = required.trim();
            if (required.isEmpty()) continue;
            if (!nurseQualifications.contains(required)) {
                result.addError("护理员【" + nurse.getName()
                                + "】缺少资质：" + translateQualification(required)
                                + "。老人护理等级/服务包要求：" + requiredQualifications.stream()
                                .map(this::translateQualification).collect(Collectors.joining("、")),
                        AbnormalType.QUALIFICATION_MISMATCH);
                break;
            }
        }

        if (requiredLevel != null && nurse.getHighestNursingLevelId() != null) {
            int nurseLevelOrder = getLevelOrder(nurse.getHighestNursingLevelId());
            int requiredLevelOrder = getLevelOrder(requiredLevel.getId());
            if (nurseLevelOrder < requiredLevelOrder) {
                result.addError("护理员【" + nurse.getName() + "】最高护理等级不足，"
                                + "要求：" + requiredLevel.getName() + "，护理员实际：最高等级ID=" + nurse.getHighestNursingLevelId(),
                        AbnormalType.QUALIFICATION_MISMATCH);
            }
        }
    }

    private Set<String> getNurseValidQualifications(Long nurseId) {
        List<NurseQualification> qualifications = nurseQualificationRepository
                .findByNurseIdAndStatus(nurseId, QualificationStatus.VALID);
        LocalDate today = LocalDate.now();
        return qualifications.stream()
                .filter(q -> q.getExpiryDate() == null || !q.getExpiryDate().isBefore(today))
                .map(NurseQualification::getType)
                .collect(Collectors.toSet());
    }

    private String translateQualification(String code) {
        Map<String, String> map = new HashMap<>();
        map.put("CAREGIVER_BASIC", "初级养老护理员证");
        map.put("CAREGIVER_INTERMEDIATE", "中级养老护理员证");
        map.put("CAREGIVER_SENIOR", "高级养老护理员证");
        map.put("NURSE_LICENSE", "护士执业证书");
        map.put("BATH_ASSIST", "助浴服务资格证");
        map.put("FIRST_AID", "红十字急救证");
        map.put("REHAB_TRAINING", "康复训练师资格证");
        map.put("MEDICAL_TRAINING", "医学护理专项培训证书");
        map.put("MEAL_TRAINING", "助餐服务专项培训");
        map.put("DEMENTIA_CARE", "认知症照护培训证书");
        map.put("HOSPICE_CARE", "安宁疗护专科证书");
        map.put("MASSAGE_THERAPIST", "按摩师资格证");
        return map.getOrDefault(code, code);
    }

    private int getLevelOrder(Long levelId) {
        if (levelId == null) return 0;
        return levelId.intValue();
    }

    private void validateServiceArea(Elder elder, Nurse nurse, DispatchValidationResult result) {
        String elderArea = elder.getServiceArea();
        String nurseAreas = nurse.getServiceAreas();

        if (elderArea != null && !elderArea.isEmpty()
                && nurseAreas != null && !nurseAreas.isEmpty()) {
            List<String> nurseAreaList = Arrays.asList(nurseAreas.split(","));
            if (!nurseAreaList.contains(elderArea)) {
                result.addError("护理员【" + nurse.getName()
                                + "】服务区域不包含老人所在区域【" + translateArea(elderArea) + "】，"
                                + "护理员服务区域：" + Arrays.stream(nurseAreas.split(","))
                                .map(this::translateArea).collect(Collectors.joining("、")),
                        AbnormalType.AREA_MISMATCH);
            }
        }
    }

    private String translateArea(String areaCode) {
        Map<String, String> map = new HashMap<>();
        map.put("CHAOWAYANG", "朝阳区");
        map.put("HAIDIAN", "海淀区");
        map.put("XICHENG", "西城区");
        map.put("DONGCHENG", "东城区");
        map.put("FENGTAI", "丰台区");
        map.put("SHIJINGSHAN", "石景山区");
        return map.getOrDefault(areaCode, areaCode);
    }

    private void validateRiskLevel(Elder elder, Nurse nurse, DispatchValidationResult result) {
        RiskLevel elderRisk = elder.getRiskLevel();
        if (elderRisk == null) return;

        int riskOrder = elderRisk.ordinal();
        Long highestLevel = nurse.getHighestNursingLevelId();

        boolean canHandle = true;
        if (riskOrder >= RiskLevel.CRITICAL.ordinal()
                && (highestLevel == null || highestLevel < 4)) {
            canHandle = false;
        } else if (riskOrder >= RiskLevel.HIGH.ordinal()
                && (highestLevel == null || highestLevel < 3)) {
            canHandle = false;
        }

        if (!canHandle) {
            result.addError("老人【" + elder.getName() + "】风险等级为【" + elderRisk
                            + "】，护理员【" + nurse.getName() + "】资质不足以处理该等级风险（要求护理等级>=NL"
                            + (riskOrder + 1) + "）",
                    AbnormalType.RISK_LEVEL_MISMATCH);
        }
    }

    private void validateTimeConflict(Nurse nurse, LocalDate date,
                                      LocalTime startTime, LocalTime endTime,
                                      DispatchValidationResult result) {
        List<WorkOrder> existingOrders = workOrderRepository
                .findByNurseIdAndScheduledDate(nurse.getId(), date);

        LocalDateTime newStart = LocalDateTime.of(date, startTime);
        LocalDateTime newEnd = LocalDateTime.of(date, endTime);

        for (WorkOrder existing : existingOrders) {
            if (existing.getScheduledStartTime() == null || existing.getScheduledEndTime() == null) continue;

            LocalDateTime existStart = LocalDateTime.of(date, existing.getScheduledStartTime());
            LocalDateTime existEnd = LocalDateTime.of(date, existing.getScheduledEndTime());

            boolean overlap = !(newEnd.isBefore(existStart) || newStart.isAfter(existEnd));
            if (overlap) {
                result.addError("护理员【" + nurse.getName() + "】在 " + date + " "
                                + existing.getScheduledStartTime() + "-" + existing.getScheduledEndTime()
                                + " 已有工单【" + existing.getOrderCode() + "】，时间冲突",
                        AbnormalType.TIME_CONFLICT);
                break;
            }
        }
    }

    private void validateScheduleCapacity(Nurse nurse, LocalDate date,
                                          LocalTime startTime, LocalTime endTime,
                                          DispatchValidationResult result) {
        com.eldercare.common.enums.DayOfWeek dayOfWeek = com.eldercare.common.enums.DayOfWeek.valueOf(date.getDayOfWeek().name());
        List<NurseScheduleCapacity> capacities = scheduleCapacityRepository
                .findByNurseIdAndDayOfWeek(nurse.getId(), dayOfWeek);

        boolean hasCapacity = false;
        for (NurseScheduleCapacity cap : capacities) {
            if (!cap.getEnabled()) continue;
            LocalTime capStart = cap.getStartTime() != null ? cap.getStartTime() : LocalTime.of(8, 0);
            LocalTime capEnd = cap.getEndTime() != null ? cap.getEndTime() : LocalTime.of(18, 0);

            if (!startTime.isBefore(capStart) && !endTime.isAfter(capEnd)) {
                hasCapacity = true;
                Integer dailyCount = workOrderRepository.countNurseDailyOrders(nurse.getId(), date);
                int maxOrders = cap.getMaxOrders() != null ? cap.getMaxOrders() : nurse.getMaxDailyOrders();
                if (dailyCount != null && dailyCount >= maxOrders) {
                    result.addError("护理员【" + nurse.getName() + "】在 " + date
                                    + " 已安排 " + dailyCount + " 单，达到最大日订单数 " + maxOrders,
                            AbnormalType.TIME_CONFLICT);
                }
                break;
            }
        }

        if (!hasCapacity && !capacities.isEmpty()) {
            result.addError("护理员【" + nurse.getName() + "】在 " + date
                            + " 该时间段未排班，排班时段：" + capacities.stream()
                            .map(c -> c.getStartTime() + "-" + c.getEndTime()).collect(Collectors.joining("、")),
                    AbnormalType.TIME_CONFLICT);
        }
    }

    private void validateContinuousHours(Nurse nurse, LocalDate date,
                                         LocalTime startTime, LocalTime endTime,
                                         DispatchValidationResult result) {
        List<WorkOrder> dailyOrders = workOrderRepository
                .findByNurseIdAndScheduledDate(nurse.getId(), date);

        int totalMinutesToday = 0;
        List<LocalTime> allStarts = new ArrayList<>();
        List<LocalTime> allEnds = new ArrayList<>();

        for (WorkOrder order : dailyOrders) {
            if (order.getScheduledStartTime() == null || order.getScheduledEndTime() == null) continue;
            int duration = (int) ChronoUnit.MINUTES.between(order.getScheduledStartTime(), order.getScheduledEndTime());
            totalMinutesToday += duration;
            allStarts.add(order.getScheduledStartTime());
            allEnds.add(order.getScheduledEndTime());
        }

        int newDuration = (int) ChronoUnit.MINUTES.between(startTime, endTime);
        totalMinutesToday += newDuration;
        allStarts.add(startTime);
        allEnds.add(endTime);

        int maxDailyMinutes = (nurse.getMaxWeeklyHours() != null ? nurse.getMaxWeeklyHours() : 40) * 60 / 7;
        maxDailyMinutes = Math.max(maxDailyMinutes, 8 * 60);
        if (totalMinutesToday > maxDailyMinutes) {
            result.addError("护理员【" + nurse.getName() + "】当日累计工时 "
                            + totalMinutesToday / 60 + "小时" + totalMinutesToday % 60 + "分钟，"
                            + "超过最大日工时限制 " + maxDailyMinutes / 60 + " 小时",
                    AbnormalType.CONTINUOUS_HOURS_EXCEEDED);
        }

        int maxContinuous = (nurse.getMaxContinuousHours() != null ? nurse.getMaxContinuousHours() : 6) * 60;
        int continuousBlock = calculateMaxContinuousBlock(allStarts, allEnds);
        if (continuousBlock > maxContinuous) {
            result.addError("护理员【" + nurse.getName() + "】连续工作时长 "
                            + continuousBlock / 60 + "小时" + continuousBlock % 60 + "分钟，"
                            + "超过最大连续工时限制 " + maxContinuous / 60 + " 小时",
                    AbnormalType.CONTINUOUS_HOURS_EXCEEDED);
        }
    }

    private int calculateMaxContinuousBlock(List<LocalTime> starts, List<LocalTime> ends) {
        if (starts.isEmpty()) return 0;

        List<int[]> intervals = new ArrayList<>();
        for (int i = 0; i < starts.size(); i++) {
            intervals.add(new int[]{
                    (int) ChronoUnit.MINUTES.between(LocalTime.MIN, starts.get(i)),
                    (int) ChronoUnit.MINUTES.between(LocalTime.MIN, ends.get(i))
            });
        }
        intervals.sort(Comparator.comparingInt(a -> a[0]));

        int maxBlock = 0;
        int currentStart = intervals.get(0)[0];
        int currentEnd = intervals.get(0)[1];

        for (int i = 1; i < intervals.size(); i++) {
            int[] next = intervals.get(i);
            if (next[0] - currentEnd <= 30) {
                currentEnd = Math.max(currentEnd, next[1]);
            } else {
                maxBlock = Math.max(maxBlock, currentEnd - currentStart);
                currentStart = next[0];
                currentEnd = next[1];
            }
        }
        maxBlock = Math.max(maxBlock, currentEnd - currentStart);
        return maxBlock;
    }

    public List<NurseCandidateVO> findCandidateNurses(Long elderId, Long servicePackageId,
                                                       LocalDate date, LocalTime startTime,
                                                       LocalTime endTime) {
        List<Nurse> allActiveNurses = nurseRepository.findByStatus(NurseStatus.ACTIVE);
        List<NurseCandidateVO> candidates = new ArrayList<>();

        Elder elder = elderRepository.findById(elderId).orElse(null);
        if (elder == null) return candidates;

        for (Nurse nurse : allActiveNurses) {
            NurseCandidateVO vo = new NurseCandidateVO();
            vo.setNurseId(nurse.getId());
            vo.setNurseCode(nurse.getNurseCode());
            vo.setNurseName(nurse.getName());
            vo.setGender(nurse.getGender() != null ? nurse.getGender().name() : null);
            vo.setPhone(nurse.getPhone());
            vo.setAverageRating(nurse.getAverageRating());
            vo.setCompletedOrdersCount(nurse.getCompletedOrdersCount());
            vo.setQualificationSummary(nurse.getQualificationSummary());
            vo.setServiceAreas(nurse.getServiceAreas());

            BigDecimal[] distanceTime = calculateDistanceAndTravelTime(
                    elder.getLongitude(), elder.getLatitude(),
                    nurse.getLongitude(), nurse.getLatitude());
            vo.setDistanceKm(distanceTime[0]);
            vo.setTravelTimeMinutes(distanceTime[1].intValue());

            DispatchValidationResult validation = validateDispatch(
                    elderId, nurse.getId(), servicePackageId, date, startTime, endTime);
            vo.setQualified(validation.isValid());
            if (!validation.isValid()) {
                vo.setDisqualifyReason(String.join("；", validation.getErrorMessages()));
            }

            com.eldercare.common.enums.DayOfWeek dayOfWeek = com.eldercare.common.enums.DayOfWeek.valueOf(date.getDayOfWeek().name());
            List<NurseScheduleCapacity> caps = scheduleCapacityRepository
                    .findByNurseIdAndDayOfWeek(nurse.getId(), dayOfWeek);
            int maxOrders = caps.stream().filter(c -> c.getEnabled())
                    .mapToInt(c -> c.getMaxOrders() != null ? c.getMaxOrders() : nurse.getMaxDailyOrders())
                    .max().orElse(nurse.getMaxDailyOrders() != null ? nurse.getMaxDailyOrders() : 6);
            Integer used = workOrderRepository.countNurseDailyOrders(nurse.getId(), date);
            vo.setDailyRemainingSlots(Math.max(0, maxOrders - (used != null ? used : 0)));

            candidates.add(vo);
        }

        candidates.sort((a, b) -> {
            if (a.isQualified() != b.isQualified()) return a.isQualified() ? -1 : 1;
            if (a.getTravelTimeMinutes() != null && b.getTravelTimeMinutes() != null) {
                return a.getTravelTimeMinutes() - b.getTravelTimeMinutes();
            }
            return 0;
        });

        return candidates;
    }

    private BigDecimal[] calculateDistanceAndTravelTime(BigDecimal lng1, BigDecimal lat1,
                                                         BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) {
            return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
        }

        double earthRadius = 6371.0;
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        BigDecimal distanceKm = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
        int avgSpeedKmh = 25;
        int travelMinutes = (int) Math.ceil(distance / avgSpeedKmh * 60);

        return new BigDecimal[]{distanceKm, BigDecimal.valueOf(travelMinutes)};
    }
}
