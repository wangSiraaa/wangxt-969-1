<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据字典</h2>
    </div>

    <el-tabs v-model="activeTab" type="card">
      <el-tab-pane label="护理等级" name="nursingLevel">
        <el-card>
          <el-table :data="nursingLevels" stripe style="width: 100%">
            <el-table-column prop="code" label="等级编码" width="120" />
            <el-table-column prop="name" label="等级名称" width="150" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="风险等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getRiskType(row.riskLevel)" size="small">{{ row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="基础时薪" width="120">
              <template #default="{ row }">¥{{ row.baseHourlyRate || '-' }}/小时</template>
            </el-table-column>
            <el-table-column label="所需资质" show-overflow-tooltip min-width="200">
              <template #default="{ row }">{{ translateQuals(row.requiredQualifications) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                  {{ row.enabled ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="服务包" name="servicePackage">
        <el-card>
          <el-table :data="servicePackages" stripe style="width: 100%">
            <el-table-column prop="code" label="服务包编码" width="120" />
            <el-table-column prop="name" label="服务包名称" width="180" />
            <el-table-column prop="serviceType" label="服务类型" width="120" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column label="标准时长" width="100">
              <template #default="{ row }">{{ row.standardDurationMinutes }} 分钟</template>
            </el-table-column>
            <el-table-column label="基础价格" width="100">
              <template #default="{ row }">¥{{ row.basePrice || '-' }}</template>
            </el-table-column>
            <el-table-column label="超时单价" width="100">
              <template #default="{ row }">¥{{ row.hourlyRate || '-' }}/小时</template>
            </el-table-column>
            <el-table-column label="风险等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getRiskType(row.riskLevel)" size="small">{{ row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="是否可保险" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.insurable" type="success" size="small">是</el-tag>
                <span v-else class="text-muted">否</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="禁忌事项" name="contraindication">
        <el-card>
          <el-table :data="contraindications" stripe style="width: 100%">
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="name" label="名称" width="180" />
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column label="严重程度" width="100">
              <template #default="{ row }">
                <el-tag :type="row.severity === 'CRITICAL' ? 'danger' : row.severity === 'HIGH' ? 'warning' : 'info'" size="small">
                  {{ row.severity }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="handlingNotes" label="处理注意事项" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { masterDataApi } from '@/api'
import { riskLevelMap } from '@/utils'
import { ElMessage } from 'element-plus'

const activeTab = ref('nursingLevel')
const nursingLevels = ref([])
const servicePackages = ref([])
const contraindications = ref([])

const getRiskType = (level) => riskLevelMap[level]?.type || 'info'

const translateQuals = (quals) => {
  if (!quals) return '-'
  const map = {
    CAREGIVER_BASIC: '初级养老护理员证',
    CAREGIVER_INTERMEDIATE: '中级养老护理员证',
    CAREGIVER_SENIOR: '高级养老护理员证',
    NURSE_LICENSE: '护士执业证书',
    BATH_ASSIST: '助浴服务资格证',
    FIRST_AID: '红十字急救证',
    REHAB_TRAINING: '康复训练师资格证',
    MEDICAL_TRAINING: '医学护理专项培训证书',
    MEAL_TRAINING: '助餐服务专项培训',
    DEMENTIA_CARE: '认知症照护培训证书',
    HOSPICE_CARE: '安宁疗护专科证书',
    MASSAGE_THERAPIST: '按摩师资格证'
  }
  return quals.split(',').map(q => map[q.trim()] || q.trim()).join('、')
}

const loadData = async () => {
  try {
    const [levels, packages, contra] = await Promise.all([
      masterDataApi.listNursingLevels(),
      masterDataApi.listServicePackages(),
      masterDataApi.listContraindications()
    ])
    nursingLevels.value = levels || []
    servicePackages.value = packages || []
    contraindications.value = contra || []
  } catch (e) {
    console.error('加载数据字典失败', e)
    ElMessage.error('加载数据字典失败')
  }
}

onMounted(() => {
  loadData()
})
</script>
