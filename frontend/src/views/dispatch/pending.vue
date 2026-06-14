<template>
  <div class="page-container">
    <div class="page-header">
      <h2>待派单需求</h2>
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon> 刷新
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="demandCode" label="需求编号" width="180" />
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column label="护理等级" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.nursingLevelCode || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="servicePackageName" label="服务包" />
        <el-table-column label="预约时间" width="200">
          <template #default="{ row }">
            {{ row.requestedDate || '-' }}
            {{ row.requestedStartTime || '-' }}
            ~ {{ row.requestedEndTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="地址" show-overflow-tooltip>
          <template #default="{ row }">{{ row.address || '-' }}</template>
        </el-table-column>
        <el-table-column label="风险等级" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.riskLevel"
              :type="riskMap[row.riskLevel]?.type || 'info'" size="small">
              {{ riskMap[row.riskLevel]?.label || row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDispatch(row)">派单</el-button>
            <el-button link @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dispatchVisible" title="调度派单" width="900px" destroy-on-close>
      <div v-if="currentDemand" class="dispatch-info">
        <el-descriptions :column="2" border size="small" class="mb-20">
          <el-descriptions-item label="需求编号">{{ currentDemand.demandCode }}</el-descriptions-item>
          <el-descriptions-item label="老人">{{ currentDemand.elderName }}</el-descriptions-item>
          <el-descriptions-item label="服务包">{{ currentDemand.servicePackageName }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ currentDemand.requestedDate }} {{ currentDemand.requestedStartTime }}~{{ currentDemand.requestedEndTime }}
          </el-descriptions-item>
          <el-descriptions-item label="服务地址">{{ currentDemand.address }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag v-if="currentDemand.riskLevel"
              :type="riskMap[currentDemand.riskLevel]?.type || 'info'">
              {{ riskMap[currentDemand.riskLevel]?.label || currentDemand.riskLevel }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-alert v-if="validation && !validation.valid"
          :title="validation.errorMessages?.join('；') || '校验未通过'"
          type="error" :closable="false" class="mb-20" show-icon />

        <h4>选择护理员（按匹配度排序，绿色为符合条件）</h4>
        <el-table :data="candidates" v-loading="candidateLoading" size="small" border>
          <el-table-column label="选择" width="60">
            <template #default="{ row }">
              <el-radio v-model="selectedNurseId" :label="row.nurseId" :disabled="!row.qualified" />
            </template>
          </el-table-column>
          <el-table-column prop="nurseCode" label="工号" width="100" />
          <el-table-column prop="nurseName" label="姓名" width="100" />
          <el-table-column label="匹配" width="80">
            <template #default="{ row }">
              <el-tag :type="row.qualified ? 'success' : 'danger'" size="small">
                {{ row.qualified ? '符合' : '不符合' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="qualificationSummary" label="资质" show-overflow-tooltip>
            <template #default="{ row }">{{ row.qualificationSummary || '-' }}</template>
          </el-table-column>
          <el-table-column prop="travelTimeMinutes" label="路程(分钟)" width="100" />
          <el-table-column prop="distanceKm" label="距离(KM)" width="90" />
          <el-table-column prop="dailyRemainingSlots" label="当日余单" width="90" />
          <el-table-column label="完成单量" width="90">
            <template #default="{ row }">{{ row.completedOrdersCount || 0 }}</template>
          </el-table-column>
          <el-table-column label="评分" width="80">
            <template #default="{ row }">{{ row.averageRating || '-' }}</template>
          </el-table-column>
          <el-table-column label="不匹配原因" show-overflow-tooltip>
            <template #default="{ row }">
              <span style="color:#f56c6c">{{ row.disqualifyReason || '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-input v-model="dispatchRemark" placeholder="派单备注/改派原因" class="mb-10" />
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!selectedNurseId" :loading="dispatching" @click="submitDispatch">
          确认派单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { demandApi, workOrderApi } from '@/api'
import { riskLevelMap } from '@/utils'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const riskMap = riskLevelMap
const dispatchVisible = ref(false)
const currentDemand = ref(null)
const candidates = ref([])
const candidateLoading = ref(false)
const selectedNurseId = ref(null)
const validation = ref(null)
const dispatchRemark = ref('')
const dispatching = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const data = await demandApi.pendingDispatch()
    tableData.value = data || []
  } catch (e) {
    ElMessage.error('加载待派单列表失败')
  } finally {
    loading.value = false
  }
}

const openDispatch = async (row) => {
  currentDemand.value = row
  dispatchVisible.value = true
  selectedNurseId.value = null
  validation.value = null
  dispatchRemark.value = ''
  candidateLoading.value = true
  try {
    candidates.value = await workOrderApi.getCandidates(row.id)
  } catch (e) {
    ElMessage.error('加载护理员候选人失败')
  } finally {
    candidateLoading.value = false
  }
}

const viewDetail = (row) => {
  router.push(`/demand/detail/${row.id}`)
}

const submitDispatch = async () => {
  if (!selectedNurseId.value) return
  dispatching.value = true
  try {
    await workOrderApi.dispatch({
      demandId: currentDemand.value.id,
      nurseId: selectedNurseId.value,
      remark: dispatchRemark.value,
      dispatchedBy: '调度员'
    })
    ElMessage.success('派单成功')
    dispatchVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '派单失败')
  } finally {
    dispatching.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.mb-20 { margin-bottom: 20px; }
.mb-10 { margin-bottom: 10px; }
</style>
