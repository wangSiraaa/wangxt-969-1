<template>
  <div class="page-container">
    <div class="page-header">
      <h2>工单管理</h2>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" size="default">
        <el-form-item label="工单编号">
          <el-input v-model="filterForm.orderCode" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option v-for="(v, k) in statusMap" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="老人">
          <el-input v-model="filterForm.elderName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="护理员">
          <el-input v-model="filterForm.nurseName" placeholder="请输入" clearable />
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
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderCode" label="工单编号" width="180" />
        <el-table-column prop="demandCode" label="需求编号" width="180" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="nurseName" label="护理员" width="100" />
        <el-table-column prop="servicePackageName" label="服务包" />
        <el-table-column label="计划时间" width="220">
          <template #default="{ row }">
            {{ row.scheduledDate || '-' }}<br />
            {{ row.scheduledStartTime || '-' }} ~ {{ row.scheduledEndTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button v-if="canCancel(row.status)" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { workOrderApi } from '@/api'
import { statusMap } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const filterForm = reactive({
  orderCode: '', status: '', elderName: '', nurseName: ''
})
const pagination = reactive({ page: 1, size: 10, total: 0 })

const canCancel = (status) => ['DISPATCHED', 'NURSE_ACCEPTED'].includes(status)

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      status: filterForm.status || undefined
    }
    const data = await workOrderApi.list(params)
    tableData.value = data?.content || data?.list || data || []
    pagination.total = data?.totalElements || data?.total || 0
  } catch (e) {
    ElMessage.error('加载工单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; loadData() }
const handleReset = () => {
  Object.assign(filterForm, { orderCode: '', status: '', elderName: '', nurseName: '' })
  pagination.page = 1; loadData()
}
const viewDetail = (row) => router.push(`/dispatch/order/${row.id}`)

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该工单吗？', '提示', { type: 'warning' })
    await workOrderApi.cancel(row.id, '调度取消', '调度员')
    ElMessage.success('取消成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '取消失败')
  }
}

const handleSizeChange = (v) => { pagination.size = v; pagination.page = 1; loadData() }
const handlePageChange = (v) => { pagination.page = v; loadData() }

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header { margin-bottom: 16px; }
.filter-card, .table-card { margin-bottom: 16px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
