<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon><House /></el-icon>
        <span>养老服务系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#c9d1d9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/elders">
          <el-icon><User /></el-icon>
          <span>老人管理</span>
        </el-menu-item>
        <el-menu-item index="/caregivers">
          <el-icon><Avatar /></el-icon>
          <span>护理员管理</span>
        </el-menu-item>
        <el-menu-item index="/demands">
          <el-icon><Document /></el-icon>
          <span>服务需求</span>
        </el-menu-item>
        <el-menu-item index="/dispatch">
          <el-icon><Connection /></el-icon>
          <span>调度派单</span>
        </el-menu-item>
        <el-menu-item index="/workorders">
          <el-icon><Tickets /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
        <el-menu-item index="/checkin">
          <el-icon><Calendar /></el-icon>
          <span>签到管理</span>
        </el-menu-item>
        <el-menu-item index="/settlements">
          <el-icon><Money /></el-icon>
          <span>结算管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="breadcrumb-title">{{ pageTitle }}</span>
        <div class="user-info">
          <el-icon><UserFilled /></el-icon>
          <span>管理员</span>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '养老上门服务工单系统')
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #001529;
  color: #fff;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: #002140;
}
.logo .el-icon {
  font-size: 22px;
  color: #409eff;
}
:deep(.el-menu) {
  border-right: none;
}
:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}
.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.breadcrumb-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}
.main {
  background: #f0f2f5;
  padding: 0;
  overflow: auto;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
