<template>
  <div id="app" class="app-layout">
    <el-container style="height: 100vh">
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <el-icon :size="28" style="color: #fff"><House /></el-icon>
          <span class="logo-text">养老工单系统</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="#1f2d3d"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          router
          class="menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>首页概览</span>
          </el-menu-item>
          <el-sub-menu index="demand">
            <template #title>
              <el-icon><EditPen /></el-icon>
              <span>需求管理</span>
            </template>
            <el-menu-item index="/demand/list">需求列表</el-menu-item>
            <el-menu-item index="/demand/create">提交需求</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="dispatch">
            <template #title>
              <el-icon><Promotion /></el-icon>
              <span>调度派单</span>
            </template>
            <el-menu-item index="/dispatch/pending">待派单列表</el-menu-item>
            <el-menu-item index="/dispatch/orders">工单列表</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="nurse">
            <template #title>
              <el-icon><User /></el-icon>
              <span>护理员端</span>
            </template>
            <el-menu-item index="/nurse/orders">我的工单</el-menu-item>
            <el-menu-item index="/nurse/checkin">签到签退</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="family">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>家属确认</span>
            </template>
            <el-menu-item index="/family/confirm">待确认列表</el-menu-item>
            <el-menu-item index="/family/history">确认历史</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="settlement">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>结算管理</span>
            </template>
            <el-menu-item index="/settlement/list">结算列表</el-menu-item>
            <el-menu-item index="/settlement/approve">待审批结算</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="abnormal">
            <template #title>
              <el-icon><Warning /></el-icon>
              <span>异常处置</span>
            </template>
            <el-menu-item index="/abnormal/list">异常队列</el-menu-item>
            <el-menu-item index="/abnormal/supplement">补单审批</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="master">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>基础数据</span>
            </template>
            <el-menu-item index="/master/elders">老人档案</el-menu-item>
            <el-menu-item index="/master/nurses">护理员档案</el-menu-item>
            <el-menu-item index="/master/dict">数据字典</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-tag type="success" effect="dark">
              <el-icon><User /></el-icon>
              管理员
            </el-tag>
          </div>
        </el-header>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const breadcrumbTitle = computed(() => route.meta?.title || '')
</script>

<style scoped>
.app-layout {
  height: 100vh;
}

.sidebar {
  background-color: #1f2d3d;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background-color: #2b3b4f;
  border-bottom: 1px solid #3a4c63;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.menu {
  border-right: none;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.main-content {
  background-color: #f5f7fa;
  padding: 0;
  overflow-y: auto;
}
</style>
