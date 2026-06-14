<template>
  <div class="page-container">
    <div class="page-header">
      <h2>补单审批</h2>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="全部状态" style="width: 150px; margin-right: 10px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="待审批" value="PENDING" />
          <el-option label="审批通过" value="APPROVED" />
          <el-option label="审批拒绝" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="openApply">
          <el-icon><Plus /></el-icon> 申请补单
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="supplementCode" label="补单编号" width="180" />
        <el-table-column prop="orderCode" label="原工单编号" width="160" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="nurseName" label="护理员" width="100" />
        <el-table-column prop="supplementType" label="补单类型" width="120" />
        <el-table-column prop="serviceDate" label="服务日期" width="120" />
        <el-table-column label="补单金额" width="100">
          <template #default="{ row }">¥{{ row.supplementAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="appliedAt" label="申请时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="success" link @click="approve(row)">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link @click="reject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无补单记录" />
    </el-card>

    <el-dialog v-model="applyVisible" title="申请补单" width="600px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="关联工单">
          <el-input v-model="applyForm.orderCode" placeholder="请输入工单编号" />
        </el-form-item>
        <el-form-item label="补单类型">
          <el-select v-model="applyForm.supplementType" placeholder="请选择补单类型" style="width: 100%">
            <el-option label="漏单补登" value="MISSED_ORDER" />
            <el-option label="超时补时" value="OVERTIME" />
            <el-option label="服务追加" value="EXTRA_SERVICE" />
            <el-option label="异常补偿" value="COMPENSATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务日期">
          <el-date-picker v-model="applyForm.serviceDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="补单金额">
          <el-input-number v-model="applyForm.supplementAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="补单原因">
          <el-input v-model="applyForm.reason" type="textarea" :rows="3" placeholder="请详细说明补单原因" />
        </el-form-item>
        <el-form-item label="申请人">
          <el-input v-model="applyForm.applicant" placeholder="请输入申请人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="补单详情" width="600px">
      <div v-if="currentSupplement">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="补单编号">{{ currentSupplement.supplementCode }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentSupplement.status)" size="small">
              {{ getStatusLabel(currentSupplement.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="原工单编号">{{ currentSupplement.orderCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="补单类型">{{ currentSupplement.supplementType }}</el-descriptions-item>
          <el-descriptions-item label="老人">{{ currentSupplement.elderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="护理员">{{ currentSupplement.nurseName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="服务日期">{{ currentSupplement.serviceDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="补单金额">¥{{ currentSupplement.supplementAmount || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentSupplement.applicant || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentSupplement.appliedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批人">{{ currentSupplement.approver || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">{{ currentSupplement.approvedAt || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>补单原因</el-divider>
        <div class="reason-box">{{ currentSupplement.reason || '暂无' }}</div>
        <el-divider>审批意见</el-divider>
        <div class="reason-box">{{ currentSupplement.approvalRemark || '暂无' }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { masterDataApi } from '@/api'
import { supplementStatusMap } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const statusFilter = ref('')
const applyVisible = ref(false)
const detailVisible = ref(false)
const currentSupplement = ref(null)
const applyForm = reactive({
  orderCode: '',
  supplementType: '',
  serviceDate: '',
  supplementAmount: 0,
  reason: '',
  applicant: ''
})

const getStatusType = (status) => supplementStatusMap[status]?.type || 'info'
const getStatusLabel = (status) => supplementStatusMap[status]?.label || status

const loadData = async () => {
  loading.value = true
  try {
    const data = await masterDataApi.listSupplements(statusFilter.value || undefined)
    tableData.value = data || []
  } catch (e) {
    console.error('加载补单列表失败', e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const openApply = () => {
  applyForm.orderCode = ''
  applyForm.supplementType = ''
  applyForm.serviceDate = ''
  applyForm.supplementAmount = 0
  applyForm.reason = ''
  applyForm.applicant = ''
  applyVisible.value = true
}

const submitApply = async () => {
  if (!applyForm.supplementType) {
    ElMessage.warning('请选择补单类型')
    return
  }
  if (!applyForm.reason) {
    ElMessage.warning('请填写补单原因')
    return
  }
  try {
    await masterDataApi.applySupplement({
      ...applyForm,
      appliedBy: applyForm.applicant
    })
    ElMessage.success('补单申请已提交')
    applyVisible.value = false
    loadData()
  } catch (e) {
    console.error('提交补单申请失败', e)
    ElMessage.error('提交失败')
  }
}

const viewDetail = async (row) => {
  try {
    const data = await masterDataApi.getSupplement(row.id)
    currentSupplement.value = data
    detailVisible.value = true
  } catch (e) {
    currentSupplement.value = row
    detailVisible.value = true
  }
}

const approve = async (row) => {
  try {
    await ElMessageBox.confirm(`确认审批通过补单 ${row.supplementCode}？`, '审批确认', { type: 'success' })
    await masterDataApi.approveSupplement(row.id, true, 'admin', '审批通过')
    ElMessage.success('审批通过')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('审批失败')
    }
  }
}

const reject = async (row) => {
  try {
    await ElMessageBox.confirm(`确认驳回补单 ${row.supplementCode}？`, '驳回确认', { type: 'warning' })
    await masterDataApi.approveSupplement(row.id, false, 'admin', '审批驳回')
    ElMessage.success('已驳回')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
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

.reason-box {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 50px;
  line-height: 1.6;
}
</style>
