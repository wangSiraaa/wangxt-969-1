<template>
  <div class="page-container">
    <div class="page-header">
      <h2>家属确认历史</h2>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" size="default">
        <el-form-item label="工单编号">
          <el-input v-model="filterForm.orderCode" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="老人">
          <el-input v-model="filterForm.elderName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="确认结果">
          <el-select v-model="filterForm.confirmed" placeholder="全部" clearable>
            <el-option label="已确认" :value="true" />
            <el-option label="已拒绝" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="filteredData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderCode" label="工单编号" width="160" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="nurseName" label="护理员" width="100" />
        <el-table-column prop="servicePackageName" label="服务包" width="140" />
        <el-table-column label="服务日期" width="120">
          <template #default="{ row }">{{ formatDate(row.scheduledDate) }}</template>
        </el-table-column>
        <el-table-column label="服务时长" width="110">
          <template #default="{ row }">
            <span v-if="row.serviceDurationMinutes">{{ row.serviceDurationMinutes }}分钟</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="确认结果" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.confirmed" type="success" size="small">已确认</el-tag>
            <el-tag v-else type="danger" size="small">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="确认人" width="100">
          <template #default="{ row }">{{ row.confirmedBy || '-' }}</template>
        </el-table-column>
        <el-table-column label="确认时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.confirmedAt) }}</template>
        </el-table-column>
        <el-table-column label="确认备注" show-overflow-tooltip>
          <template #default="{ row }">{{ row.confirmationRemark || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { workOrderApi } from '@/api'
import { formatDate, formatDateTime } from '@/utils'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const allData = ref([])
const filterForm = reactive({
  orderCode: '',
  elderName: '',
  confirmed: null
})

const filteredData = computed(() => {
  return allData.value.filter(item => {
    if (filterForm.orderCode && !item.orderCode?.includes(filterForm.orderCode)) return false
    if (filterForm.elderName && !item.elderName?.includes(filterForm.elderName)) return false
    if (filterForm.confirmed !== null && item.confirmed !== filterForm.confirmed) return false
    return true
  })
})

const loadData = async () => {
  loading.value = true
  try {
    const confirmed = await workOrderApi.listByStatus('FAMILY_CONFIRMED')
    const rejected = await workOrderApi.listByStatus('FAMILY_REJECTED')
    allData.value = [...(confirmed || []), ...(rejected || [])]
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {}

const handleReset = () => {
  filterForm.orderCode = ''
  filterForm.elderName = ''
  filterForm.confirmed = null
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
.filter-card { margin-bottom: 16px; }
.table-card { margin-bottom: 16px; }
</style>
