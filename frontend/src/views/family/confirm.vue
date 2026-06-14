<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待家属确认</h2>
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <el-alert
      title="家属确认说明"
      type="info" :closable="false" show-icon class="mb-16">
      <template #default>
        请核对护理员的服务记录、服务时长、服务照片，确认无误后点击"确认"按钮；如有问题可选择"拒绝"并填写原因。
      </template>
    </el-alert>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderCode" label="工单编号" width="160" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="nurseName" label="护理员" width="100" />
        <el-table-column prop="servicePackageName" label="服务包" width="140" />
        <el-table-column label="服务时间" width="220">
          <template #default="{ row }">
            {{ formatDate(row.scheduledDate) }} {{ row.scheduledStartTime }}-{{ row.scheduledEndTime }}
          </template>
        </el-table-column>
        <el-table-column label="实际服务时长" width="140">
          <template #default="{ row }">
            <span v-if="row.serviceDurationMinutes">{{ row.serviceDurationMinutes }}分钟</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="完成时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.completedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
            <el-button type="success" link @click="handleConfirm(row)">确认</el-button>
            <el-button type="danger" link @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="服务记录详情" width="800px" destroy-on-close>
      <div v-if="currentOrder">
        <el-descriptions :column="2" border size="small" class="mb-20">
          <el-descriptions-item label="工单编号">{{ currentOrder.orderCode }}</el-descriptions-item>
          <el-descriptions-item label="老人">{{ currentOrder.elderName }}</el-descriptions-item>
          <el-descriptions-item label="护理员">{{ currentOrder.nurseName }}</el-descriptions-item>
          <el-descriptions-item label="服务包">{{ currentOrder.servicePackageName }}</el-descriptions-item>
          <el-descriptions-item label="签到时间">{{ formatDateTime(currentCheckIn?.checkInTime) }}</el-descriptions-item>
          <el-descriptions-item label="签退时间">{{ formatDateTime(currentCheckIn?.checkOutTime) }}</el-descriptions-item>
          <el-descriptions-item label="签到地点">{{ currentCheckIn?.checkInAddress || '-' }}</el-descriptions-item>
          <el-descriptions-item label="服务时长">{{ currentCheckIn?.serviceDurationMinutes || '-' }}分钟</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">服务记录</el-divider>
        <el-descriptions :column="1" border size="small" class="mb-20">
          <el-descriptions-item label="服务内容">{{ currentServiceRecord?.serviceContent || '-' }}</el-descriptions-item>
          <el-descriptions-item label="老人状况">{{ currentServiceRecord?.elderCondition || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用药情况">{{ currentServiceRecord?.medicationStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="异常情况">{{ currentServiceRecord?.abnormalSituation || '无' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentServiceRecord?.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">服务照片</el-divider>
        <div class="photo-placeholder">
          <el-icon :size="40"><Picture /></el-icon>
          <p>服务照片占位 - 实际部署后支持图片上传</p>
        </div>

        <el-divider content-position="left">签到定位</el-divider>
        <div class="photo-placeholder">
          <el-icon :size="40"><Location /></el-icon>
          <p>签到定位占位 - 经度：{{ currentCheckIn?.checkInLongitude || '-' }}，纬度：{{ currentCheckIn?.checkInLatitude || '-' }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleConfirm(currentOrder)">确认服务</el-button>
        <el-button type="danger" @click="handleReject(currentOrder)">拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="拒绝确认" width="500px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" required>
          <el-input v-model="rejectForm.rejectReason" type="textarea" :rows="4" placeholder="请输入拒绝原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { workOrderApi, checkInApi, familyApi } from '@/api'
import { statusMap, formatDate, formatDateTime } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentOrder = ref(null)
const currentCheckIn = ref(null)
const currentServiceRecord = ref(null)
const submitting = ref(false)
const rejectForm = reactive({
  rejectReason: ''
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await workOrderApi.listByStatus('PENDING_FAMILY_CONFIRM')
    tableData.value = data || []
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  currentOrder.value = row
  try {
    const [ci, sr] = await Promise.all([
      checkInApi.getByWorkOrder(row.id),
      checkInApi.getServiceRecord(row.id)
    ])
    currentCheckIn.value = ci
    currentServiceRecord.value = sr
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认服务记录无误？确认后将进入结算流程。', '确认服务', { type: 'success' })
    await familyApi.confirm({
      workOrderId: row.id,
      confirmed: true,
      confirmedBy: '家属',
      confirmationRemark: '服务记录确认无误'
    })
    ElMessage.success('确认成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  }
}

const handleReject = (row) => {
  currentOrder.value = row
  rejectForm.rejectReason = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.rejectReason.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  submitting.value = true
  try {
    await familyApi.confirm({
      workOrderId: currentOrder.value.id,
      confirmed: false,
      confirmedBy: '家属',
      confirmationRemark: rejectForm.rejectReason
    })
    ElMessage.success('已提交拒绝')
    rejectVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.table-card { margin-bottom: 16px; }
.mb-16 { margin-bottom: 16px; }
.mb-20 { margin-bottom: 20px; }
.photo-placeholder {
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  padding: 40px;
  text-align: center;
  color: #909399;
  background: #fafafa;
}
.text-gray { color: #909399; }
</style>
