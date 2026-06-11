<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">老人管理</div>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        <span>新增老人</span>
      </el-button>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" :model="filter">
        <el-form-item label="姓名">
          <el-input v-model="filter.name" placeholder="输入姓名搜索" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="正常" value="ACTIVE" />
            <el-option label="暂停服务" value="SUSPENDED" />
            <el-option label="已停用" value="INACTIVE" />
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
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="住址" show-overflow-tooltip />
        <el-table-column prop="emergencyContact" label="紧急联系人" width="110" />
        <el-table-column prop="emergencyPhone" label="紧急联系电话" width="140" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'SUSPENDED' ? 'warning' : 'danger'">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              link type="warning"
              @click="handleSuspend(row)"
            >暂停服务</el-button>
            <el-button
              v-if="row.status === 'SUSPENDED'"
              link type="success"
              @click="handleResume(row)"
            >恢复服务</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑老人' : '新增老人'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="住址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="紧急联系人" prop="emergencyContact">
              <el-input v-model="form.emergencyContact" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急联系电话" prop="emergencyPhone">
              <el-input v-model="form.emergencyPhone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="健康状况">
          <el-input v-model="form.healthCondition" type="textarea" :rows="3" />
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
const filter = reactive({ name: '', status: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentId = ref(null)
const form = reactive({
  name: '', idCard: '', phone: '', address: '',
  emergencyContact: '', emergencyPhone: '', healthCondition: ''
})
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const filteredList = computed(() => {
  let result = list.value
  if (filter.name) result = result.filter(i => i.name.includes(filter.name))
  if (filter.status) result = result.filter(i => i.status === filter.status)
  return result
})

const getStatusText = (s) => ({ ACTIVE: '正常', SUSPENDED: '暂停服务', INACTIVE: '已停用' })[s] || s

const resetForm = () => {
  Object.assign(form, { name: '', idCard: '', phone: '', address: '', emergencyContact: '', emergencyPhone: '', healthCondition: '' })
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
  list.value = await api.getElders()
}

const resetFilter = () => {
  filter.name = ''
  filter.status = ''
  loadData()
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (isEdit.value) {
    await api.updateElder(currentId.value, form)
    ElMessage.success('修改成功')
  } else {
    form.status = form.status || 'ACTIVE'
    await api.createElder(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleSuspend = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入暂停服务原因', '暂停服务', {
      confirmButtonText: '确认', cancelButtonText: '取消', inputPlaceholder: '暂停原因'
    })
    await api.suspendElder(row.id, value)
    ElMessage.success('已暂停服务')
    loadData()
  } catch (_) {}
}

const handleResume = async (row) => {
  await api.resumeElder(row.id)
  ElMessage.success('已恢复服务')
  loadData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除老人「${row.name}」吗？`, '提示', { type: 'warning' })
    await api.deleteElder(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (_) {}
}

onMounted(loadData)
</script>
