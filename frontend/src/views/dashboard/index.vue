<template>
  <div class="dashboard">
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409eff">
                <el-icon :size="24"><EditPen /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalDemands || 0 }}</div>
                <div class="stat-label">服务需求</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #e6a23c">
                <el-icon :size="24"><Promotion /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.pendingDispatch || 0 }}</div>
                <div class="stat-label">待派单</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67c23a">
                <el-icon :size="24"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.todaySettlement || '0.00' }}</div>
                <div class="stat-label">今日结算(元)</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover" @click="$router.push('/abnormal/list')">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f56c6c">
                <el-icon :size="24"><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number text-danger">{{ stats.pendingAbnormals || 0 }}</div>
                <div class="stat-label">待处理异常</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span class="card-title">工单状态分布</span>
              <el-button type="primary" link @click="$router.push('/dispatch/orders')">查看全部</el-button>
            </div>
          </template>
          <div ref="orderStatusChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span class="card-title">异常类型分布</span>
              <el-button type="primary" link @click="$router.push('/abnormal/list')">查看全部</el-button>
            </div>
          </template>
          <div ref="abnormalChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span class="card-title">最近需求</span>
              <el-button type="primary" link @click="$router.push('/demand/list')">更多</el-button>
            </div>
          </template>
          <el-table :data="recentDemands" size="small" style="width: 100%">
            <el-table-column prop="demandCode" label="需求编号" width="160" />
            <el-table-column prop="elderName" label="老人" width="100" />
            <el-table-column prop="servicePackageName" label="服务" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="flex-between">
              <span class="card-title">最近异常</span>
              <el-button type="danger" link @click="handleDetectAbnormals">
                <el-icon><Refresh /></el-icon> 检测异常
              </el-button>
            </div>
          </template>
          <el-table :data="recentAbnormals" size="small" style="width: 100%">
            <el-table-column prop="eventCode" label="异常编号" width="160" />
            <el-table-column prop="abnormalType" label="类型" width="140">
              <template #default="{ row }">
                <el-tag :type="getAbnormalType(row.abnormalType)?.type || 'info'" size="small">
                  {{ getAbnormalType(row.abnormalType)?.label || row.abnormalType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="severity" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="row.severity === 'CRITICAL' ? 'danger' : row.severity === 'HIGH' ? 'warning' : 'info'" size="small">
                  {{ row.severity }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">快速入口</span>
          </template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/demand/create')">
              <el-icon :size="32" color="#409eff"><Edit /></el-icon>
              <span>提交需求</span>
            </div>
            <div class="action-item" @click="$router.push('/dispatch/pending')">
              <el-icon :size="32" color="#e6a23c"><Promotion /></el-icon>
              <span>调度派单</span>
            </div>
            <div class="action-item" @click="$router.push('/nurse/checkin')">
              <el-icon :size="32" color="#67c23a"><Location /></el-icon>
              <span>签到签退</span>
            </div>
            <div class="action-item" @click="$router.push('/family/confirm')">
              <el-icon :size="32" color="#909399"><UserFilled /></el-icon>
              <span>家属确认</span>
            </div>
            <div class="action-item" @click="$router.push('/settlement/approve')">
              <el-icon :size="32" color="#67c23a"><Money /></el-icon>
              <span>结算审批</span>
            </div>
            <div class="action-item" @click="$router.push('/abnormal/list')">
              <el-icon :size="32" color="#f56c6c"><Warning /></el-icon>
              <span>异常处置</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { demandApi, abnormalApi, dashboardApi, workOrderApi } from '@/api'
import { statusMap, abnormalTypeMap } from '@/utils'
import { ElMessage } from 'element-plus'

const router = useRouter()
const stats = ref({})
const recentDemands = ref([])
const recentAbnormals = ref([])
const orderStatusChart = ref(null)
const abnormalChart = ref(null)

const getStatusType = (status) => statusMap[status]?.type || 'info'
const getStatusLabel = (status) => statusMap[status]?.label || status
const getAbnormalType = (type) => abnormalTypeMap[type]

const loadData = async () => {
  try {
    const summary = await dashboardApi.getSummary()
    stats.value = {
      totalDemands: summary.totalDemands || 0,
      pendingDispatch: summary.pendingDispatch || 0,
      pendingAbnormals: summary.pendingAbnormals || 0,
      todaySettlement: summary.statistics?.todaySettlementAmount || '0.00'
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }

  try {
    const demands = await demandApi.list({ page: 0, size: 5 })
    recentDemands.value = demands?.list || demands?.content || []
  } catch (e) {
    console.error('加载需求列表失败', e)
  }

  try {
    const abnormals = await abnormalApi.pending()
    recentAbnormals.value = (abnormals || []).slice(0, 5)
  } catch (e) {
    console.error('加载异常列表失败', e)
  }
}

const initCharts = () => {
  nextTick(() => {
    if (orderStatusChart.value) {
      const chart = echarts.init(orderStatusChart.value)
      chart.setOption({
        tooltip: { trigger: 'item' },
        legend: { bottom: '5%', left: 'center' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          emphasis: {
            label: { show: true, fontSize: 16, fontWeight: 'bold' }
          },
          data: [
            { value: 12, name: '待派单', itemStyle: { color: '#e6a23c' } },
            { value: 8, name: '已派发', itemStyle: { color: '#409eff' } },
            { value: 15, name: '服务中', itemStyle: { color: '#909399' } },
            { value: 25, name: '已完成', itemStyle: { color: '#67c23a' } },
            { value: 3, name: '异常', itemStyle: { color: '#f56c6c' } }
          ]
        }]
      })
    }

    if (abnormalChart.value) {
      const chart2 = echarts.init(abnormalChart.value)
      chart2.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: ['资质不匹配', '暂停服务', '服务超时', '未签退', '家属拒确认', '时间冲突'],
          axisLabel: { rotate: 20 }
        },
        yAxis: { type: 'value' },
        series: [{
          type: 'bar',
          data: [2, 1, 3, 1, 2, 1],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f56c6c' },
              { offset: 1, color: '#e6a23c' }
            ])
          },
          barWidth: '50%'
        }]
      })
    }
  })
}

const handleDetectAbnormals = async () => {
  try {
    await dashboardApi.detectAbnormals()
    ElMessage.success('异常检测已执行')
    loadData()
  } catch (e) {
    ElMessage.error('异常检测失败')
  }
}

onMounted(() => {
  loadData()
  initCharts()
})
</script>

<style scoped>
.dashboard {
  padding: 16px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 280px;
}

.mt-20 {
  margin-top: 20px;
}

.quick-actions {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 30px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
}

.action-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.action-item span {
  font-size: 14px;
  color: #606266;
}
</style>
