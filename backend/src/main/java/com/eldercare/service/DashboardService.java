package com.eldercare.service;

import com.eldercare.dto.DashboardStatisticsVO;
import com.eldercare.common.enums.AbnormalStatus;
import com.eldercare.common.enums.ElderStatus;
import com.eldercare.common.enums.NurseStatus;
import com.eldercare.common.enums.OrderStatus;
import com.eldercare.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ServiceDemandRepository demandRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AbnormalEventRepository abnormalEventRepository;
    private final ElderRepository elderRepository;
    private final NurseRepository nurseRepository;
    private final SettlementService settlementService;

    public DashboardStatisticsVO getStatistics() {
        DashboardStatisticsVO vo = DashboardStatisticsVO.builder()
                .totalDemands(demandRepository.count())
                .pendingDispatchDemands(demandRepository.countByStatus("PENDING_DISPATCH"))
                .totalWorkOrders(workOrderRepository.count())
                .inProgressOrders(workOrderRepository.countByStatus(OrderStatus.CHECKED_IN)
                        + workOrderRepository.countByStatus(OrderStatus.IN_PROGRESS)
                        + workOrderRepository.countByStatus(OrderStatus.NURSE_ACCEPTED))
                .pendingSettlementOrders(workOrderRepository.countByStatus(OrderStatus.PENDING_SETTLEMENT)
                        + workOrderRepository.countByStatus(OrderStatus.FAMILY_CONFIRMED)
                        + workOrderRepository.countByStatus(OrderStatus.PENDING_FAMILY_CONFIRM))
                .settledOrders(workOrderRepository.countByStatus(OrderStatus.SETTLED))
                .pendingAbnormalEvents(abnormalEventRepository.countByStatus(AbnormalStatus.PENDING))
                .totalElders(elderRepository.count())
                .activeElders(elderRepository.countByStatus(ElderStatus.ACTIVE))
                .pausedElders(elderRepository.countByStatus(ElderStatus.PAUSED))
                .totalNurses(nurseRepository.count())
                .activeNurses(nurseRepository.countByStatus(NurseStatus.ACTIVE))
                .todaySettlementAmount(settlementService.calculateTodaySettlementAmount())
                .monthSettlementAmount(settlementService.calculateMonthSettlementAmount())
                .todayCompletedOrders(settlementService.countTodayCompletedOrders())
                .todayNewDemands(demandRepository.countTodaySubmitted(
                        LocalDate.now().atStartOfDay(),
                        LocalDate.now().plusDays(1).atStartOfDay()))
                .statisticsDate(LocalDate.now())
                .build();
        return vo;
    }
}
