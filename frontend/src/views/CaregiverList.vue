<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">护理员管理</div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        <span>新增护理员</span>
      </el-button>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" :model="filter">
        <el-form-item label="姓名">
          <el-input v-model="filter.name" placeholder="输入姓名搜索" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.active" placeholder="全部" clearable style="width: 140px">
            <el-option label="在职" :value="true" />
            <el-option label="停用" :value="false" />
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
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="qualifications" label="资质证书" show-overflow-tooltip />
        <el-table-column prop="skillLevel" label="技能等级" width="100" />
        <el-table-column prop="serviceTypes" label="服务类型" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'danger'">
              {{ row.active ? '在职' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑护理员' : '新增护理员'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="住址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="资质证书" prop="qualifications">
              <el-input v-model="form.qualifications" placeholder="多个用逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="技能等级">
              <el-select v-model="form.skillLevel" placeholder="请选择" style="width: 100%">
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="服务类型">
          <el-input v-model="form.serviceTypes" placeholder="多个用逗号分隔，如：日常生活照料, 医疗护理" />
        </el-form-item>
        <el-form-item label="在职状态">
          <el-switch v-model="form.active" active-text="在职" inactive-text="停用" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import * as api from '@/api'

const list = ref([])
const filter = reactive({ name: '', active: null })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentId = ref(null)
const form = reactive({
  name: '', idCard: '', phone: '', address: '',
  qualifications: '', skillLevel: '', serviceTypes: '', active: true
})
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  qualifications: [{ required: true, message: '请输入资质证书', trigger: 'blur' }]
}

const filteredList = computed(() => {
  let result = list.value
  if (filter.name) result = result.filter(i => i.name.includes(filter.name))
  if (filter.active !== null && filter.active !== '') {
    result = result.filter(i => i.active === filter.active)
  }
  return result
})

const resetForm = () => {
  Object.assign(form, {
    name: '', idCard: '', phone: '', address: '',
    qualifications: '', skillLevel: '', serviceTypes: '', active: true
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
  list.value = await api.getCaregivers()
}

const resetFilter = () => {
  filter.name = ''
  filter.active = null
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await api.updateCaregiver(currentId.value, form)
    ElMessage.success('修改成功')
  } else {
    await api.createCaregiver(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除护理员「${row.name}」吗？`, '提示', { type: 'warning' })
    await api.deleteCaregiver(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (_) {}
}

onMounted(loadData)
</script>
