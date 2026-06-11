<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">调度派单中心</div>
    </div>

    <el-row :gutter="16">
      <el-col :span="14">
        <div class="table-container" style="min-height: 600px">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
            待派单需求列表
          </div>
          <el-table :data="pendingDemands" stripe border highlight-current-row @current-change="handleDemandSelect">
            <el-table-column prop="id" label="编号" width="60" />
            <el-table-column prop="elderName" label="老人" width="90" />
            <el-table-column prop="serviceType" label="服务类型" width="110" />
            <el-table-column prop="serviceContent" label="服务内容" show-overflow-tooltip />
            <el-table-column prop="requiredQualifications" label="所需资质" show-overflow-tooltip />
            <el-table-column label="期望时间" width="160">
              <template #default="{ row }">{{ formatDate(row.expectedTime) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="table-container" style="min-height: 600px">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
            派单操作
          </div>

          <div v-if="!selectedDemand" style="color: #909399; text-align: center; padding: 80px 0;">
            <el-icon style="font-size: 48px; color: #dcdfe6;"><Document /></el-icon>
            <div style="margin-top: 12px;">请在左侧选择一条待派单需求</div>
          </div>

          <div v-else>
            <el-descriptions :column="1" border size="small" style="margin-bottom: 20px;">
              <el-descriptions-item label="需求编号">{{ selectedDemand.id }}</el-descriptions-item>
              <el-descriptions-item label="老人">{{ selectedDemand.elderName }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ selectedDemand.applicant }}（{{ selectedDemand.relation }}）</el-descriptions-item>
              <el-descriptions-item label="服务类型">{{ selectedDemand.serviceType }}</el-descriptions-item>
              <el-descriptions-item label="服务内容">{{ selectedDemand.serviceContent }}</el-descriptions-item>
              <el-descriptions-item label="所需资质">
                <el-tag v-if="selectedDemand.requiredQualifications" type="warning">
                  {{ selectedDemand.requiredQualifications }}
                </el-tag>
                <span v-else style="color: #909399">无特殊要求</span>
              </el-descriptions-item>
              <el-descriptions-item label="期望时间">{{ formatDate(selectedDemand.expectedTime) }}</el-descriptions-item>
              <el-descriptions-item label="特殊要求">
                <span v-if="selectedDemand.specialRequirement">{{ selectedDemand.specialRequirement }}</span>
                <span v-else style="color: #909399">无</span>
              </el-descriptions-item>
            </el-descriptions>

            <el-divider>选择护理员</el-divider>

            <div style="margin-bottom: 12px">
              <el-checkbox v-model="showMatchedOnly" @change="loadCaregivers">
                仅显示资质匹配的护理员
              </el-checkbox>
            </div>

            <el-table
              :data="caregivers"
              stripe border
              height="260"
              highlight-current-row
              @current-change="handleCaregiverSelect"
            >
              <el-table-column prop="name" label="姓名" width="90" />
              <el-table-column prop="skillLevel" label="等级" width="60" />
              <el-table-column prop="qualifications" label="资质" show-overflow-tooltip />
              <el-table-column label="状态" width="60">
                <template #default="{ row }">
                  <el-tag v-if="row.active" type="success" size="small">在职</el-tag>
                  <el-tag v-else type="danger" size="small">停用</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="匹配" width="60">
                <template #default="{ row }">
                  <el-tag v-if="isMatched(row)" type="success" size="small">匹配</el-tag>
                  <el-tag v-else type="info" size="small">待检</el-tag>
                </template>
              </el-table-column>
            </el-table>

            <el-divider />

            <el-form label-width="80px" style="margin-bottom: 16px">
              <el-form-item label="调度员">
                <el-input v-model="dispatchForm.dispatcher" placeholder="调度员姓名" />
              </el-form-item>
              <el-form-item label="派单备注">
                <el-input v-model="dispatchForm.dispatchRemark" type="textarea" :rows="2" />
              </el-form-item>
            </el-form>

            <el-button
              type="primary"
              size="large"
              style="width: 100%"
              :disabled="!selectedCaregiver"
              @click="handleDispatch"
            >
              <el-icon><Connection /></el-icon>
              <span>确认派单给 {{ selectedCaregiver?.name || '...' }}</span>
            </el-button>

            <div v-if="!selectedCaregiver" style="color: #909399; font-size: 12px; margin-top: 8px; text-align: center;">
              请先选择护理员
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as api from '@/api'
import dayjs from 'dayjs'

const route = useRoute()
const pendingDemands = ref([])
const allCaregivers = ref([])
const caregivers = ref([])
const selectedDemand = ref(null)
const selectedCaregiver = ref(null)
const showMatchedOnly = ref(true)
const dispatchForm = reactive({ dispatcher: '', dispatchRemark: '' })

const formatDate = (d) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'

const isMatched = (cg) => {
  if (!selectedDemand.value?.requiredQualifications) return true
  const required = selectedDemand.value.requiredQualifications.split(/[,，、;；]/).map(s => s.trim()).filter(Boolean)
  if (required.length === 0) return true
  const all = (cg.qualifications || '') + ',' + (cg.serviceTypes || '')
  return required.every(r => all.includes(r))
}

const matchedCaregivers = computed(() =>
  allCaregivers.value.filter(cg => cg.active && isMatched(cg))
)

const handleDemandSelect = (row) => {
  selectedDemand.value = row
  selectedCaregiver.value = null
  loadCaregivers()
}

const handleCaregiverSelect = (row) => {
  selectedCaregiver.value = row
}

const loadDemands = async () => {
  pendingDemands.value = await api.getDemandsByStatus('PENDING_DISPATCH')
  if (route.query.demandId) {
    const found = pendingDemands.value.find(d => d.id === Number(route.query.demandId))
    if (found) {
      handleDemandSelect(found)
    }
  } else if (pendingDemands.value.length > 0) {
    handleDemandSelect(pendingDemands.value[0])
  }
}

const loadCaregivers = async () => {
  allCaregivers.value = await api.getActiveCaregivers()
  caregivers.value = showMatchedOnly.value ? matchedCaregivers.value : allCaregivers.value.filter(c => c.active)
}

const handleDispatch = async () => {
  if (!selectedDemand.value || !selectedCaregiver.value) return

  if (!isMatched(selectedCaregiver.value)) {
    ElMessage.error('该护理员资质不匹配，不能派单')
    return
  }

  try {
    await api.dispatchWorkOrder({
      demandId: selectedDemand.value.id,
      caregiverId: selectedCaregiver.value.id,
      dispatcher: dispatchForm.dispatcher || '系统调度员',
      dispatchRemark: dispatchForm.dispatchRemark
    })
    ElMessage.success('派单成功')
    selectedDemand.value = null
    selectedCaregiver.value = null
    dispatchForm.dispatcher = ''
    dispatchForm.dispatchRemark = ''
    loadDemands()
  } catch (_) {}
}

watch(showMatchedOnly, () => {
  caregivers.value = showMatchedOnly.value ? matchedCaregivers.value : allCaregivers.value.filter(c => c.active)
})

onMounted(() => {
  loadDemands()
  loadCaregivers()
})
</script>
