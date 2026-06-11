<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">服务需求管理</div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        <span>提交服务需求</span>
      </el-button>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" :model="filter">
        <el-form-item label="需求状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待派单" value="PENDING_DISPATCH" />
            <el-option label="已派单" value="DISPATCHED" />
            <el-option label="服务中" value="IN_SERVICE" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <el-table :data="filteredList" stripe border>
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="elderName" label="老人姓名" width="100" />
        <el-table-column prop="applicant" label="申请人" width="100" />
        <el-table-column prop="relation" label="关系" width="80" />
        <el-table-column prop="serviceType" label="服务类型" width="120" />
        <el-table-column prop="serviceContent" label="服务内容" show-overflow-tooltip />
        <el-table-column prop="requiredQualifications" label="所需资质" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="demandStatusType(row.status)">{{ getDemandStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="期望时间" width="170">
          <template #default="{ row }">{{ formatDate(row.expectedTime) }}</template>
        </el-table-column>
        <el-table-column label="预估金额" width="100">
          <template #default="{ row }">¥{{ row.estimatedAmount || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goTrace(row)">状态追踪</el-button>
            <el-button
              v-if="row.status === 'PENDING_DISPATCH'"
              link type="warning"
              @click="openDialog(row)"
            >编辑</el-button>
            <el-button
              v-if="row.status === 'PENDING_DISPATCH'"
              link type="success"
              @click="goDispatch(row)"
            >去派单</el-button>
            <el-button
              v-if="row.status === 'PENDING_DISPATCH'"
              link type="danger"
              @click="handleCancel(row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑服务需求' : '提交服务需求'" width="650px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="选择老人" prop="elderId">
          <el-select v-model="form.elderId" placeholder="请选择老人" filterable style="width: 100%">
            <el-option
              v-for="e in elders"
              :key="e.id"
              :label="`${e.name}（${e.phone}）`"
              :value="e.id"
              :disabled="e.status === 'SUSPENDED'"
            />
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 4px">
            暂停服务状态的老人不能提交需求
          </div>
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="申请人" prop="applicant">
              <el-input v-model="form.applicant" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话" prop="applicantPhone">
              <el-input v-model="form.applicantPhone" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="与老人关系" prop="relation">
              <el-select v-model="form.relation" placeholder="请选择" style="width: 100%">
                <el-option label="子女" value="子女" />
                <el-option label="配偶" value="配偶" />
                <el-option label="亲属" value="亲属" />
                <el-option label="本人" value="本人" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="服务类型" prop="serviceType">
          <el-select v-model="form.serviceType" placeholder="请选择" style="width: 100%">
            <el-option label="日常生活照料" value="日常生活照料" />
            <el-option label="医疗护理" value="医疗护理" />
            <el-option label="康复训练" value="康复训练" />
            <el-option label="心理陪伴" value="心理陪伴" />
            <el-option label="送餐服务" value="送餐服务" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务内容" prop="serviceContent">
          <el-input v-model="form.serviceContent" type="textarea" :rows="3" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="期望服务时间" prop="expectedTime">
              <el-date-picker
                v-model="form.expectedTime"
                type="datetime"
                placeholder="选择日期时间"
                style="width: 100%"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预估金额(元)">
              <el-input-number v-model="form.estimatedAmount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所需资质">
          <el-input v-model="form.requiredQualifications" placeholder="如：养老护理员中级, 护士资格证" />
          <div style="color: #909399; font-size: 12px; margin-top: 4px">
            派单时将校验护理员是否具备这些资质
          </div>
        </el-form-item>
        <el-form-item label="特殊要求">
          <el-input v-model="form.specialRequirement" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as api from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const list = ref([])
const elders = ref([])
const filter = reactive({ status: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentId = ref(null)
const form = reactive({
  elderId: null, applicant: '', applicantPhone: '', relation: '',
  serviceType: '', serviceContent: '', expectedTime: '',
  estimatedAmount: 0, requiredQualifications: '', specialRequirement: ''
})
const rules = {
  elderId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  serviceContent: [{ required: true, message: '请输入服务内容', trigger: 'blur' }],
  expectedTime: [{ required: true, message: '请选择期望时间', trigger: 'change' }],
  applicant: [{ required: true, message: '请输入申请人', trigger: 'blur' }],
  applicantPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const filteredList = computed(() => {
  return filter.status ? list.value.filter(i => i.status === filter.status) : list.value
})

const formatDate = (d) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
const getDemandStatusText = (s) => ({
  PENDING_DISPATCH: '待派单', DISPATCHED: '已派单', IN_SERVICE: '服务中',
  COMPLETED: '已完成', CANCELLED: '已取消'
})[s] || s
const demandStatusType = (s) => ({
  PENDING_DISPATCH: 'warning', DISPATCHED: 'primary', IN_SERVICE: 'success',
  COMPLETED: 'info', CANCELLED: 'danger'
})[s] || 'info'

const resetForm = () => {
  Object.assign(form, {
    elderId: null, applicant: '', applicantPhone: '', relation: '',
    serviceType: '', serviceContent: '', expectedTime: '',
    estimatedAmount: 0, requiredQualifications: '', specialRequirement: ''
  })
  currentId.value = null
  isEdit.value = false
}

const openDialog = (row) => {
  resetForm()
  if (row) {
    isEdit.value = true
    currentId.value = row.id
    Object.assign(form, row)
  }
  dialogVisible.value = true
}

const loadData = async () => {
  list.value = await api.getDemands()
  elders.value = await api.getElders()
}

const resetFilter = () => {
  filter.status = ''
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await api.updateDemand(currentId.value, form)
    ElMessage.success('修改成功')
  } else {
    try {
      await api.createDemand(form)
      ElMessage.success('需求提交成功')
    } catch (e) {
      return
    }
  }
  dialogVisible.value = false
  loadData()
}

const handleCancel = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消需求', {
      confirmButtonText: '确认', cancelButtonText: '取消', inputPlaceholder: '取消原因'
    })
    await api.cancelDemand(row.id, value)
    ElMessage.success('已取消需求')
    loadData()
  } catch (_) {}
}

const goDispatch = (row) => {
  router.push({ path: '/dispatch', query: { demandId: row.id } })
}

const goTrace = (row) => {
  router.push({ path: `/trace/${row.id}` })
}

onMounted(loadData)
</script>
