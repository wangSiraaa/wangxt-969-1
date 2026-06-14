<template>
  <div class="page-container">
    <div class="page-header">
      <h2>需求详情</h2>
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>

    <el-card v-if="demand" class="detail-card">
      <el-descriptions title="需求基本信息" :column="3" border class="mb-20">
        <el-descriptions-item label="需求编号">{{ demand.demandCode }}</el-descriptions-item>
        <el-descriptions-item label="当前版本">v{{ demand.currentVersion || 1 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[demand.status]?.type || 'info'">{{ statusMap[demand.status]?.label || demand.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="老人">{{ demand.elderName }}</el-descriptions-item>
        <el-descriptions-item label="服务包">{{ demand.servicePackageName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务类型">{{ demand.serviceType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="护理等级">{{ demand.nursingLevelCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="riskLevelMap[demand.riskLevel]?.type || 'info'">
            {{ riskLevelMap[demand.riskLevel]?.label || demand.riskLevel }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交人">{{ demand.submittedBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ formatDate(demand.requestedDate) }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ demand.requestedStartTime || '-' }} ~ {{ demand.requestedEndTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(demand.submittedAt) }}</el-descriptions-item>
        <el-descriptions-item label="服务地址" :span="3">{{ demand.address }}</el-descriptions-item>
        <el-descriptions-item label="服务说明" :span="3">{{ demand.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="特殊要求" :span="3">{{ demand.specialRequirements || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ demand.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="activeTab" class="mt-20">
        <el-tab-pane label="版本历史" name="versions">
          <el-table :data="versions" stripe size="small">
            <el-table-column prop="version" label="版本" width="80" />
            <el-table-column prop="changeSummary" label="变更摘要" />
            <el-table-column prop="changedBy" label="操作人" width="120" />
            <el-table-column prop="createdAt" label="操作时间" width="170" :formatter="formatDateTimeCol" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="关联工单" name="workOrder" v-if="workOrder">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="工单编号">{{ workOrder.orderCode }}</el-descriptions-item>
            <el-descriptions-item label="护理员">{{ workOrder.nurseName }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusMap[workOrder.status]?.type || 'info'">
                {{ statusMap[workOrder.status]?.label || workOrder.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="派单时间">{{ formatDateTime(workOrder.dispatchedAt) }}</el-descriptions-item>
            <el-descriptions-item label="派单人">{{ workOrder.dispatchedBy }}</el-descriptions-item>
            <el-descriptions-item label="操作">
              <el-button type="primary" link @click="goToOrder">查看工单</el-button>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="审计日志" name="audit">
          <el-table :data="auditLogs" stripe size="small">
            <el-table-column prop="action" label="操作" width="160" />
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="createdAt" label="操作时间" width="170" :formatter="formatDateTimeCol" />
            <el-table-column prop="detail" label="详情" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-empty v-else description="加载中..." />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { demandApi, workOrderApi } from '@/api'
import { statusMap, riskLevelMap, formatDate, formatDateTime } from '@/utils'

const router = useRouter()
const route = useRoute()
const demand = ref(null)
const versions = ref([])
const auditLogs = ref([])
const workOrder = ref(null)
const activeTab = ref('versions')

const loadData = async () => {
  const id = route.params.id
  try {
    demand.value = await demandApi.get(id)
    const [v, a] = await Promise.all([
      demandApi.getVersions(id),
      demandApi.getAuditLogs(id)
    ])
    versions.value = v || []
    auditLogs.value = a || []
    const orders = await workOrderApi.list({ page: 0, size: 1 })
    const list = orders?.content || orders?.list || []
    if (list.length > 0 && list[0].demandId === Number(id)) {
      workOrder.value = list[0]
    }
  } catch (e) {
    console.error('加载详情失败', e)
  }
}

const formatDateTimeCol = (row) => formatDateTime(row.createdAt)

const goToOrder = () => {
  if (workOrder.value) {
    router.push(`/dispatch/order/${workOrder.value.id}`)
  }
}

const handleBack = () => {
  router.push('/demand/list')
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
.detail-card { margin-bottom: 16px; }
.mb-20 { margin-bottom: 20px; }
.mt-20 { margin-top: 20px; }
</style>
