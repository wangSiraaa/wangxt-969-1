<template>
  <div class="page-container">
    <div class="page-header">
      <h2>护理员档案</h2>
      <div class="header-actions">
        <el-input v-model="searchKeyword" placeholder="搜索护理员姓名/工号" style="width: 200px; margin-right: 10px" clearable @clear="loadData">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="全部状态" style="width: 120px; margin-right: 10px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="在岗" value="ACTIVE" />
          <el-option label="休假" value="OFF_DUTY" />
          <el-option label="离职" value="INACTIVE" />
        </el-select>
        <el-button type="primary" @click="loadData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="nurseCode" label="工号" width="140" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.gender" :type="row.gender === 'MALE' ? 'primary' : 'success'" size="small">
              {{ row.gender === 'MALE' ? '男' : '女' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column label="服务区域" width="150">
          <template #default="{ row }">
            <span class="text-muted">{{ translateAreas(row.serviceAreas) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最高护理等级" width="120">
          <template #default="{ row }">{{ row.highestNursingLevelName || '-' }}</template>
        </el-table-column>
        <el-table-column label="完成单量" width="100">
          <template #default="{ row }">{{ row.completedOrdersCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <span class="text-warning">
              <el-icon><Star /></el-icon> {{ row.averageRating || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
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

    <el-dialog v-model="detailVisible" title="护理员档案详情" width="750px">
      <div v-if="currentNurse">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="工号">{{ currentNurse.nurseCode }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentNurse.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentNurse.gender === 'MALE' ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ currentNurse.birthDate }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentNurse.phone }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentNurse.idCard }}</el-descriptions-item>
          <el-descriptions-item label="入职日期">{{ currentNurse.hireDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentNurse.status)" size="small">
              {{ getStatusLabel(currentNurse.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="服务区域" :span="2">{{ translateAreas(currentNurse.serviceAreas) }}</el-descriptions-item>
          <el-descriptions-item label="完成单量">{{ currentNurse.completedOrdersCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="平均评分">{{ currentNurse.averageRating || '-' }}</el-descriptions-item>
          <el-descriptions-item label="准时率">{{ currentNurse.onTimeRate ? currentNurse.onTimeRate + '%' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="最大日单量">{{ currentNurse.maxDailyOrders || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>资质证书</el-divider>
        <el-table :data="qualifications" size="small" border>
          <el-table-column prop="name" label="证书名称" />
          <el-table-column prop="certificateNo" label="证书编号" width="180" />
          <el-table-column prop="level" label="等级" width="100" />
          <el-table-column prop="issuingAuthority" label="发证机关" width="180" />
          <el-table-column prop="issuedDate" label="发证日期" width="110" />
          <el-table-column prop="expiryDate" label="有效期至" width="110" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'VALID' ? 'success' : 'danger'" size="small">
                {{ row.status === 'VALID' ? '有效' : '已过期' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-divider>资质简介</el-divider>
        <p>{{ currentNurse.qualificationSummary || '暂无' }}</p>

        <el-divider>住址</el-divider>
        <p>{{ currentNurse.address || '暂无' }}</p>

        <el-divider>备注</el-divider>
        <p>{{ currentNurse.remark || '暂无' }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { masterDataApi } from '@/api'
import { nurseStatusMap } from '@/utils'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const statusFilter = ref('')
const detailVisible = ref(false)
const currentNurse = ref(null)
const qualifications = ref([])

const getStatusType = (status) => nurseStatusMap[status]?.type || 'info'
const getStatusLabel = (status) => nurseStatusMap[status]?.label || status

const translateAreas = (areas) => {
  if (!areas) return '-'
  const map = {
    CHAOWAYANG: '朝阳区',
    HAIDIAN: '海淀区',
    XICHENG: '西城区',
    DONGCHENG: '东城区',
    FENGTAI: '丰台区',
    SHIJINGSHAN: '石景山区'
  }
  return areas.split(',').map(a => map[a] || a).join('、')
}

const loadData = async () => {
  loading.value = true
  try {
    let data
    if (statusFilter.value === 'ACTIVE') {
      data = await masterDataApi.listActiveNurses()
    } else {
      data = await masterDataApi.listNurses({ page: 0, size: 100 })
      data = data?.content || data?.list || data || []
    }
    let list = Array.isArray(data) ? data : (data?.content || data?.list || [])
    if (searchKeyword.value) {
      list = list.filter(n =>
        n.name?.includes(searchKeyword.value) ||
        n.nurseCode?.includes(searchKeyword.value)
      )
    }
    tableData.value = list
  } catch (e) {
    console.error('加载护理员列表失败', e)
    ElMessage.error('加载护理员列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const data = await masterDataApi.getNurse(row.id)
    currentNurse.value = data
    try {
      const quals = await masterDataApi.getNurseQualifications(row.id)
      qualifications.value = quals || []
    } catch (e) {
      qualifications.value = []
    }
    detailVisible.value = true
  } catch (e) {
    currentNurse.value = row
    qualifications.value = []
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
