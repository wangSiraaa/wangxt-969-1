package com.eldercare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatisticsVO {

    private Long totalDemands;
    private Long pendingDispatchDemands;
    private Long totalWorkOrders;
    private Long inProgressOrders;
    private Long pendingSettlementOrders;
    private Long settledOrders;
    private Long pendingAbnormalEvents;
    private Long totalElders;
    private Long activeElders;
    private Long pausedElders;
    private Long totalNurses;
    private Long activeNurses;
    private BigDecimal todaySettlementAmount;
    private BigDecimal monthSettlementAmount;
    private Long todayCompletedOrders;
    private Long todayNewDemands;
    private LocalDate statisticsDate;
}
