<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑服务需求' : '新建服务需求' }}</h2>
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
    </div>

    <el-card>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-divider content-position="left">老人信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="选择老人" prop="elderId">
              <el-select v-model="form.elderId" placeholder="请选择老人" filterable @change="onElderChange">
                <el-option
                  v-for="item in elderList"
                  :key="item.id"
                  :label="`${item.name} (${item.idCard})`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="护理等级">
              <el-input v-model="elderInfo.nursingLevelName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="风险等级">
              <el-tag :type="elderInfo.riskLevel === 'HIGH' ? 'danger' : elderInfo.riskLevel === 'MEDIUM' ? 'warning' : 'info'">
                {{ elderInfo.riskLevel || '-' }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="form.elderId">
          <el-col :span="8">
            <el-form-item label="联系电话">
              <el-input v-model="elderInfo.phone" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="居住地址">
              <el-input v-model="elderInfo.address" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">服务信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="服务包" prop="servicePackageId">
              <el-select v-model="form.servicePackageId" placeholder="请选择服务包" @change="onPackageChange">
                <el-option
                  v-for="item in packageList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="服务时长">
              <el-input v-model="packageInfo.serviceDuration" disabled>
                <template #append>小时</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="服务价格">
              <el-input v-model="packageInfo.price" disabled>
                <template #append>元</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="预约日期" prop="scheduledDate">
              <el-date-picker
                v-model="form.scheduledDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="form.startTime"
                placeholder="选择时间"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束时间">
              <el-input v-model="form.endTime" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="服务备注">
              <el-input
                v-model="form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入服务备注或特殊需求"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="elderInfo.contraindications && elderInfo.contraindications.length > 0">
          <el-col :span="24">
            <el-form-item label="禁忌事项">
              <div class="contraindications-box">
                <el-tag v-for="item in elderInfo.contraindications" :key="item" type="danger" effect="light">
                  {{ item }}
                </el-tag>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">紧急联系人</el-divider>
        <el-table :data="contactList" border size="small" style="margin-bottom: 20px; width: 100%">
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="relationship" label="关系" width="100" />
          <el-table-column prop="phone" label="联系电话" width="150" />
          <el-table-column prop="isPrimary" label="是否首选" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isPrimary" type="success" size="small">是</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            <el-icon><Check /></el-icon> 提交需求
          </el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { demandApi, masterDataApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const submitting = ref(false)
const elderList = ref([])
const packageList = ref([])
const elderInfo = reactive({
  nursingLevelName: '',
  riskLevel: '',
  phone: '',
  address: '',
  contraindications: []
})
const packageInfo = reactive({
  serviceDuration: '',
  price: ''
})
const contactList = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  elderId: null,
  servicePackageId: null,
  scheduledDate: '',
  startTime: '',
  endTime: '',
  remark: '',
  submittedBy: '系统用户'
})

const rules = {
  elderId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  servicePackageId: [{ required: true, message: '请选择服务包', trigger: 'change' }],
  scheduledDate: [{ required: true, message: '请选择预约日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

const loadElders = async () => {
  try {
    const data = await masterDataApi.listActiveElders()
    elderList.value = data || []
  } catch (e) {
    console.error('加载老人列表失败', e)
  }
}

const loadPackages = async () => {
  try {
    const data = await masterDataApi.listServicePackages()
    packageList.value = data || []
  } catch (e) {
    console.error('加载服务包失败', e)
  }
}

const loadDetail = async (id) => {
  try {
    const data = await demandApi.get(id)
    Object.assign(form, data)
    if (data.elderId) {
      onElderChange(data.elderId)
    }
    if (data.servicePackageId) {
      onPackageChange(data.servicePackageId)
    }
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const onElderChange = async (elderId) => {
  if (!elderId) return
  try {
    const data = await masterDataApi.getElder(elderId)
    elderInfo.nursingLevelName = data.nursingLevelName || ''
    elderInfo.riskLevel = data.riskLevel || ''
    elderInfo.phone = data.phone || ''
    elderInfo.address = data.address || ''
    elderInfo.contraindications = data.contraindicationNames || []
    
    try {
      const contacts = await masterDataApi.getElderContacts(elderId)
      contactList.value = contacts || []
    } catch (e) {
      contactList.value = []
    }
  } catch (e) {
    console.error('加载老人信息失败', e)
  }
}

const onPackageChange = (packageId) => {
  if (!packageId) return
  const pkg = packageList.value.find(p => p.id === packageId)
  if (pkg) {
    packageInfo.serviceDuration = pkg.serviceDuration || ''
    packageInfo.price = pkg.price || ''
    calculateEndTime()
  }
}

const calculateEndTime = () => {
  if (!form.startTime || !packageInfo.serviceDuration) return
  const [h, m] = form.startTime.split(':').map(Number)
  const hours = h + parseFloat(packageInfo.serviceDuration)
  const endH = Math.floor(hours)
  const endM = Math.round((hours - endH) * 60)
  form.endTime = `${String(endH).padStart(2, '0')}:${String(endM).padStart(2, '0')}`
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await demandApi.update(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await demandApi.create(form)
      ElMessage.success('提交成功')
    }
    
    router.push('/demand/list')
  } catch (e) {
    if (e !== false) {
      ElMessage.error(e.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  router.back()
}

onMounted(() => {
  loadElders()
  loadPackages()
  if (isEdit.value) {
    loadDetail(route.params.id)
  }
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

.contraindications-box {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
