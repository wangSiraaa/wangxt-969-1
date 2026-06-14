<template>
  <div class="page-container">
    <div class="page-header">
      <h2>服务需求管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon> 新建需求
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" size="default">
        <el-form-item label="需求编号">
          <el-input v-model="filterForm.demandCode" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="老人">
          <el-input v-model="filterForm.elderName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option label="待派单" value="PENDING_DISPATCH" />
            <el-option label="已派发" value="DISPATCHED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="服务中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
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
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="demandCode" label="需求编号" width="180" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="elderIdCard" label="身份证号" width="180" />
        <el-table-column prop="nursingLevelName" label="护理等级" width="120" />
        <el-table-column prop="servicePackageName" label="服务包" />
        <el-table-column prop="scheduledDate" label="预约日期" width="120">
          <template #default="{ row }">
            {{ row.scheduledDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="100" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'DRAFT' || row.status === 'PENDING_DISPATCH'" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'PENDING_DISPATCH'" type="danger" link @click="handleCancel(row)">取消</el-button>
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
import { demandApi } from '@/api'
import { statusMap } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const filterForm = reactive({
  demandCode: '',
  elderName: '',
  status: ''
})
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getStatusType = (status) => statusMap[status]?.type || 'info'
const getStatusLabel = (status) => statusMap[status]?.label || status

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...filterForm
    }
    Object.keys(params).forEach(k => {
      if (!params[k]) delete params[k]
    })
    const data = await demandApi.list(params)
    tableData.value = data?.content || data?.list || data || []
    pagination.total = data?.totalElements || data?.total || 0
  } catch (e) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  filterForm.demandCode = ''
  filterForm.elderName = ''
  filterForm.status = ''
  pagination.page = 1
  loadData()
}

const handleCreate = () => {
  router.push('/demand/create')
}

const handleView = (row) => {
  router.push(`/demand/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/demand/edit/${row.id}`)
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该需求吗？', '提示', { type: 'warning' })
    await demandApi.cancel(row.id, '用户取消', '系统')
    ElMessage.success('取消成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const handleSizeChange = (val) => {
  pagination.size = val
  pagination.page = 1
  loadData()
}

const handlePageChange = (val) => {
  pagination.page = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
