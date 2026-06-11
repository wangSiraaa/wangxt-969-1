import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'elders', name: 'Elders', component: () => import('@/views/ElderList.vue'), meta: { title: '老人管理' } },
      { path: 'caregivers', name: 'Caregivers', component: () => import('@/views/CaregiverList.vue'), meta: { title: '护理员管理' } },
      { path: 'demands', name: 'Demands', component: () => import('@/views/DemandList.vue'), meta: { title: '服务需求' } },
      { path: 'dispatch', name: 'Dispatch', component: () => import('@/views/DispatchCenter.vue'), meta: { title: '调度派单' } },
      { path: 'workorders', name: 'WorkOrders', component: () => import('@/views/WorkOrderList.vue'), meta: { title: '工单管理' } },
      { path: 'checkin', name: 'CheckIn', component: () => import('@/views/CheckInCenter.vue'), meta: { title: '签到管理' } },
      { path: 'settlements', name: 'Settlements', component: () => import('@/views/SettlementList.vue'), meta: { title: '结算管理' } },
      { path: 'trace/:demandId', name: 'Trace', component: () => import('@/views/StatusTrace.vue'), meta: { title: '状态追踪' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
