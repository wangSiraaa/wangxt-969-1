<template>
  <div class="page-container">
    <div class="page-header">
      <h2>异常事件队列</h2>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="全部状态" style="width: 140px; margin-right: 10px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已解决" value="RESOLVED" />
        </el-select>
        <el-select v-model="typeFilter" placeholder="全部类型" style="width: 160px; margin-right: 10px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="服务超时" value="SERVICE_TIMEOUT" />
          <el-option label="临时改派" value="TEMP_REASSIGN" />
          <el-option label="未签退" value="NO_CHECKOUT" />
          <el-option label="家属拒确认" value="FAMILY_REJECTED" />
          <el-option label="资质不匹配" value="QUALIFICATION_MISMATCH" />
          <el-option label="老人暂停服务" value="ELDER_PAUSED" />
          <el-option label="未签到" value="NO_CHECKIN" />
          <el-option label="区域不匹配" value="AREA_MISMATCH" />
          <el-option label="时间冲突" value="TIME_CONFLICT" />
          <el-option label="风险等级不匹配" value="RISK_LEVEL_MISMATCH" />
        </el-select>
        <el-button type="primary" @click="handleDetect">
          <el-icon><Refresh /></el-icon> 检测异常
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="eventCode" label="异常编号" width="160" />
        <el-table-column prop="abnormalType" label="类型" width="140">
          <template #default="{ row }">
            <el-tag :type="getAbnormalType(row.abnormalType)?.type || 'info'" size="small">
              {{ getAbnormalType(row.abnormalType)?.label || row.abnormalType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getSeverityType(row.severity)" size="small">{{ row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderCode" label="工单编号" width="160" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="nurseName" label="护理员" width="100" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="200" />
        <el-table-column label="检测方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.autoDetected ? 'info' : 'warning'" size="small">
              {{ row.autoDetected ? '系统检测' : '人工上报' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detectedAt" label="检测时间" width="160" />
        <el-table-column label="处理人" width="100">
          <template #default="{ row }">{{ row.handlerName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING' || row.status === 'PROCESSING'" type="success" link @click="handleResolve(row)">
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="异常详情" width="700px">
      <div v-if="currentAbnormal">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="异常编号">{{ currentAbnormal.eventCode }}</el-descriptions-item>
          <el-descriptions-item label="异常类型">
            <el-tag :type="getAbnormalType(currentAbnormal.abnormalType)?.type || 'info'" size="small">
              {{ getAbnormalType(currentAbnormal.abnormalType)?.label || currentAbnormal.abnormalType }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="严重级别">
            <el-tag :type="getSeverityType(currentAbnormal.severity)" size="small">{{ currentAbnormal.severity }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentAbnormal.status)" size="small">
              {{ getStatusLabel(currentAbnormal.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联工单">{{ currentAbnormal.orderCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联老人">{{ currentAbnormal.elderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联护理员">{{ currentAbnormal.nurseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="检测方式">
            {{ currentAbnormal.autoDetected ? '系统自动检测' : '人工上报' }}
          </el-descriptions-item>
          <el-descriptions-item label="发生时间">{{ currentAbnormal.occurredAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="检测时间">{{ currentAbnormal.detectedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentAbnormal.handlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="解决时间">{{ currentAbnormal.resolvedAt || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>异常描述</el-divider>
        <div class="description-box">{{ currentAbnormal.description }}</div>

        <el-divider>处理方案</el-divider>
        <div class="description-box">{{ currentAbnormal.resolution || '暂无处理方案' }}</div>

        <el-divider>处理备注</el-divider>
        <div class="description-box">{{ currentAbnormal.handlingNotes || '暂无处理备注' }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentAbnormal && currentAbnormal.status !== 'RESOLVED'" type="primary" @click="openResolve">
          处理异常
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resolveVisible" title="处理异常" width="600px">
      <el-form label-width="100px">
        <el-form-item label="处理方案">
          <el-input v-model="resolveForm.resolution" type="textarea" :rows="4" placeholder="请输入处理方案" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="resolveForm.handlingNotes" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="resolveForm.targetStatus" placeholder="选择工单目标状态" style="width: 100%">
            <el-option label="保持当前状态" value="" />
            <el-option label="已派发" value="DISPATCHED" />
            <el-option label="已接单" value="NURSE_ACCEPTED" />
            <el-option label="服务完成" value="SERVICE_COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResolve">提交处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { abnormalApi, dashboardApi } from '@/api'
import { abnormalTypeMap, abnormalStatusMap } from '@/utils'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const statusFilter = ref('')
const typeFilter = ref('')
const detailVisible = ref(false)
const resolveVisible = ref(false)
const currentAbnormal = ref(null)
const resolveForm = reactive({
  resolution: '',
  handlingNotes: '',
  targetStatus: ''
})

const getAbnormalType = (type) => abnormalTypeMap[type]
const getStatusType = (status) => abnormalStatusMap[status]?.type || 'info'
const getStatusLabel = (status) => abnormalStatusMap[status]?.label || status

const getSeverityType = (severity) => {
  const map = {
    CRITICAL: 'danger',
    HIGH: 'warning',
    MEDIUM: 'warning',
    LOW: 'info'
  }
  return map[severity] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    let data
    if (statusFilter.value === 'PENDING') {
      data = await abnormalApi.pending()
    } else if (typeFilter.value) {
      data = await abnormalApi.list({ type: typeFilter.value })
    } else {
      data = await abnormalApi.list({ page: 0, size: 100 })
      data = data?.content || data?.list || data || []
    }
    tableData.value = Array.isArray(data) ? data : (data?.content || data?.list || [])
  } catch (e) {
    console.error('加载异常列表失败', e)
    ElMessage.error('加载异常列表失败')
  } finally {
    loading.value = false
  }
}

const handleDetail = async (row) => {
  try {
    const data = await abnormalApi.get(row.id)
    currentAbnormal.value = data
    detailVisible.value = true
  } catch (e) {
    console.error('加载异常详情失败', e)
  }
}

const handleResolve = (row) => {
  currentAbnormal.value = row
  resolveForm.resolution = ''
  resolveForm.handlingNotes = ''
  resolveForm.targetStatus = ''
  resolveVisible.value = true
}

const openResolve = () => {
  resolveForm.resolution = ''
  resolveForm.handlingNotes = ''
  resolveForm.targetStatus = ''
  resolveVisible.value = true
}

const submitResolve = async () => {
  if (!currentAbnormal.value) return
  if (!resolveForm.resolution) {
    ElMessage.warning('请输入处理方案')
    return
  }
  try {
    await abnormalApi.resolve(currentAbnormal.value.id, {
      handlerId: 1,
      handlerName: '管理员',
      resolution: resolveForm.resolution,
      handlingNotes: resolveForm.handlingNotes,
      targetStatus: resolveForm.targetStatus || undefined
    })
    ElMessage.success('处理成功')
    resolveVisible.value = false
    detailVisible.value = false
    loadData()
  } catch (e) {
    console.error('处理异常失败', e)
    ElMessage.error('处理失败')
  }
}

const handleDetect = async () => {
  try {
    await dashboardApi.detectAbnormals()
    ElMessage.success('异常检测已执行')
    loadData()
  } catch (e) {
    console.error('异常检测失败', e)
    ElMessage.error('异常检测失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
}

.description-box {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 60px;
  color: #303133;
  line-height: 1.6;
}
</style>
