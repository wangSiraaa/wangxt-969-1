<template>
  <div class="nurse-checkin">
    <el-card v-if="order" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <el-tag :type="statusTagType(order.status)" size="large">{{ statusMap[order.status] }}</el-tag>
            <span class="order-no">#{{ order.orderNo }}</span>
          </div>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-descriptions title="工单信息" :column="1" border size="default">
            <el-descriptions-item label="老人">{{ elderName }}</el-descriptions-item>
            <el-descriptions-item label="服务包">{{ servicePackageName }}</el-descriptions-item>
            <el-descriptions-item label="计划时间">{{ order.plannedStartTime }} ~ {{ order.plannedEndTime }}</el-descriptions-item>
            <el-descriptions-item label="服务地址">{{ order.serviceAddress }}</el-descriptions-item>
            <el-descriptions-item label="护理等级">
              <el-tag v-if="nursingLevel" :type="nursingLevelColor(nursingLevel.code)">{{ nursingLevel.name }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="风险等级">
              <el-tag :type="riskLevelTagType(order.riskLevel)">{{ riskLevelMap[order.riskLevel] }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <el-card v-if="taboos && taboos.length" class="mt-4" shadow="never">
            <template #header>
              <div class="sub-header">
                <el-icon><Warning /></el-icon>
                <span>禁忌事项（{{ taboos.length }}条）</span>
              </div>
            </template>
            <el-tag v-for="t in taboos" :key="t.id" type="danger" effect="dark" class="mr-2 mb-2">{{ t.name }}</el-tag>
          </el-card>

          <el-card v-if="emergencyContacts && emergencyContacts.length" class="mt-4" shadow="never">
            <template #header>
              <div class="sub-header">
                <el-icon><Phone /></el-icon>
                <span>紧急联系人（{{ emergencyContacts.length }}人）</span>
              </div>
            </template>
            <el-table :data="emergencyContacts" size="small">
              <el-table-column prop="name" label="姓名" width="100" />
              <el-table-column prop="relation" label="关系" width="100" />
              <el-table-column prop="phone" label="电话" width="140" />
              <el-table-column prop="isPrimary" label="主联系人" width="100">
                <template #default="{row}">
                  <el-tag v-if="row.isPrimary" type="success" size="small">是</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <div class="sub-header">
                <el-icon><Location /></el-icon>
                <span>签到定位（演示占位）</span>
              </div>
            </template>
            <div class="map-placeholder">
              <el-icon color="#409EFF" size="64px"><Location /></el-icon>
              <div class="map-info">
                <p><strong>老人地址：</strong>{{ order.serviceAddress }}</p>
                <p><strong>当前定位：</strong>北京市{{ order.district }}（模拟GPS定位）</p>
                <p><strong>距离围栏：</strong>{{ mockDistance.toFixed(2) }} km / 半径 3.0 km</p>
                <el-tag :type="mockDistance < 3 ? 'success' : 'danger'" size="large">
                  {{ mockDistance < 3 ? '✓ 在服务范围内' : '✗ 超出服务范围' }}
                </el-tag>
              </div>
            </div>
          </el-card>

          <el-card class="mt-4" shadow="never">
            <template #header>
              <div class="sub-header">
                <el-icon><Camera /></el-icon>
                <span>服务照片（演示占位）</span>
              </div>
            </template>
            <el-upload
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="6"
              :on-change="handlePhotoChange"
              :file-list="photoList"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="tip">上传前与服务后对比照片，最多6张（当前为占位模拟）</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="24" class="mt-4">
        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <div class="sub-header">
                <el-icon><Clock /></el-icon>
                <span>签到签退</span>
              </div>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="签到时间">
                <span class="success-text" v-if="checkIn?.checkInTime">{{ checkIn.checkInTime }}</span>
                <el-tag v-else type="info" size="small">未签到</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="签到定位">
                <span v-if="checkIn?.checkInLatitude">{{ checkIn.checkInLatitude }}, {{ checkIn.checkInLongitude }}</span>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="签退时间">
                <span class="success-text" v-if="checkIn?.checkOutTime">{{ checkIn.checkOutTime }}</span>
                <el-tag v-else type="info" size="small">未签退</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="服务时长">
                <span v-if="checkIn?.serviceDurationMinutes">{{ checkIn.serviceDurationMinutes }} 分钟</span>
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
            <div class="actions mt-4">
              <el-button
                v-if="canCheckIn"
                type="primary"
                size="large"
                @click="handleCheckIn"
                :loading="checkInLoading"
              >
                签到上岗
              </el-button>
              <el-button
                v-if="canCheckOut"
                type="success"
                size="large"
                @click="handleCheckOut"
                :loading="checkOutLoading"
              >
                签退完成
              </el-button>
              <el-button
                v-if="order.status === 'SERVICE_COMPLETED'"
                type="warning"
                size="large"
                @click="reportAbnormal"
              >
                异常上报
              </el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card shadow="never">
            <template #header>
              <div class="sub-header">
                <el-icon><Document /></el-icon>
                <span>服务记录</span>
              </div>
            </template>
            <el-form :model="serviceRecord" label-width="100px">
              <el-form-item label="服务内容">
                <el-input
                  v-model="serviceRecord.serviceContent"
                  type="textarea"
                  :rows="4"
                  placeholder="请填写服务内容和完成情况"
                />
              </el-form-item>
              <el-form-item label="老人状况">
                <el-radio-group v-model="serviceRecord.elderCondition">
                  <el-radio label="GOOD">良好</el-radio>
                  <el-radio label="NORMAL">一般</el-radio>
                  <el-radio label="POOR">较差</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="服务备注">
                <el-input
                  v-model="serviceRecord.remark"
                  type="textarea"
                  :rows="2"
                  placeholder="其他需要说明的情况"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveServiceRecord" :loading="saveLoading">
                  保存服务记录
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-dialog v-model="abnormalDialogVisible" title="异常上报" width="500px">
      <el-form :model="abnormalForm" label-width="100px">
        <el-form-item label="异常类型">
          <el-select v-model="abnormalForm.type" placeholder="请选择">
            <el-option label="服务超时" value="SERVICE_OVERTIME" />
            <el-option label="老人状况异常" value="ELDER_ABNORMAL" />
            <el-option label="环境异常" value="ENVIRONMENT_ABNORMAL" />
            <el-option label="设备故障" value="EQUIPMENT_FAULT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="abnormalForm.riskLevel" placeholder="请选择">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input
            v-model="abnormalForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述异常情况"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="abnormalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAbnormal" :loading="abnormalLoading">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning, Phone, Location, Camera, Plus, Clock, Document } from '@element-plus/icons-vue'
import { workOrderApi, checkInApi, masterDataApi, abnormalApi, serviceRecordApi } from '@/api'

const route = useRoute()
const router = useRouter()

const orderId = route.params.id
const order = ref(null)
const checkIn = ref(null)
const elderName = ref('')
const servicePackageName = ref('')
const nursingLevel = ref(null)
const taboos = ref([])
const emergencyContacts = ref([])
const mockDistance = ref(1.25)
const photoList = ref([])
const checkInLoading = ref(false)
const checkOutLoading = ref(false)
const saveLoading = ref(false)
const abnormalLoading = ref(false)
const abnormalDialogVisible = ref(false)

const statusMap = {
  DISPATCHED: '已派单',
  NURSE_ACCEPTED: '已接单',
  CHECKED_IN: '服务中',
  CHECKED_OUT: '已签退',
  SERVICE_COMPLETED: '服务完成',
  PENDING_FAMILY_CONFIRM: '待家属确认',
  FAMILY_CONFIRMED: '家属已确认'
}

const riskLevelMap = {
  LOW: '低风险',
  MEDIUM: '中风险',
  HIGH: '高风险',
  CRITICAL: '极高风险'
}

const abnormalForm = ref({
  type: '',
  riskLevel: 'MEDIUM',
  description: ''
})

const serviceRecord = ref({
  serviceContent: '',
  elderCondition: 'NORMAL',
  remark: ''
})

const canCheckIn = computed(() => {
  return order.value && (order.value.status === 'DISPATCHED' || order.value.status === 'NURSE_ACCEPTED')
})

const canCheckOut = computed(() => {
  return order.value && order.value.status === 'CHECKED_IN'
})

const statusTagType = (status) => {
  const types = {
    DISPATCHED: 'warning',
    NURSE_ACCEPTED: 'primary',
    CHECKED_IN: 'success',
    CHECKED_OUT: 'info',
    SERVICE_COMPLETED: 'success',
    PENDING_FAMILY_CONFIRM: 'warning',
    FAMILY_CONFIRMED: 'success'
  }
  return types[status] || 'info'
}

const riskLevelTagType = (level) => {
  const types = {
    LOW: 'success',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }
  return types[level] || 'info'
}

const nursingLevelColor = (code) => {
  const colors = {
    NL01: '',
    NL02: 'warning',
    NL03: 'danger',
    NL04: 'danger'
  }
  return colors[code] || ''
}

const loadOrder = async () => {
  try {
    const data = await workOrderApi.get(orderId)
    order.value = data
  } catch (e) {
    ElMessage.error('加载工单失败')
  }
}

const loadCheckIn = async () => {
  try {
    const data = await checkInApi.getByWorkOrder(orderId)
    checkIn.value = data
  } catch (e) {
    console.log('暂无签到记录')
  }
}

const loadElderDetail = async () => {
  if (!order.value) return
  try {
    const data = await masterDataApi.getElder(order.value.elderId)
    elderName.value = data.name
    nursingLevel.value = data.nursingLevel
    taboos.value = data.taboos || []
    emergencyContacts.value = data.emergencyContacts || []
  } catch (e) {
    console.log('加载老人详情失败')
  }
}

const loadServicePackage = async () => {
  if (!order.value?.servicePackageId) return
  try {
    const data = await masterDataApi.getServicePackage(order.value.servicePackageId)
    servicePackageName.value = data.name
  } catch (e) {
    console.log('加载服务包失败')
  }
}

const handleCheckIn = async () => {
  if (mockDistance.value >= 3) {
    ElMessage.warning('当前位置超出服务范围，无法签到')
    return
  }
  try {
    checkInLoading.value = true
    await checkInApi.checkIn({
      workOrderId: orderId,
      checkInLatitude: 39.9042 + Math.random() * 0.01,
      checkInLongitude: 116.4074 + Math.random() * 0.01
    })
    ElMessage.success('签到成功')
    await loadOrder()
    await loadCheckIn()
  } catch (e) {
    ElMessage.error(e.message || '签到失败')
  } finally {
    checkInLoading.value = false
  }
}

const handleCheckOut = async () => {
  try {
    checkOutLoading.value = true
    await checkInApi.checkOut({
      workOrderId: orderId,
      checkOutLatitude: 39.9042 + Math.random() * 0.01,
      checkOutLongitude: 116.4074 + Math.random() * 0.01
    })
    ElMessage.success('签退成功')
    await loadOrder()
    await loadCheckIn()
  } catch (e) {
    ElMessage.error(e.message || '签退失败')
  } finally {
    checkOutLoading.value = false
  }
}

const handlePhotoChange = (file) => {
  if (photoList.value.length < 6) {
    photoList.value.push({
      name: file.name,
      url: URL.createObjectURL(file.raw)
    })
  }
}

const saveServiceRecord = async () => {
  if (!serviceRecord.value.serviceContent) {
    ElMessage.warning('请填写服务内容')
    return
  }
  try {
    saveLoading.value = true
    await serviceRecordApi.create({
      workOrderId: orderId,
      ...serviceRecord.value
    })
    ElMessage.success('服务记录已保存')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}

const reportAbnormal = () => {
  abnormalDialogVisible.value = true
}

const submitAbnormal = async () => {
  if (!abnormalForm.value.type) {
    ElMessage.warning('请选择异常类型')
    return
  }
  if (!abnormalForm.value.description) {
    ElMessage.warning('请填写异常描述')
    return
  }
  try {
    abnormalLoading.value = true
    await abnormalApi.create({
      workOrderId: orderId,
      ...abnormalForm.value
    })
    ElMessage.success('异常已上报，将进入异常处置队列')
    abnormalDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.message || '上报失败')
  } finally {
    abnormalLoading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  await loadOrder()
  await Promise.all([
    loadCheckIn(),
    loadElderDetail(),
    loadServicePackage()
  ])
})
</script>

<style scoped>
.nurse-checkin {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-no {
  margin-left: 12px;
  color: #606266;
  font-size: 14px;
}
.sub-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.map-placeholder {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}
.map-info p {
  margin: 8px 0;
}
.tip {
  margin-top: 12px;
  color: #909399;
  font-size: 12px;
}
.actions {
  display: flex;
  gap: 12px;
}
.success-text {
  color: #67c23a;
  font-weight: 600;
}
.mt-4 {
  margin-top: 16px;
}
.mr-2 {
  margin-right: 8px;
}
.mb-2 {
  margin-bottom: 8px;
}
</style>
