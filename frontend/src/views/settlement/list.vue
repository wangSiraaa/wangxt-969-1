<template>
  <div class="page-container">
    <div class="page-header">
      <h2>结算列表</h2>
      <div class="header-actions">
        <el-select v-model="statusFilter" placeholder="全部状态" style="width: 150px; margin-right: 10px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="待结算" value="PENDING_SETTLEMENT" />
          <el-option label="已结算" value="SETTLED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="loadData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
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
        <el-table-column label="服务时长(分钟)" width="120">
          <template #default="{ row }">{{ row.serviceDurationMinutes || 0 }}</template>
        </el-table-column>
        <el-table-column label="基础金额" width="100">
          <template #default="{ row }">¥{{ row.baseAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column label="总金额" width="100">
          <template #default="{ row }">
            <span class="text-primary font-bold">¥{{ row.totalAmount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="医保报销" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.insuranceCovered" type="success" size="small">
              ¥{{ row.insuranceAmount || '0.00' }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="自付金额" width="100">
          <template #default="{ row }">¥{{ row.selfPayAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING_SETTLEMENT'" type="success" link @click="approveSettlement(row)">
              审批
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="mt-20"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="结算详情" width="700px">
      <div v-if="currentSettlement">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="结算编号">{{ currentSettlement.settlementCode }}</el-descriptions-item>
          <el-descriptions-item label="工单编号">{{ currentSettlement.orderCode }}</el-descriptions-item>
          <el-descriptions-item label="老人">{{ currentSettlement.elderName }}</el-descriptions-item>
          <el-descriptions-item label="护理员">{{ currentSettlement.nurseName }}</el-descriptions-item>
          <el-descriptions-item label="服务包">{{ currentSettlement.servicePackageName }}</el-descriptions-item>
          <el-descriptions-item label="服务日期">{{ currentSettlement.serviceDate }}</el-descriptions-item>
          <el-descriptions-item label="服务时长">{{ currentSettlement.serviceDurationMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentSettlement.status)" size="small">
              {{ getStatusLabel(currentSettlement.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>费用明细</el-divider>
        <el-table :data="feeDetail" size="small" border>
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
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { settlementApi } from '@/api'
import { settlementStatusMap } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const detailVisible = ref(false)
const currentSettlement = ref(null)

const getStatusType = (status) => settlementStatusMap[status]?.type || 'info'
const getStatusLabel = (status) => settlementStatusMap[status]?.label || status

const feeDetail = computed(() => {
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
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (statusFilter.value) {
      const data = await settlementApi.listByStatus(statusFilter.value)
      tableData.value = data || []
      total.value = data?.length || 0
    } else {
      const data = await settlementApi.list(params)
      tableData.value = data?.content || data?.list || []
      total.value = data?.totalElements || data?.total || 0
    }
  } catch (e) {
    console.error('加载结算列表失败', e)
    ElMessage.error('加载结算列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const data = await settlementApi.get(row.id)
    currentSettlement.value = data
    detailVisible.value = true
  } catch (e) {
    console.error('加载结算详情失败', e)
  }
}

const approveSettlement = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认审批通过结算单 ${row.settlementCode}？`,
      '审批确认',
      { type: 'success' }
    )
    await settlementApi.approve(row.id, 'admin', '审批通过')
    ElMessage.success('审批成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('审批失败', e)
      ElMessage.error('审批失败')
    }
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadData()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<script>
import { computed } from 'vue'
export default {
  computed: {
    feeDetail() {
      if (!this.currentSettlement) return []
      return [
        { name: '基础服务费', amount: this.currentSettlement.baseAmount || '0.00' },
        { name: '额外费用', amount: this.currentSettlement.extraAmount || '0.00' },
        { name: '减免金额', amount: '-' + (this.currentSettlement.deductAmount || '0.00') }
      ]
    }
  }
}
</script>

<style scoped>
.font-bold {
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
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
