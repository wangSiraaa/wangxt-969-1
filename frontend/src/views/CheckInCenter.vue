<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">签到管理</div>
    </div>

    <el-row :gutter="16">
      <el-col :span="14">
        <div class="table-container" style="min-height: 600px">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
            待签到/服务中工单
          </div>
          <el-table
            :data="activeOrders"
            stripe border highlight-current-row
            @current-change="handleOrderSelect"
          >
            <el-table-column prop="orderNo" label="工单号" width="180" />
            <el-table-column prop="elderName" label="老人" width="80" />
            <el-table-column prop="caregiverName" label="护理员" width="80" />
            <el-table-column prop="serviceType" label="服务类型" width="100" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="orderStatusType(row.orderStatus)">{{ row.orderStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="预约时间" width="150">
              <template #default="{ row }">{{ formatDate(row.scheduledTime) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="table-container" style="margin-top: 16px">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
            最近签到记录
          </div>
          <el-table :data="recentRecords" stripe>
            <el-table-column prop="id" label="编号" width="60" />
            <el-table-column prop="elderName" label="老人" width="80" />
            <el-table-column prop="caregiverName" label="护理员" width="80" />
            <el-table-column label="签到时间" width="160">
              <template #default="{ row }">{{ formatDate(row.checkInTime) }}</template>
            </el-table-column>
            <el-table-column label="签退时间" width="160">
              <template #default="{ row }">{{ formatDate(row.checkOutTime) || '进行中' }}</template>
            </el-table-column>
            <el-table-column label="服务时长" width="90">
              <template #default="{ row }">{{ row.serviceHours || '-' }} 小时</template>
            </el-table-column>
            <el-table-column prop="checkInLocation" label="签到地点" show-overflow-tooltip />
          </el-table>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="table-container" style="min-height: 600px">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133;">
            签到操作
          </div>

          <div v-if="!selectedOrder" style="color: #909399; text-align: center; padding: 80px 0;">
            <el-icon style="font-size: 48px; color: #dcdfe6;"><Calendar /></el-icon>
            <div style="margin-top: 12px;">请在左侧选择一条工单</div>
          </div>

          <div v-else>
            <el-descriptions :column="1" border size="small" style="margin-bottom: 20px;">
              <el-descriptions-item label="工单号">{{ selectedOrder.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="老人">{{ selectedOrder.elderName }}</el-descriptions-item>
              <el-descriptions-item label="护理员">{{ selectedOrder.caregiverName }}</el-descriptions-item>
              <el-descriptions-item label="服务类型">{{ selectedOrder.serviceType }}</el-descriptions-item>
              <el-descriptions-item label="服务内容">{{ selectedOrder.serviceContent }}</el-descriptions-item>
              <el-descriptions-item label="工单状态">
                <el-tag :type="orderStatusType(selectedOrder.orderStatus)">{{ selectedOrder.orderStatus }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <template v-if="!currentCheckIn">
              <el-divider>上门签到</el-divider>
              <el-form label-width="90px">
                <el-form-item label="签到地点">
                  <el-input v-model="checkInForm.checkInLocation" placeholder="请输入签到地址" />
                </el-form-item>
                <el-form-item label="签到照片">
                  <el-input v-model="checkInForm.signImageUrl" placeholder="签到凭证照片URL（选填）" />
                </el-form-item>
              </el-form>
              <el-button
                type="primary"
                size="large"
                style="width: 100%"
                :disabled="selectedOrder.orderStatus === '已完成' || selectedOrder.orderStatus === '已取消'"
                @click="handleCheckIn"
              >
                <el-icon><Promotion /></el-icon>
                <span>确认签到</span>
              </el-button>
            </template>

            <template v-else>
              <el-descriptions :column="1" border size="small" style="margin-bottom: 16px;" title="签到信息">
                <el-descriptions-item label="签到时间">{{ formatDate(currentCheckIn.checkInTime) }}</el-descriptions-item>
                <el-descriptions-item label="签到地点">{{ currentCheckIn.checkInLocation || '-' }}</el-descriptions-item>
                <el-descriptions-item label="签退时间">
                  <span v-if="currentCheckIn.checkOutTime">{{ formatDate(currentCheckIn.checkOutTime) }}</span>
                  <el-tag v-else type="warning" size="small">服务中</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="服务时长">
                  {{ currentCheckIn.serviceHours ? currentCheckIn.serviceHours + ' 小时' : '-' }}
                </el-descriptions-item>
              </el-descriptions>

              <template v-if="!currentCheckIn.checkOutTime">
                <el-divider>服务完成签退</el-divider>
                <el-form label-width="90px">
                  <el-form-item label="服务记录">
                    <el-input v-model="checkOutForm.serviceRecord" type="textarea" :rows="3" placeholder="详细记录服务内容" />
                  </el-form-item>
                  <el-form-item label="老人状况">
                    <el-input v-model="checkOutForm.elderCondition" type="textarea" :rows="2" placeholder="记录老人身体、精神状况" />
                  </el-form-item>
                  <el-form-item label="备注">
                    <el-input v-model="checkOutForm.caregiverRemark" type="textarea" :rows="2" placeholder="护理员备注" />
                  </el-form-item>
                </el-form>
                <el-button
                  type="success"
                  size="large"
                  style="width: 100%"
                  @click="handleCheckOut"
                >
                  <el-icon><CircleCheck /></el-icon>
                  <span>确认签退并完成服务</span>
                </el-button>
              </template>

              <template v-else>
                <el-alert
                  type="success"
                  :closable="false"
                  title="服务已完成签退"
                  description="如需结算请前往结算管理页面"
                  show-icon
                />
                <div style="margin-top: 12px;">
                  <strong>服务记录：</strong>{{ currentCheckIn.serviceRecord || '-' }}
                </div>
                <div style="margin-top: 8px;">
                  <strong>老人状况：</strong>{{ currentCheckIn.elderCondition || '-' }}
                </div>
                <div style="margin-top: 8px;">
                  <strong>护理员备注：</strong>{{ currentCheckIn.caregiverRemark || '-' }}
                </div>
              </template>
            </template>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as api from '@/api'
import dayjs from 'dayjs'

const route = useRoute()
const activeOrders = ref([])
const recentRecords = ref([])
const selectedOrder = ref(null)
const currentCheckIn = ref(null)
const checkInForm = reactive({ checkInLocation: '', signImageUrl: '' })
const checkOutForm = reactive({ serviceRecord: '', elderCondition: '', caregiverRemark: '' })

const formatDate = (d) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
const orderStatusType = (s) => ({
  '待签到': 'primary', '服务中': 'success', '已完成': 'info', '已取消': 'danger'
})[s] || 'info'

const handleOrderSelect = async (row) => {
  selectedOrder.value = row
  currentCheckIn.value = null
  try {
    const records = await api.getCheckInsByWorkOrder(row.id)
    if (records && records.length > 0) {
      currentCheckIn.value = records.sort((a, b) => new Date(b.checkInTime) - new Date(a.checkInTime))[0]
    }
  } catch (_) {}
}

const loadActiveOrders = async () => {
  const all = await api.getWorkOrders()
  activeOrders.value = all.filter(o => o.orderStatus === '待签到' || o.orderStatus === '服务中')
  if (route.query.orderId) {
    const found = activeOrders.value.find(o => o.id === Number(route.query.orderId))
    if (found) {
      handleOrderSelect(found)
    } else if (activeOrders.value.length > 0) {
      handleOrderSelect(activeOrders.value[0])
    }
  } else if (activeOrders.value.length > 0) {
    handleOrderSelect(activeOrders.value[0])
  }
}

const loadRecords = async () => {
  const all = await api.getCheckIns()
  recentRecords.value = all.slice(0, 10)
}

const handleCheckIn = async () => {
  if (!selectedOrder.value) return
  try {
    await api.doCheckIn({
      workOrderId: selectedOrder.value.id,
      checkInLocation: checkInForm.checkInLocation,
      signImageUrl: checkInForm.signImageUrl
    })
    ElMessage.success('签到成功')
    checkInForm.checkInLocation = ''
    checkInForm.signImageUrl = ''
    loadActiveOrders()
    loadRecords()
  } catch (_) {}
}

const handleCheckOut = async () => {
  if (!currentCheckIn.value) return
  try {
    await api.doCheckOut(currentCheckIn.value.id, {
      serviceRecord: checkOutForm.serviceRecord,
      elderCondition: checkOutForm.elderCondition,
      caregiverRemark: checkOutForm.caregiverRemark
    })
    ElMessage.success('签退成功，服务已完成')
    checkOutForm.serviceRecord = ''
    checkOutForm.elderCondition = ''
    checkOutForm.caregiverRemark = ''
    loadActiveOrders()
    loadRecords()
  } catch (_) {}
}

onMounted(() => {
  loadActiveOrders()
  loadRecords()
})
</script>
