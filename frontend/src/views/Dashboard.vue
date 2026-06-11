<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">工作台 - 养老上门服务工单系统</div>
    </div>

    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <div class="dashboard-card">
          <div class="card-title">待派单需求</div>
          <div class="card-value warning">{{ stats.pendingDemand }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="dashboard-card">
          <div class="card-title">待签到工单</div>
          <div class="card-value primary">{{ stats.pendingCheckIn }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="dashboard-card">
          <div class="card-title">服务中工单</div>
          <div class="card-value success">{{ stats.inService }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="dashboard-card">
          <div class="card-title">待结算</div>
          <div class="card-value danger">{{ stats.pendingSettlement }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="12">
        <div class="table-container">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
            最新服务需求
          </div>
          <el-table :data="recentDemands" stripe>
            <el-table-column prop="id" label="编号" width="70" />
            <el-table-column prop="elderName" label="老人" width="100" />
            <el-table-column prop="serviceType" label="服务类型" width="120" />
            <el-table-column prop="serviceContent" label="服务内容" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="demandStatusType(row.status)">{{ getDemandStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="期望时间" width="170">
              <template #default="{ row }">{{ formatDate(row.expectedTime) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="table-container">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
            最新工单
          </div>
          <el-table :data="recentOrders" stripe>
            <el-table-column prop="orderNo" label="工单号" width="180" />
            <el-table-column prop="elderName" label="老人" width="90" />
            <el-table-column prop="caregiverName" label="护理员" width="90" />
            <el-table-column prop="serviceType" label="服务类型" width="110" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="orderStatusType(row.orderStatus)">{{ row.orderStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="预约时间" width="160">
              <template #default="{ row }">{{ formatDate(row.scheduledTime) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as api from '@/api'
import dayjs from 'dayjs'

const stats = ref({
  pendingDemand: 0,
  pendingCheckIn: 0,
  inService: 0,
  pendingSettlement: 0
})
const recentDemands = ref([])
const recentOrders = ref([])

const formatDate = (d) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
const getDemandStatusText = (s) => ({
  PENDING_DISPATCH: '待派单', DISPATCHED: '已派单', IN_SERVICE: '服务中',
  COMPLETED: '已完成', CANCELLED: '已取消'
})[s] || s
const demandStatusType = (s) => ({
  PENDING_DISPATCH: 'warning', DISPATCHED: 'primary', IN_SERVICE: 'success',
  COMPLETED: 'info', CANCELLED: 'danger'
})[s] || 'info'
const orderStatusType = (s) => ({
  '待签到': 'primary', '服务中': 'success', '已完成': 'info', '已取消': 'danger'
})[s] || 'info'

const loadStats = async () => {
  try {
    const demands = await api.getDemands()
    const orders = await api.getWorkOrders()
    const settlements = await api.getSettlements()

    stats.value.pendingDemand = demands.filter(d => d.status === 'PENDING_DISPATCH').length
    stats.value.pendingCheckIn = orders.filter(o => o.orderStatus === '待签到').length
    stats.value.inService = orders.filter(o => o.orderStatus === '服务中').length
    stats.value.pendingSettlement = settlements.filter(s => s.status === 'PENDING').length

    recentDemands.value = demands.slice(0, 6)
    recentOrders.value = orders.slice(0, 6)
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadStats)
</script>

<style scoped>
.stat-cards {
  margin-bottom: 4px;
}
</style>
