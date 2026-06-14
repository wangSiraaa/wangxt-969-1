<template>
  <div class="page-container">
    <div class="page-header">
      <h2>提交服务需求</h2>
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>

    <el-card class="form-card">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="老人" prop="elderId">
          <el-select v-model="formData.elderId" placeholder="请选择老人" filterable style="width: 100%" @change="handleElderChange">
            <el-option v-for="e in elders" :key="e.id" :label="`${e.name} - ${e.elderCode}`" :value="e.id" />
          </el-select>
        </el-form-item>

        <el-row v-if="selectedElder" :gutter="20">
          <el-col :span="12">
            <el-descriptions :column="1" border size="small" class="mb-20">
              <el-descriptions-item label="护理等级">{{ selectedElder.nursingLevelName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="riskLevelMap[selectedElder.riskLevel]?.type || 'info'" size="small">
                  {{ riskLevelMap[selectedElder.riskLevel]?.label || selectedElder.riskLevel }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="服务状态">
                <el-tag :type="elderStatusMap[selectedElder.status]?.type || 'info'" size="small">
                  {{ elderStatusMap[selectedElder.status]?.label || selectedElder.status }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="紧急联系人">{{ selectedElder.emergencyContacts || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          <el-col :span="12">
            <el-alert v-if="selectedElder.status === 'PAUSED'" type="warning" show-icon class="mb-10">
              <template #title>该老人当前处于暂停服务状态，派单时将被拦截</template>
              {{ selectedElder.pauseReason || '暂停原因未填写' }}
              <span v-if="selectedElder.pauseEndTime">，暂停至：{{ formatDate(selectedElder.pauseEndTime) }}</span>
            </el-alert>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="地址">{{ selectedElder.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="健康状况">{{ selectedElder.healthStatus || '-' }}</el-descriptions-item>
              <el-descriptions-item label="过敏史">{{ selectedElder.allergies || '无' }}</el-descriptions-item>
              <el-descriptions-item label="医保报销">{{ selectedElder.insuranceCoverage ? selectedElder.insuranceCoverage + '%' : '无' }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <el-form-item label="服务包" prop="servicePackageId">
          <el-select v-model="formData.servicePackageId" placeholder="请选择服务包" filterable style="width: 100%" @change="handlePackageChange">
            <el-option v-for="p in servicePackages" :key="p.id" :label="`${p.name} - ${p.standardDurationMinutes}分钟 - ¥${p.basePrice}`" :value="p.id">
              <span style="float: left">{{ p.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ p.standardDurationMinutes }}分钟 | ¥{{ p.basePrice }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-row v-if="selectedPackage" :gutter="20" class="mb-20">
          <el-col :span="24">
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="服务类型">{{ selectedPackage.serviceType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="标准时长">{{ selectedPackage.standardDurationMinutes }}分钟</el-descriptions-item>
              <el-descriptions-item label="基础价格">¥{{ selectedPackage.basePrice }}</el-descriptions-item>
              <el-descriptions-item label="超时单价">¥{{ selectedPackage.hourlyRate }}/小时</el-descriptions-item>
              <el-descriptions-item label="要求资质">{{ selectedPackage.requiredQualifications || '-' }}</el-descriptions-item>
              <el-descriptions-item label="可医保报销">
                <el-tag :type="selectedPackage.insurable ? 'success' : 'info'" size="small">
                  {{ selectedPackage.insurable ? '是' : '否' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="预约日期" prop="requestedDate">
              <el-date-picker v-model="formData.requestedDate" type="date" placeholder="选择日期" style="width: 100%" :disabled-date="disabledDate" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开始时间" prop="requestedStartTime">
              <el-time-picker v-model="formData.requestedStartTime" placeholder="选择开始时间" format="HH:mm" value-format="HH:mm" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束时间" prop="requestedEndTime">
              <el-time-picker v-model="formData.requestedEndTime" placeholder="选择结束时间" format="HH:mm" value-format="HH:mm" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="服务地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="服务说明" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请描述服务需求、老人近况等" />
        </el-form-item>

        <el-form-item label="特殊要求" prop="specialRequirements">
          <el-input v-model="formData.specialRequirements" type="textarea" :rows="2" placeholder="饮食禁忌、注意事项等特殊要求" />
        </el-form-item>

        <el-form-item label="禁忌事项">
          <el-select v-model="formData.contraindicationIds" multiple placeholder="请选择该老人的禁忌事项" filterable style="width: 100%">
            <el-option v-for="c in contraindications" :key="c.id" :label="`${c.name} - ${c.severity}`" :value="String(c.id)" />
          </el-select>
        </el-form-item>

        <el-form-item label="提交人" prop="submittedBy">
          <el-input v-model="formData.submittedBy" placeholder="请输入提交人姓名" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            <el-icon><Check /></el-icon> 提交需求
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { demandApi, masterDataApi } from '@/api'
import { riskLevelMap, elderStatusMap, formatDate } from '@/utils'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const elders = ref([])
const servicePackages = ref([])
const contraindications = ref([])
const selectedElder = ref(null)
const selectedPackage = ref(null)

const formData = reactive({
  elderId: null,
  servicePackageId: null,
  requestedDate: null,
  requestedStartTime: null,
  requestedEndTime: null,
  address: '',
  description: '',
  specialRequirements: '',
  contraindicationIds: [],
  submittedBy: ''
})

const rules = {
  elderId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  servicePackageId: [{ required: true, message: '请选择服务包', trigger: 'change' }],
  requestedDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  requestedStartTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  requestedEndTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  address: [{ required: true, message: '请输入服务地址', trigger: 'blur' }],
  submittedBy: [{ required: true, message: '请输入提交人', trigger: 'blur' }]
}

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const loadMasterData = async () => {
  try {
    const [e, p, c] = await Promise.all([
      masterDataApi.listActiveElders(),
      masterDataApi.listServicePackages(),
      masterDataApi.listContraindications()
    ])
    elders.value = e || []
    servicePackages.value = p || []
    contraindications.value = c || []
  } catch (e) {
    ElMessage.error('加载基础数据失败')
  }
}

const handleElderChange = async (elderId) => {
  selectedElder.value = null
  if (elderId) {
    try {
      selectedElder.value = await masterDataApi.getElder(elderId)
      if (selectedElder.value && !formData.address) {
        formData.address = selectedElder.value.address || ''
      }
    } catch (e) {
      ElMessage.error('加载老人详情失败')
    }
  }
}

const handlePackageChange = (packageId) => {
  selectedPackage.value = null
  if (packageId) {
    selectedPackage.value = servicePackages.value.find(p => p.id === packageId)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    await demandApi.create(formData)
    ElMessage.success('需求提交成功，等待调度员派单')
    setTimeout(() => router.push('/demand/list'), 1000)
  } catch (e) {
    if (e !== false) {
      ElMessage.error(e?.response?.data?.message || e?.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  formRef.value?.resetFields()
  selectedElder.value = null
  selectedPackage.value = null
}

const handleBack = () => {
  router.push('/demand/list')
}

onMounted(() => {
  loadMasterData()
})
</script>

<style scoped>
.page-container { padding: 16px; }
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}
.form-card { margin-bottom: 16px; }
.mb-20 { margin-bottom: 20px; }
.mb-10 { margin-bottom: 10px; }
</style>
