<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">结算管理</div>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" :model="filter">
        <el-form-item label="结算状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待结算" value="PENDING" />
            <el-option label="已结算" value="SETTLED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="16">
      <el-col :span="14">
        <div class="table-container">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div style="font-size: 16px; font-weight: 600; color: #303133;">已完成工单（待结算）</div>
            <el-button type="success" @click="createSettlement(currentOrder)" :disabled="!currentOrder">
              <el-icon><Money /></el-icon>
              <span>创建结算单</span>
            </el-button>
          </div>
          <el-table
            :data="completedOrders"
            stripe border highlight-current-row
            @current-change="handleOrderSelect"
          >
            <el-table-column prop="orderNo" label="工单号" width="180" />
            <el-table-column prop="elderName" label="老人" width="80" />
            <el-table-column prop="caregiverName" label="护理员" width="80" />
            <el-table-column prop="serviceType" label="服务类型" width="100" />
            <el-table-column label="预约时间" width="150">
              <template #default="{ row }">{{ formatDate(row.scheduledTime) }}</template>
            </el-table-column>
            <el-table-column label="是否已签到" width="100">
              <template #default="{ row }">
                <el-tag v-if="checkedInMap[row.id]" type="success" size="small">已签到</el-tag>
                <el-tag v-else type="danger" size="small">未签到</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div style="color: #909399; font-size: 12px; margin-top: 12px;">
            <el-icon><InfoFilled /></el-icon>
            注意：未签到的工单不能创建结算单
          </div>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="table-container" style="min-height: 500px">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
            创建结算单
          </div>

          <div v-if="!currentOrder" style="color: #909399; text-align: center; padding: 80px 0;">
            <el-icon style="font-size: 48px; color: #dcdfe6;"><Money /></el-icon>
            <div style="margin-top: 12px;">请在左侧选择已完成工单</div>
          </div>

          <div v-else>
            <el-descriptions :column="1" border size="small" style="margin-bottom: 16px;">
              <el-descriptions-item label="工单号">{{ currentOrder.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="老人">{{ currentOrder.elderName }}</el-descriptions-item>
              <el-descriptions-item label="护理员">{{ currentOrder.caregiverName }}</el-descriptions-item>
              <el-descriptions-item label="服务类型">{{ currentOrder.serviceType }}</el-descriptions-item>
            </el-descriptions>

            <el-form label-width="100px" ref="settleFormRef" :model="settleForm" :rules="settleRules">
              <el-form-item label="服务时长">
                <el-input v-model="settleForm.serviceHoursDisplay" disabled />
                <div style="color: #909399; font-size: 12px; margin-top: 4px;">根据签到记录自动计算</div>
              </el-form-item>
              <el-form-item label="每小时单价(元)" prop="hourlyRate">
                <el-input-number v-model="settleForm.hourlyRate" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
              <el-form-item label="基础费用">
                <el-input :value="'¥ ' + baseAmount" disabled />
              </el-form-item>
              <el-form-item label="额外费用(元)">
                <el-input-number v-model="settleForm.extraAmount" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
              <el-form-item label="额外费用说明">
                <el-input v-model="settleForm.extraRemark" placeholder="选填" />
              </el-form-item>
              <el-form-item label="优惠减免(元)">
                <el-input-number v-model="settleForm.discountAmount" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
              <el-form-item label="优惠说明">
                <el-input v-model="settleForm.discountRemark" placeholder="选填" />
              </el-form-item>
              <el-form-item label="结算备注">
                <el-input v-model="settleForm.settleRemark" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="应收总额">
                <div style="font-size: 22px; font-weight: 600; color: #f56c6c;">¥ {{ totalAmount }}</div>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="table-container" style="margin-top: 16px;">
      <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
        结算单列表
      </div>
      <el-table :data="filteredList" stripe border>
        <el-table-column prop="settlementNo" label="结算单号" width="200" />
        <el-table-column prop="elderName" label="老人" width="80" />
        <el-table-column prop="caregiverName" label="护理员" width="80" />
        <el-table-column label="服务时长" width="100">
          <template #default="{ row }">{{ row.serviceHours || 0 }} 小时</template>
        </el-table-column>
        <el-table-column label="基础费用" width="100">
          <template #default="{ row }">¥{{ row.baseAmount || 0 }}</template>
        </el-table-column>
        <el-table-column label="总额" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600;">¥{{ row.totalAmount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="settlementStatusType(row.status)">{{ getSettlementStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结算时间" width="160">
          <template #default="{ row }">{{ formatDate(row.settleTime) || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              link type="success"
              @click="handleConfirm(row)"
            >确认结算</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              link type="danger"
              @click="handleCancel(row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as api from '@/api'
import dayjs from 'dayjs'

const list = ref([])
const completedOrders = ref([])
const currentOrder = ref(null)
const filter = reactive({ status: '' })
const settleFormRef = ref()
const currentCheckInRecord = ref(null)
const settleForm = reactive({
  hourlyRate: 50.00,
  extraAmount: 0,
  extraRemark: '',
  discountAmount: 0,
  discountRemark: '',
  settleRemark: '',
  serviceHoursDisplay: '-'
})
const settleRules = {
  hourlyRate: [{ required: true, message: '请输入单价', trigger: 'blur' }]
}
const checkedInMap = reactive({})

const filteredList = computed(() => {
  return filter.status ? list.value.filter(s => s.status === filter.status) : list.value
})

const baseAmount = computed(() => {
  if (!currentCheckInRecord.value?.serviceHours) return '0.00'
  return (Number(currentCheckInRecord.value.serviceHours) * Number(settleForm.hourlyRate || 0)).toFixed(2)
})

const totalAmount = computed(() => {
  const base = parseFloat(baseAmount.value) || 0
  const extra = Number(settleForm.extraAmount) || 0
  const discount = Number(settleForm.discountAmount) || 0
  return Math.max(0, base + extra - discount).toFixed(2)
})

const formatDate = (d) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
const getSettlementStatusText = (s) => ({
  PENDING: '待结算', SETTLED: '已结算', CANCELLED: '已取消'
})[s] || s
const settlementStatusType = (s) => ({
  PENDING: 'warning', SETTLED: 'success', CANCELLED: 'info'
})[s] || 'info'

const loadData = async () => {
  list.value = await api.getSettlements()
}

const loadCompletedOrders = async () => {
  const all = await api.getWorkOrders()
  completedOrders.value = all.filter(o => o.orderStatus === '已完成')

  for (const order of completedOrders.value) {
    try {
      checkedInMap[order.id] = await api.hasCheckedIn(order.id)
    } catch (_) {
      checkedInMap[order.id] = false
    }
  }
}

const handleOrderSelect = async (row) => {
  currentOrder.value = row
  currentCheckInRecord.value = null
  settleForm.serviceHoursDisplay = '-'

  if (row) {
    try {
      const latest = await api.getLatestCheckIn(row.id)
      if (latest) {
        currentCheckInRecord.value = latest
        settleForm.serviceHoursDisplay = (latest.serviceHours || 0) + ' 小时'
      }
    } catch (_) {}
  }
}

const resetFilter = () => {
  filter.status = ''
  loadData()
}

const createSettlement = async (order) => {
  if (!order) return
  if (!checkedInMap[order.id]) {
    ElMessage.error('该工单尚未签到完成，不能结算')
    return
  }
  if (!currentCheckInRecord.value?.checkOutTime) {
    ElMessage.error('该工单尚未签退完成，不能结算')
    return
  }
  try {
    await api.createSettlement({
      workOrderId: order.id,
      hourlyRate: settleForm.hourlyRate,
      extraAmount: settleForm.extraAmount,
      discountAmount: settleForm.discountAmount,
      extraRemark: settleForm.extraRemark,
      discountRemark: settleForm.discountRemark,
      settleRemark: settleForm.settleRemark
    })
    ElMessage.success('结算单创建成功')
    settleForm.hourlyRate = 50.00
    settleForm.extraAmount = 0
    settleForm.extraRemark = ''
    settleForm.discountAmount = 0
    settleForm.discountRemark = ''
    settleForm.settleRemark = ''
    loadData()
  } catch (_) {}
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(`确认结算「${row.settlementNo}」总额 ¥${row.totalAmount}？`, '确认结算', { type: 'warning' })
    await api.confirmSettlement(row.id, '管理员')
    ElMessage.success('结算成功')
    loadData()
  } catch (_) {}
}

const handleCancel = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消结算', {
      confirmButtonText: '确认', cancelButtonText: '取消', inputPlaceholder: '取消原因'
    })
    await api.cancelSettlement(row.id, value)
    ElMessage.success('已取消')
    loadData()
  } catch (_) {}
}

onMounted(() => {
  loadData()
  loadCompletedOrders()
})
</script>
