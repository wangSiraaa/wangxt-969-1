<template>
  <div class="page-container">
    <div class="page-header">
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h2>工单详情</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="mb-16">
          <template #header>
            <div class="flex-between">
              <span class="card-title">工单基本信息</span>
              <el-tag :type="statusMap[order?.status]?.type || 'info'" size="large">
                {{ statusMap[order?.status]?.label || order?.status }}
              </el-tag>
            </div>
          </template>
          <el-descriptions v-if="order" :column="2" border>
            <el-descriptions-item label="工单编号">{{ order.orderCode }}</el-descriptions-item>
            <el-descriptions-item label="需求编号">{{ order.demandCode }}</el-descriptions-item>
            <el-descriptions-item label="老人">{{ order.elderName }}</el-descriptions-item>
            <el-descriptions-item label="护理员">{{ order.nurseName }}</el-descriptions-item>
            <el-descriptions-item label="服务包">{{ order.servicePackageName }}</el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag v-if="order.riskLevel"
                :type="riskMap[order.riskLevel]?.type || 'info'">
                {{ riskMap[order.riskLevel]?.label || order.riskLevel }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="计划时间" :span="2">
              {{ order.scheduledDate }} {{ order.scheduledStartTime }} ~ {{ order.scheduledEndTime }}
            </el-descriptions-item>
            <el-descriptions-item label="服务地址" :span="2">{{ order.address }}</el-descriptions-item>
            <el-descriptions-item label="派单时间">{{ order.dispatchedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="接单时间">{{ order.acceptedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="服务开始">{{ order.startedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ order.completedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-if="checkIn" class="mb-16">
          <template #header><span class="card-title">签到签退记录</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="签到时间">{{ checkIn.checkInTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="签退时间">{{ checkIn.checkOutTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="签到地址">{{ checkIn.checkInAddress || '-' }}</el-descriptions-item>
            <el-descriptions-item label="签退地址">{{ checkIn.checkOutAddress || '-' }}</el-descriptions-item>
            <el-descriptions-item label="签到定位">
              {{ checkIn.checkInLongitude }}, {{ checkIn.checkInLatitude }}
            </el-descriptions-item>
            <el-descriptions-item label="服务时长">
              {{ checkIn.serviceDurationMinutes || 0 }} 分钟
            </el-descriptions-item>
            <el-descriptions-item label="签到照片">
              <el-image v-if="checkIn.checkInPhotoUrl"
                :src="`https://via.placeholder.com/120x90?text=签到照片占位`"
                style="width:120px;height:90px" fit="cover" />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="签退照片">
              <el-image v-if="checkIn.checkOutPhotoUrl"
                :src="`https://via.placeholder.com/120x90?text=签退照片占位`"
                style="width:120px;height:90px" fit="cover" />
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-if="serviceRecord" class="mb-16">
          <template #header><span class="card-title">服务记录</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="服务内容">{{ serviceRecord.serviceContent || '-' }}</el-descriptions-item>
            <el-descriptions-item label="健康观察">{{ serviceRecord.healthObservation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用药情况">{{ serviceRecord.medicationAdministered || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生命体征">{{ serviceRecord.vitalSigns || '-' }}</el-descriptions-item>
            <el-descriptions-item label="服务照片">
              <el-image v-if="serviceRecord.servicePhotos"
                :src="`https://via.placeholder.com/160x120?text=服务照片占位`"
                style="width:160px;height:120px" fit="cover" />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="异常情况">{{ serviceRecord.abnormalSituation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="风险事件">
              <el-tag v-if="serviceRecord.riskEventOccurred" type="danger">已发生</el-tag>
              <span v-else>无</span>
              <span v-if="serviceRecord.riskEventDetail">：{{ serviceRecord.riskEventDetail }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-if="familyConfirm" class="mb-16">
          <template #header><span class="card-title">家属确认</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="确认人">{{ familyConfirm.confirmedBy || '-' }}</el-descriptions-item>
            <el-descriptions-item label="确认时间">{{ familyConfirm.confirmedAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="确认结果">
              <el-tag :type="familyConfirm.confirmed ? 'success' : 'danger'">
                {{ familyConfirm.confirmed ? '已确认' : '已拒绝' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="确认状态">
              <el-tag size="small">{{ familyConfirm.status }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="备注" v-if="familyConfirm.confirmRemark" :span="2">
              {{ familyConfirm.confirmRemark }}
            </el-descriptions-item>
            <el-descriptions-item label="拒绝原因" v-if="familyConfirm.rejectReason" :span="2">
              {{ familyConfirm.rejectReason }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card v-if="settlement" class="mb-16">
          <template #header><span class="card-title">结算信息</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="结算单号">{{ settlement.settlementCode }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="settlement.status === 'SETTLED' ? 'success' : 'warning'">
                {{ settlement.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="基础金额">¥ {{ settlement.baseAmount }}</el-descriptions-item>
            <el-descriptions-item label="额外费用">¥ {{ settlement.extraAmount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="总金额">¥ {{ settlement.totalAmount }}</el-descriptions-item>
            <el-descriptions-item label="保险报销">¥ {{ settlement.insuranceAmount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="自付金额">¥ {{ settlement.selfPayAmount }}</el-descriptions-item>
            <el-descriptions-item label="结算时间">{{ settlement.settledAt || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="mb-16">
          <template #header><span class="card-title">异常事件（{{ abnormals.length }}）</span></template>
          <el-table v-if="abnormals.length" :data="abnormals" size="small" border>
            <el-table-column prop="eventCode" label="编号" width="170" />
            <el-table-column label="类型" width="140">
              <template #default="{ row }">
                <el-tag :type="abnormalMap[row.abnormalType]?.type || 'info'" size="small">
                  {{ abnormalMap[row.abnormalType]?.label || row.abnormalType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="严重" width="90">
              <template #default="{ row }">
                <el-tag :type="row.severity==='CRITICAL'?'danger':row.severity==='HIGH'?'warning':'info'" size="small">
                  {{ row.severity }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status==='RESOLVED'?'success':row.status==='PROCESSING'?'warning':'danger'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="detectedAt" label="发现时间" width="170" />
          </el-table>
          <el-empty v-else description="暂无异常事件" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="mb-16">
          <template #header><span class="card-title">快捷操作</span></template>
          <div style="display:flex;flex-direction:column;gap:10px;">
            <el-button v-if="order && canAccept(order.status)" type="primary" @click="handleAccept">
              护理员接单
            </el-button>
            <el-button v-if="order && canCheckIn(order.status)" type="success" @click="handleCheckIn">
              签到
            </el-button>
            <el-button v-if="order && canCheckOut(order.status)" type="warning" @click="handleCheckOut">
              签退
            </el-button>
            <el-button v-if="order && canConfirm(order.status)" type="primary" plain @click="handleFamilyConfirm">
              家属确认
            </el-button>
            <el-button v-if="order && canSettle(order.status)" type="success" plain @click="handleSettle">
              申请结算
            </el-button>
            <el-button v-if="order && canReassign(order.status)" type="warning" plain @click="handleReassign">
              改派
            </el-button>
            <el-button v-if="order && canCancel(order.status)" type="danger" plain @click="handleCancel">
              取消工单
            </el-button>
          </div>
        </el-card>

        <el-card class="mb-16">
          <template #header><span class="card-title">审计日志</span></template>
          <el-timeline v-if="auditLogs.length">
            <el-timeline-item
              v-for="(log, i) in auditLogs.slice(0, 15)" :key="i"
              :timestamp="log.operatedAt"
              :type="log.operation.includes('ABNORMAL')||log.operation.includes('REJECT')?'danger':log.operation.includes('SETTLE')||log.operation.includes('CONFIRM')?'success':'primary'"
              size="large"
            >
              <div><strong>{{ log.operation }}</strong></div>
              <div style="color:#909399;font-size:12px">操作人：{{ log.operator || 'SYSTEM' }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无审计日志" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { workOrderApi, checkInApi, familyApi, settlementApi, abnormalApi } from '@/api'
import { statusMap, riskLevelMap, abnormalTypeMap } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const order = ref(null)
const checkIn = ref(null)
const serviceRecord = ref(null)
const familyConfirm = ref(null)
const settlement = ref(null)
const abnormals = ref([])
const auditLogs = ref([])
const riskMap = riskLevelMap
const abnormalMap = abnormalTypeMap

const canAccept = s => s === 'DISPATCHED'
const canCheckIn = s => ['DISPATCHED', 'NURSE_ACCEPTED'].includes(s)
const canCheckOut = s => ['CHECKED_IN', 'IN_PROGRESS'].includes(s)
const canConfirm = s => ['SERVICE_COMPLETED', 'PENDING_FAMILY_CONFIRM', 'FAMILY_REJECTED'].includes(s)
const canSettle = s => s === 'FAMILY_CONFIRMED'
const canReassign = s => ['DISPATCHED', 'NURSE_ACCEPTED', 'ABNORMAL'].includes(s)
const canCancel = s => ['DISPATCHED', 'NURSE_ACCEPTED'].includes(s)

const loadData = async () => {
  const id = route.params.id
  try {
    order.value = await workOrderApi.get(id)
  } catch (e) { ElMessage.error('加载工单详情失败') }
  try {
    checkIn.value = await checkInApi.getByWorkOrder(id)
  } catch (e) {}
  try {
    serviceRecord.value = await checkInApi.getServiceRecord(id)
  } catch (e) {}
  try {
    familyConfirm.value = await familyApi.getByWorkOrder(id)
  } catch (e) {}
  try {
    settlement.value = await settlementApi.getByWorkOrder(id)
  } catch (e) {}
  try {
    abnormals.value = await abnormalApi.getByWorkOrder(id)
  } catch (e) {}
  try {
    auditLogs.value = await workOrderApi.getAuditLogs(id)
  } catch (e) {}
}

const handleAccept = async () => {
  try {
    await ElMessageBox.confirm(`护理员 ${order.value.nurseName} 确认接单？`, '确认接单')
    await workOrderApi.accept(order.value.id, order.value.nurseId)
    ElMessage.success('接单成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '操作失败') }
}

const handleCheckIn = async () => {
  try {
    await ElMessageBox.confirm(`护理员 ${order.value.nurseName} 确认签到？`, '确认签到')
    await checkInApi.checkIn({
      workOrderId: order.value.id,
      nurseId: order.value.nurseId,
      checkInLongitude: order.value.longitude,
      checkInLatitude: order.value.latitude,
      checkInAddress: order.value.address
    })
    ElMessage.success('签到成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '签到失败') }
}

const handleCheckOut = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请填写服务内容', '签退确认', {
      inputType: 'textarea',
      inputValue: '按计划完成服务，老人状态良好',
      confirmButtonText: '确认签退'
    })
    await checkInApi.checkOut({
      workOrderId: order.value.id,
      nurseId: order.value.nurseId,
      serviceContent: value,
      healthObservation: '生命体征平稳，情绪良好',
      checkOutLongitude: order.value.longitude,
      checkOutLatitude: order.value.latitude,
      checkOutAddress: order.value.address
    })
    ElMessage.success('签退成功，请等待家属确认')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '签退失败') }
}

const handleFamilyConfirm = async () => {
  try {
    await ElMessageBox.confirm('家属确认服务完成？', '家属确认', { type: 'success' })
    await familyApi.confirm({
      workOrderId: order.value.id,
      confirmed: true,
      confirmedBy: '家属演示',
      confirmRemark: '服务满意，老人状态良好'
    })
    ElMessage.success('家属确认成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '确认失败') }
}

const handleSettle = async () => {
  try {
    await ElMessageBox.confirm('确认生成结算单？', '结算确认', { type: 'warning' })
    await settlementApi.create({
      workOrderId: order.value.id,
      settledBy: '财务演示'
    })
    ElMessage.success('结算单创建成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '结算失败') }
}

const handleReassign = () => {
  ElMessage.info('改派功能：请前往待派单页面重新选择护理员派单')
  router.push('/dispatch/pending')
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定取消该工单？', '取消工单', { type: 'warning' })
    await workOrderApi.cancel(order.value.id, '详情页取消', '操作人')
    ElMessage.success('取消成功')
    loadData()
  } catch (e) { if (e !== 'cancel') ElMessage.error(e?.message || '取消失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header {
  display: flex; align-items: center; gap: 16px; margin-bottom: 16px;
}
.card-title { font-size: 16px; font-weight: 600; }
.flex-between {
  display: flex; justify-content: space-between; align-items: center;
}
.mb-16 { margin-bottom: 16px; }
</style>
