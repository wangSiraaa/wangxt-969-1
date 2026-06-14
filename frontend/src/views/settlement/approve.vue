<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待审批结算</h2>
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="settlementCode" label="结算编号" width="180" />
        <el-table-column prop="orderCode" label="工单编号" width="180" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="nurseName" label="护理员" width="100" />
        <el-table-column prop="servicePackageName" label="服务包" show-overflow-tooltip />
        <el-table-column label="服务日期" width="120">
          <template #default="{ row }">{{ row.serviceDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="总金额" width="100">
          <template #default="{ row }">
            <span class="text-primary font-bold">¥{{ row.totalAmount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="自付金额" width="100">
          <template #default="{ row }">¥{{ row.selfPayAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ row.createdAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button type="success" link @click="approveSettlement(row)">通过</el-button>
            <el-button type="danger" link @click="rejectSettlement(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && tableData.length === 0" description="暂无待审批结算单" />
    </el-card>

    <el-dialog v-model="detailVisible" title="结算审批" width="700px">
      <div v-if="currentSettlement">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="结算编号">{{ currentSettlement.settlementCode }}</el-descriptions-item>
          <el-descriptions-item label="工单编号">{{ currentSettlement.orderCode }}</el-descriptions-item>
          <el-descriptions-item label="老人">{{ currentSettlement.elderName }}</el-descriptions-item>
          <el-descriptions-item label="护理员">{{ currentSettlement.nurseName }}</el-descriptions-item>
          <el-descriptions-item label="服务包">{{ currentSettlement.servicePackageName }}</el-descriptions-item>
          <el-descriptions-item label="服务日期">{{ currentSettlement.serviceDate }}</el-descriptions-item>
          <el-descriptions-item label="服务时长">{{ currentSettlement.serviceDurationMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="家属确认">
            <el-tag :type="currentSettlement.familyConfirmed ? 'success' : 'danger'" size="small">
              {{ currentSettlement.familyConfirmed ? '已确认' : '未确认' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>费用明细</el-divider>
        <el-table :data="feeDetailList" size="small" border>
          <el-table-column prop="name" label="项目" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
        </el-table>

        <el-divider />
        <div class="settlement-summary">
          <div class="summary-item">
            <span class="label">总金额：</span>
            <span class="value text-primary">¥{{ currentSettlement.totalAmount || '0.00' }}</span>
          </div>
          <div class="summary-item">
            <span class="label">医保报销：</span>
            <span class="value text-success">-¥{{ currentSettlement.insuranceAmount || '0.00' }}</span>
          </div>
          <div class="summary-item">
            <span class="label">自付金额：</span>
            <span class="value text-danger font-bold">¥{{ currentSettlement.selfPayAmount || '0.00' }}</span>
          </div>
        </div>

        <el-divider />
        <el-form label-width="80px">
          <el-form-item label="审批意见">
            <el-input v-model="approvalRemark" type="textarea" :rows="3" placeholder="请输入审批意见" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">驳回</el-button>
        <el-button type="primary" @click="handleApprove">通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { settlementApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentSettlement = ref(null)
const approvalRemark = ref('')

const feeDetailList = computed(() => {
  if (!currentSettlement.value) return []
  return [
    { name: '基础服务费', amount: currentSettlement.value.baseAmount || '0.00' },
    { name: '额外费用', amount: currentSettlement.value.extraAmount || '0.00' },
    { name: '减免金额', amount: '-' + (currentSettlement.value.deductAmount || '0.00') }
  ]
})

const loadData = async () => {
  loading.value = true
  try {
    const data = await settlementApi.list({ page: 0, size: 100, status: 'PENDING_SETTLEMENT' })
    tableData.value = data?.content || data?.list || []
    if (!data?.content && !data?.list) {
      tableData.value = data?.filter(d => d.status === 'PENDING_SETTLEMENT') || []
    }
  } catch (e) {
    console.error('加载待审批结算失败', e)
    ElMessage.error('加载待审批结算失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const data = await settlementApi.get(row.id)
    currentSettlement.value = data
    approvalRemark.value = ''
    detailVisible.value = true
  } catch (e) {
    console.error('加载结算详情失败', e)
  }
}

const approveSettlement = (row) => {
  viewDetail(row)
}

const rejectSettlement = (row) => {
  viewDetail(row)
}

const handleApprove = async () => {
  if (!currentSettlement.value) return
  try {
    await ElMessageBox.confirm(
      `确认审批通过结算单 ${currentSettlement.value.settlementCode}？`,
      '审批确认',
      { type: 'success' }
    )
    await settlementApi.approve(
      currentSettlement.value.id,
      'admin',
      approvalRemark.value || '审批通过'
    )
    ElMessage.success('审批通过')
    detailVisible.value = false
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('审批失败', e)
      ElMessage.error('审批失败')
    }
  }
}

const handleReject = async () => {
  if (!currentSettlement.value) return
  try {
    await ElMessageBox.confirm(
      `确认驳回结算单 ${currentSettlement.value.settlementCode}？`,
      '驳回确认',
      { type: 'warning' }
    )
    ElMessage.success('已驳回')
    detailVisible.value = false
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('驳回失败', e)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.font-bold {
  font-weight: 600;
}

.settlement-summary {
  display: flex;
  justify-content: flex-end;
  gap: 30px;
  padding: 10px 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-item .label {
  color: #606266;
  font-size: 14px;
}

.summary-item .value {
  font-size: 16px;
  font-weight: 600;
}
</style>
