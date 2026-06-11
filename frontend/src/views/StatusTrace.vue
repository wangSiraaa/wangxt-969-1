<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">状态追踪 - 需求编号 #{{ demandId }}</div>
      <el-button @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </el-button>
    </div>

    <div v-loading="loading" class="table-container">
      <template v-if="demand">
        <el-descriptions :column="2" border title="需求信息" style="margin-bottom: 20px;">
          <el-descriptions-item label="需求编号">#{{ demand.id }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="demandStatusType(demand.status)" size="large">
              {{ getDemandStatusText(demand.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ demand.elderName }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ demand.applicant }}（{{ demand.relation }}）</el-descriptions-item>
          <el-descriptions-item label="服务类型">{{ demand.serviceType }}</el-descriptions-item>
          <el-descriptions-item label="期望时间">{{ formatDate(demand.expectedTime) }}</el-descriptions-item>
          <el-descriptions-item label="服务内容" :span="2">{{ demand.serviceContent }}</el-descriptions-item>
          <el-descriptions-item label="所需资质" :span="2">{{ demand.requiredQualifications || '无特殊要求' }}</el-descriptions-item>
          <el-descriptions-item label="特殊要求" :span="2">{{ demand.specialRequirement || '无' }}</el-descriptions-item>
        </el-descriptions>

        <el-steps :active="currentStep" finish-status="success" align-center style="margin: 40px 0;">
          <el-step title="需求提交" :description="formatDate(demand.createTime)">
            <template #icon><el-icon size="18"><Edit /></el-icon></template>
          </el-step>
          <el-step title="调度派单" :description="workOrder ? formatDate(workOrder.dispatchTime) : '等待派单'">
            <template #icon><el-icon size="18"><Connection /></el-icon></template>
          </el-step>
          <el-step title="护理员签到" :description="checkInRecord ? formatDate(checkInRecord.checkInTime) : (workOrder ? '等待签到' : '-')">
            <template #icon><el-icon size="18"><Promotion /></el-icon></template>
          </el-step>
          <el-step title="服务完成签退" :description="checkInRecord?.checkOutTime ? formatDate(checkInRecord.checkOutTime) : (checkInRecord ? '服务中' : '-')">
            <template #icon><el-icon size="18"><CircleCheck /></el-icon></template>
          </el-step>
          <el-step title="结算完成" :description="settlement ? formatDate(settlement.settleTime) : '等待结算'">
            <template #icon><el-icon size="18"><Money /></el-icon></template>
          </el-step>
        </el-steps>

        <el-row :gutter="16">
          <el-col :span="12">
            <div v-if="workOrder" class="table-container" style="box-shadow: none; border: 1px solid #ebeef5;">
              <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #303133;">
                <el-icon style="color: #409eff;"><Tickets /></el-icon>
                工单信息
              </div>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="工单号">{{ workOrder.orderNo }}</el-descriptions-item>
                <el-descriptions-item label="护理员">{{ workOrder.caregiverName }}</el-descriptions-item>
                <el-descriptions-item label="调度员">{{ workOrder.dispatcher || '-' }}</el-descriptions-item>
                <el-descriptions-item label="派单时间">{{ formatDate(workOrder.dispatchTime) }}</el-descriptions-item>
                <el-descriptions-item label="工单状态">
                  <el-tag :type="orderStatusType(workOrder.orderStatus)">{{ workOrder.orderStatus }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="派单备注">{{ workOrder.dispatchRemark || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="暂无工单，请先派单" :image-size="80" />
          </el-col>

          <el-col :span="12">
            <div v-if="checkInRecord" class="table-container" style="box-shadow: none; border: 1px solid #ebeef5;">
              <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #303133;">
                <el-icon style="color: #67c23a;"><Calendar /></el-icon>
                签到记录
              </div>
              <el-descriptions :column="1" size="small">
                <el-descriptions-item label="签到时间">{{ formatDate(checkInRecord.checkInTime) }}</el-descriptions-item>
                <el-descriptions-item label="签退时间">{{ formatDate(checkInRecord.checkOutTime) || '服务进行中' }}</el-descriptions-item>
                <el-descriptions-item label="服务时长">{{ checkInRecord.serviceHours ? checkInRecord.serviceHours + ' 小时' : '-' }}</el-descriptions-item>
                <el-descriptions-item label="签到地点">{{ checkInRecord.checkInLocation || '-' }}</el-descriptions-item>
                <el-descriptions-item label="服务记录">{{ checkInRecord.serviceRecord || '-' }}</el-descriptions-item>
                <el-descriptions-item label="老人状况">{{ checkInRecord.elderCondition || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else :description="workOrder ? '护理员尚未签到' : '暂无签到信息'" :image-size="80" />
          </el-col>
        </el-row>

        <div v-if="settlement" class="table-container" style="margin-top: 16px; box-shadow: none; border: 1px solid #ebeef5;">
          <div style="font-size: 14px; font-weight: 600; margin-bottom: 12px; color: #303133;">
            <el-icon style="color: #f56c6c;"><Money /></el-icon>
            结算信息
          </div>
          <el-descriptions :column="3" size="small" border>
            <el-descriptions-item label="结算单号">{{ settlement.settlementNo }}</el-descriptions-item>
            <el-descriptions-item label="结算状态">
              <el-tag :type="settlementStatusType(settlement.status)">{{ getSettlementStatusText(settlement.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="结算时间">{{ formatDate(settlement.settleTime) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="服务时长">{{ settlement.serviceHours || 0 }} 小时</el-descriptions-item>
            <el-descriptions-item label="单价">¥{{ settlement.hourlyRate || 0 }}/小时</el-descriptions-item>
            <el-descriptions-item label="基础费用">¥{{ settlement.baseAmount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="额外费用">{{ settlement.extraAmount ? '¥' + settlement.extraAmount : '-' }}</el-descriptions-item>
            <el-descriptions-item label="优惠减免">{{ settlement.discountAmount ? '-¥' + settlement.discountAmount : '-' }}</el-descriptions-item>
            <el-descriptions-item label="应收总额">
              <span style="color: #f56c6c; font-weight: 600; font-size: 16px;">¥{{ settlement.totalAmount || 0 }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </template>

      <el-empty v-else description="未找到该需求信息" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import * as api from '@/api'
import dayjs from 'dayjs'

const route = useRoute()
const demandId = computed(() => route.params.demandId)
const loading = ref(false)
const demand = ref(null)
const workOrder = ref(null)
const checkInRecord = ref(null)
const settlement = ref(null)

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
const getSettlementStatusText = (s) => ({
  PENDING: '待结算', SETTLED: '已结算', CANCELLED: '已取消'
})[s] || s
const settlementStatusType = (s) => ({
  PENDING: 'warning', SETTLED: 'success', CANCELLED: 'info'
})[s] || 'info'

const currentStep = computed(() => {
  let step = 0
  if (demand.value) {
    step = 1
    if (workOrder.value) {
      step = 2
      if (checkInRecord.value) {
        step = 3
        if (checkInRecord.value.checkOutTime) {
          step = 4
          if (settlement.value && settlement.value.status === 'SETTLED') {
            step = 5
          }
        }
      }
    }
  }
  return step
})

const loadTraceData = async () => {
  loading.value = true
  try {
    demand.value = await api.getDemand(demandId.value)

    const orders = await api.getWorkOrdersByDemand(demandId.value)
    if (orders && orders.length > 0) {
      workOrder.value = orders[0]

      const records = await api.getCheckInsByWorkOrder(workOrder.value.id)
      if (records && records.length > 0) {
        checkInRecord.value = records.sort((a, b) => new Date(b.checkInTime) - new Date(a.checkInTime))[0]
      }

      const settlements = await api.getSettlementsByWorkOrder(workOrder.value.id)
      if (settlements && settlements.length > 0) {
        settlement.value = settlements[0]
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(loadTraceData)
</script>
