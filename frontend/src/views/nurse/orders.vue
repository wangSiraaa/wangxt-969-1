<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的工单</h2>
      <el-select v-model="currentNurseId" @change="loadData" style="width:200px">
        <el-option v-for="n in nurseList" :key="n.id" :label="n.name" :value="n.id" />
      </el-select>
    </div>

    <el-tabs v-model="activeTab" type="card" class="mb-16">
      <el-tab-pane label="待执行" name="active" />
      <el-tab-pane label="今日排班" name="daily" />
      <el-tab-pane label="全部工单" name="all" />
    </el-tabs>

    <el-card v-if="activeTab === 'daily'" class="mb-16">
      <template #header>
        <div class="flex-between">
          <span>当日排班 - {{ currentDate }}</span>
          <el-date-picker v-model="currentDate" type="date" value-format="YYYY-MM-DD" @change="loadData" />
        </div>
      </template>
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step
          v-for="(order, idx) in dailyOrders" :key="order.id"
          :title="order.servicePackageName"
          :description="`${order.scheduledStartTime}-${order.scheduledEndTime}\n${order.elderName}`"
          :status="getStepStatus(order.status, idx)"
        />
      </el-steps>
    </el-card>

    <el-card class="table-card">
      <el-table :data="orders" v-loading="loading" stripe style="width:100%">
        <el-table-column prop="orderCode" label="工单号" width="180" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="servicePackageName" label="服务" />
        <el-table-column label="时间" width="220">
          <template #default="{ row }">
            {{ row.scheduledDate }}<br />
            {{ row.scheduledStartTime }} ~ {{ row.scheduledEndTime }}
          </template>
        </el-table-column>
        <el-table-column label="地址" show-overflow-tooltip>
          <template #default="{ row }">{{ row.address }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status==='DISPATCHED'" type="primary" link @click="handleAccept(row)">接单</el-button>
            <el-button v-if="['DISPATCHED','NURSE_ACCEPTED'].includes(row.status)" type="success" link @click="handleCheckIn(row)">签到</el-button>
            <el-button v-if="['CHECKED_IN','IN_PROGRESS'].includes(row.status)" type="warning" link @click="handleCheckOut(row)">签退</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { workOrderApi, checkInApi, masterDataApi } from '@/api'
import { statusMap } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const nurseList = ref([])
const currentNurseId = ref(null)
const activeTab = ref('active')
const activeOrders = ref([])
const allOrders = ref([])
const dailyOrders = ref([])
const currentDate = ref(dayjs().format('YYYY-MM-DD'))

const orders = computed(() => {
  if (activeTab.value === 'active') return activeOrders.value
  if (activeTab.value === 'daily') return dailyOrders.value
  return allOrders.value
})

const activeStep = computed(() => {
  const idx = dailyOrders.value.findIndex(o =>
    !['CANCELLED', 'SETTLED', 'FAMILY_CONFIRMED', 'SERVICE_COMPLETED', 'PENDING_FAMILY_CONFIRM'].includes(o.status)
  )
  return idx >= 0 ? idx : dailyOrders.value.length
})

const getStepStatus = (status, idx) => {
  if (['CANCELLED', 'ABNORMAL'].includes(status)) return 'error'
  if (['SETTLED', 'FAMILY_CONFIRMED', 'PENDING_FAMILY_CONFIRM', 'SERVICE_COMPLETED', 'CHECKED_OUT'].includes(status)) return 'success'
  if (idx <= activeStep.value) return 'process'
  return 'wait'
}

const loadNurses = async () => {
  try {
    nurseList.value = await masterDataApi.listActiveNurses() || []
    if (nurseList.value.length && !currentNurseId.value) {
      currentNurseId.value = nurseList.value[0].id
      loadData()
    }
  } catch (e) { console.error(e) }
}

const loadData = async () => {
  if (!currentNurseId.value) return
  loading.value = true
  try {
    activeOrders.value = await workOrderApi.listNurseActive(currentNurseId.value) || []
    allOrders.value = await workOrderApi.listByNurse(currentNurseId.value) || []
    dailyOrders.value = await workOrderApi.listNurseDaily(currentNurseId.value, currentDate.value) || []
  } catch (e) {
    ElMessage.error('加载工单失败')
  } finally { loading.value = false }
}

const viewDetail = (row) => router.push(`/dispatch/order/${row.id}`)

const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm(`确认接单：${row.servicePackageName}？`, '接单确认')
    await workOrderApi.accept(row.id, currentNurseId.value)
    ElMessage.success('接单成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '失败') }
}

const handleCheckIn = async (row) => {
  try {
    await ElMessageBox.confirm(`确认签到？将记录当前位置和时间。`, '签到确认')
    await checkInApi.checkIn({
      workOrderId: row.id, nurseId: currentNurseId.value,
      checkInLongitude: row.longitude, checkInLatitude: row.latitude,
      checkInAddress: row.address
    })
    ElMessage.success('签到成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '签到失败') }
}

const handleCheckOut = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请填写服务内容', '签退', {
      inputType: 'textarea',
      inputValue: '按计划完成服务',
      confirmButtonText: '确认签退'
    })
    await checkInApi.checkOut({
      workOrderId: row.id, nurseId: currentNurseId.value,
      serviceContent: value,
      healthObservation: '状态良好',
      checkOutLongitude: row.longitude, checkOutLatitude: row.latitude,
      checkOutAddress: row.address
    })
    ElMessage.success('签退成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '签退失败') }
}

onMounted(() => loadNurses())
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.mb-16 { margin-bottom: 16px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.table-card { margin-bottom: 16px; }
</style>
