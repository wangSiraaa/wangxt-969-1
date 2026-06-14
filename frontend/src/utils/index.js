import dayjs from 'dayjs'

export const statusMap = {
  PENDING_DISPATCH: { label: '待派单', type: 'warning' },
  DISPATCHED: { label: '已派发', type: 'primary' },
  NURSE_ACCEPTED: { label: '已接单', type: 'info' },
  CHECKED_IN: { label: '已签到', type: 'success' },
  IN_PROGRESS: { label: '服务中', type: 'primary' },
  SERVICE_COMPLETED: { label: '服务完成', type: 'info' },
  PENDING_FAMILY_CONFIRM: { label: '待家属确认', type: 'warning' },
  FAMILY_CONFIRMED: { label: '家属已确认', type: 'success' },
  FAMILY_REJECTED: { label: '家属拒绝', type: 'danger' },
  PENDING_SETTLEMENT: { label: '待结算', type: 'warning' },
  SETTLED: { label: '已结算', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' },
  ABNORMAL: { label: '异常', type: 'danger' }
}

export const abnormalTypeMap = {
  SERVICE_TIMEOUT: { label: '服务超时', type: 'warning' },
  TEMP_REASSIGN: { label: '临时改派', type: 'info' },
  NO_CHECKOUT: { label: '未签退', type: 'danger' },
  FAMILY_REJECTED: { label: '家属拒确认', type: 'warning' },
  QUALIFICATION_MISMATCH: { label: '资质不匹配', type: 'danger' },
  ELDER_PAUSED: { label: '老人暂停服务', type: 'warning' },
  NO_CHECKIN: { label: '未签到', type: 'danger' },
  AREA_MISMATCH: { label: '区域不匹配', type: 'warning' },
  TIME_CONFLICT: { label: '时间冲突', type: 'warning' },
  CONTINUOUS_HOURS_EXCEEDED: { label: '连续工时超限', type: 'warning' },
  RISK_LEVEL_MISMATCH: { label: '风险等级不匹配', type: 'danger' },
  OTHER: { label: '其他', type: 'info' }
}

export const abnormalStatusMap = {
  PENDING: { label: '待处理', type: 'danger' },
  PROCESSING: { label: '处理中', type: 'warning' },
  RESOLVED: { label: '已解决', type: 'success' }
}

export const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return ''
  return dayjs(date).format(format)
}

export const formatDateTime = (date, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!date) return ''
  return dayjs(date).format(format)
}

export const formatTime = (date, format = 'HH:mm') => {
  if (!date) return ''
  return dayjs(date).format(format)
}

export const genderMap = {
  MALE: { label: '男', type: 'primary' },
  FEMALE: { label: '女', type: 'success' }
}

export const elderStatusMap = {
  ACTIVE: { label: '正常服务', type: 'success' },
  PAUSED: { label: '暂停服务', type: 'warning' },
  INACTIVE: { label: '未激活', type: 'info' }
}

export const nurseStatusMap = {
  ACTIVE: { label: '在岗', type: 'success' },
  OFF_DUTY: { label: '休假', type: 'info' },
  INACTIVE: { label: '离职', type: 'danger' }
}

export const riskLevelMap = {
  LOW: { label: '低风险', type: 'success' },
  MEDIUM: { label: '中风险', type: 'warning' },
  HIGH: { label: '高风险', type: 'danger' },
  CRITICAL: { label: '极高风险', type: 'danger' }
}

export const qualificationStatusMap = {
  VALID: { label: '有效', type: 'success' },
  EXPIRED: { label: '已过期', type: 'danger' },
  SUSPENDED: { label: '暂停', type: 'warning' }
}

export const settlementStatusMap = {
  PENDING_SETTLEMENT: { label: '待结算', type: 'warning' },
  SETTLED: { label: '已结算', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' }
}

export const supplementStatusMap = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '审批通过', type: 'success' },
  REJECTED: { label: '审批拒绝', type: 'danger' }
}
