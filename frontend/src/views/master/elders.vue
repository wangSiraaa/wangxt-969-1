<template>
  <div class="page-container">
    <div class="page-header">
      <h2>老人档案</h2>
      <div class="header-actions">
        <el-input v-model="searchKeyword" placeholder="搜索老人姓名/编号" style="width: 200px; margin-right: 10px" clearable @clear="loadData">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="elderCode" label="老人编号" width="160" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.gender" :type="row.gender === 'MALE' ? 'primary' : 'success'" size="small">
              {{ row.gender === 'MALE' ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80">
          <template #default="{ row }">{{ calculateAge(row.birthDate) }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip min-width="200" />
        <el-table-column label="服务区域" width="100">
          <template #default="{ row }">{{ translateArea(row.serviceArea) }}</template>
        </el-table-column>
        <el-table-column label="护理等级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.nursingLevelCode" type="info" size="small">{{ row.nursingLevelCode }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.riskLevel" :type="getRiskType(row.riskLevel)" size="small">
              {{ getRiskLabel(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'warning'" size="small">
              {{ row.status === 'ACTIVE' ? '正常服务' : '暂停服务' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="老人档案详情" width="700px">
      <div v-if="currentElder">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="老人编号">{{ currentElder.elderCode }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentElder.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentElder.gender === 'MALE' ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ currentElder.birthDate }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentElder.phone }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentElder.idCard }}</el-descriptions-item>
          <el-descriptions-item label="服务区域">{{ translateArea(currentElder.serviceArea) }}</el-descriptions-item>
          <el-descriptions-item label="护理等级">{{ currentElder.nursingLevelCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="getRiskType(currentElder.riskLevel)" size="small">
              {{ getRiskLabel(currentElder.riskLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentElder.status === 'ACTIVE' ? 'success' : 'warning'" size="small">
              {{ currentElder.status === 'ACTIVE' ? '正常服务' : '暂停服务' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="医保类型">{{ currentElder.insuranceType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="医保报销比例">{{ currentElder.insuranceCoverage ? currentElder.insuranceCoverage + '%' : '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>居住地址</el-divider>
        <p>{{ currentElder.address }}</p>

        <el-divider>健康状况</el-divider>
        <p>{{ currentElder.healthStatus || '暂无记录' }}</p>

        <el-divider>过敏史</el-divider>
        <p>{{ currentElder.allergies || '无' }}</p>

        <el-divider>禁忌事项</el-divider>
        <p>{{ currentElder.contraindicationNames || '无' }}</p>

        <el-divider>紧急联系人</el-divider>
        <el-table :data="emergencyContacts" size="small" border>
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="relation" label="关系" width="80" />
          <el-table-column prop="phone" label="电话" width="130" />
          <el-table-column prop="altPhone" label="备用电话" width="130" />
          <el-table-column label="是否主要联系人" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.isPrimary" type="success" size="small">是</el-tag>
              <span v-else class="text-muted">否</span>
            </template>
          </el-table-column>
          <el-table-column label="可确认服务" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.canConfirmService" type="success" size="small">是</el-tag>
              <span v-else class="text-muted">否</span>
            </template>
          </el-table-column>
        </el-table>

        <el-divider v-if="currentElder.status === 'PAUSED'">暂停服务信息</el-divider>
        <el-alert v-if="currentElder.status === 'PAUSED'" :title="currentElder.pauseReason || '暂停服务中'" type="warning" :closable="false">
          <template #default>
            <p>暂停开始：{{ currentElder.pauseStartTime || '-' }}</p>
            <p>预计结束：{{ currentElder.pauseEndTime || '-' }}</p>
          </template>
        </el-alert>

        <el-divider>备注</el-divider>
        <p>{{ currentElder.remark || '暂无' }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { masterDataApi } from '@/api'
import { riskLevelMap } from '@/utils'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const detailVisible = ref(false)
const currentElder = ref(null)
const emergencyContacts = ref([])

const getRiskType = (level) => riskLevelMap[level]?.type || 'info'
const getRiskLabel = (level) => riskLevelMap[level]?.label || level

const calculateAge = (birthDate) => {
  if (!birthDate) return '-'
  return dayjs().diff(dayjs(birthDate), 'year')
}

const translateArea = (area) => {
  const map = {
    CHAOWAYANG: '朝阳区',
    HAIDIAN: '海淀区',
    XICHENG: '西城区',
    DONGCHENG: '东城区',
    FENGTAI: '丰台区',
    SHIJINGSHAN: '石景山区'
  }
  return map[area] || area || '-'
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await masterDataApi.listElders({ page: 0, size: 100 })
    let list = data?.content || data?.list || data || []
    if (searchKeyword.value) {
      list = list.filter(e =>
        e.name?.includes(searchKeyword.value) ||
        e.elderCode?.includes(searchKeyword.value)
      )
    }
    tableData.value = list
  } catch (e) {
    console.error('加载老人列表失败', e)
    ElMessage.error('加载老人列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const data = await masterDataApi.getElder(row.id)
    currentElder.value = data
    try {
      const contacts = await masterDataApi.getElderContacts(row.id)
      emergencyContacts.value = contacts || []
    } catch (e) {
      emergencyContacts.value = []
    }
    detailVisible.value = true
  } catch (e) {
    currentElder.value = row
    emergencyContacts.value = []
    detailVisible.value = true
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.header-actions {
  display: flex;
  align-items: center;
}
</style>
