import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/index.vue'),
    meta: { title: '首页概览' }
  },
  {
    path: '/demand/list',
    name: 'DemandList',
    component: () => import('@/views/demand/list.vue'),
    meta: { title: '需求列表' }
  },
  {
    path: '/demand/create',
    name: 'DemandCreate',
    component: () => import('@/views/demand/create.vue'),
    meta: { title: '提交需求' }
  },
  {
    path: '/demand/detail/:id',
    name: 'DemandDetail',
    component: () => import('@/views/demand/detail.vue'),
    meta: { title: '需求详情' }
  },
  {
    path: '/dispatch/pending',
    name: 'DispatchPending',
    component: () => import('@/views/dispatch/pending.vue'),
    meta: { title: '待派单列表' }
  },
  {
    path: '/dispatch/orders',
    name: 'DispatchOrders',
    component: () => import('@/views/dispatch/orders.vue'),
    meta: { title: '工单列表' }
  },
  {
    path: '/dispatch/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/dispatch/orderDetail.vue'),
    meta: { title: '工单详情' }
  },
  {
    path: '/nurse/orders',
    name: 'NurseOrders',
    component: () => import('@/views/nurse/orders.vue'),
    meta: { title: '我的工单' }
  },
  {
    path: '/nurse/checkin',
    name: 'NurseCheckIn',
    component: () => import('@/views/nurse/checkin.vue'),
    meta: { title: '签到签退' }
  },
  {
    path: '/family/confirm',
    name: 'FamilyConfirm',
    component: () => import('@/views/family/confirm.vue'),
    meta: { title: '待确认列表' }
  },
  {
    path: '/family/history',
    name: 'FamilyHistory',
    component: () => import('@/views/family/history.vue'),
    meta: { title: '确认历史' }
  },
  {
    path: '/settlement/list',
    name: 'SettlementList',
    component: () => import('@/views/settlement/list.vue'),
    meta: { title: '结算列表' }
  },
  {
    path: '/settlement/approve',
    name: 'SettlementApprove',
    component: () => import('@/views/settlement/approve.vue'),
    meta: { title: '待审批结算' }
  },
  {
    path: '/abnormal/list',
    name: 'AbnormalList',
    component: () => import('@/views/abnormal/list.vue'),
    meta: { title: '异常队列' }
  },
  {
    path: '/abnormal/supplement',
    name: 'SupplementList',
    component: () => import('@/views/abnormal/supplement.vue'),
    meta: { title: '补单审批' }
  },
  {
    path: '/master/elders',
    name: 'MasterElders',
    component: () => import('@/views/master/elders.vue'),
    meta: { title: '老人档案' }
  },
  {
    path: '/master/nurses',
    name: 'MasterNurses',
    component: () => import('@/views/master/nurses.vue'),
    meta: { title: '护理员档案' }
  },
  {
    path: '/master/dict',
    name: 'MasterDict',
    component: () => import('@/views/master/dict.vue'),
    meta: { title: '数据字典' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
