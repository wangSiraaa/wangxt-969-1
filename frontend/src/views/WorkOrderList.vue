<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">工单管理</div>
    </div>

    <div class="filter-bar">
      <el-form :inline="true" :model="filter">
        <el-form-item label="工单状态">
          <el-select v-model="filter.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待签到" value="待签到" />
            <el-option label="服务中" value="服务中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已取消" value="已取消" />
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
        <el-table-column prop="orderNo" label="工单号" width="200" />
        <el-table-column prop="elderName" label="老人" width="90" />
        <el-table-column prop="caregiverName" label="护理员" width="90" />
        <el-table-column prop="serviceType" label="服务类型" width="110" />
        <el-table-column prop="serviceContent" label="服务内容" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="orderStatusType(row.orderStatus)">{{ row.orderStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dispatcher" label="调度员" width="90" />
        <el-table-column label="预约时间" width="160">
          <template #default="{ row }">{{ formatDate(row.scheduledTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goTrace(row)">状态追踪</el-button>
            <el-button
              v-if="row.orderStatus === '待签到' || row.orderStatus === '服务中'"
              link type="success"
              @click="goCheckIn(row)"
            >去签到</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as api from '@/api'
import dayjs from 'dayjs'

const router = useRouter()
const list = ref([])
const filter = reactive({ status: '' })

const filteredList = computed(() => {
  return filter.status ? list.value.filter(i => i.orderStatus === filter.status) : list.value
})

const formatDate = (d) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-'
const orderStatusType = (s) => ({
  '待签到': 'primary', '服务中': 'success', '已完成': 'info', '已取消': 'danger'
})[s] || 'info'

const loadData = async () => {
  list.value = await api.getWorkOrders()
}

const resetFilter = () => {
  filter.status = ''
  loadData()
}

const goCheckIn = (row) => {
  router.push({ path: '/checkin', query: { orderId: row.id } })
}

const goTrace = (row) => {
  router.push({ path: `/trace/${row.demandId}` })
}

onMounted(loadData)
</script>
