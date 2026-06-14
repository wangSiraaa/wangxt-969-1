import request from './request'

export const dashboardApi = {
  getStatistics: () => request.get('/dashboard/statistics').then(r => r.data),
  getSummary: () => request.get('/dashboard/summary').then(r => r.data),
  detectAbnormals: () => request.post('/dashboard/detect-abnormals').then(r => r.data)
}

export const demandApi = {
  list: (params) => request.get('/demands', { params }).then(r => r.data),
  get: (id) => request.get(`/demands/${id}`).then(r => r.data),
  getByCode: (code) => request.get(`/demands/code/${code}`).then(r => r.data),
  create: (data) => request.post('/demands', data).then(r => r.data),
  update: (id, data) => request.put(`/demands/${id}`, data).then(r => r.data),
  cancel: (id, reason, operator) => request.put(`/demands/${id}/cancel`, null, { params: { reason, operator } }).then(r => r.data),
  pendingDispatch: () => request.get('/demands/pending-dispatch').then(r => r.data),
  getVersions: (id) => request.get(`/demands/${id}/versions`).then(r => r.data),
  getAuditLogs: (id) => request.get(`/demands/${id}/audit-logs`).then(r => r.data)
}

export const workOrderApi = {
  list: (params) => request.get('/work-orders', { params }).then(r => r.data),
  get: (id) => request.get(`/work-orders/${id}`).then(r => r.data),
  getByCode: (code) => request.get(`/work-orders/code/${code}`).then(r => r.data),
  dispatch: (data) => request.post('/work-orders/dispatch', data).then(r => r.data),
  accept: (id, nurseId) => request.put(`/work-orders/${id}/accept`, null, { params: { nurseId } }).then(r => r.data),
  cancel: (id, reason, operator) => request.put(`/work-orders/${id}/cancel`, null, { params: { reason, operator } }).then(r => r.data),
  getCandidates: (demandId) => request.get(`/work-orders/demand/${demandId}/candidates`).then(r => r.data),
  previewValidation: (demandId, nurseId) => request.get('/work-orders/preview-validation', { params: { demandId, nurseId } }).then(r => r.data),
  listByStatus: (status) => request.get(`/work-orders/status/${status}`).then(r => r.data),
  listByNurse: (nurseId) => request.get(`/work-orders/nurse/${nurseId}`).then(r => r.data),
  listNurseActive: (nurseId) => request.get(`/work-orders/nurse/${nurseId}/active`).then(r => r.data),
  listNurseDaily: (nurseId, date) => request.get(`/work-orders/nurse/${nurseId}/daily`, { params: { date } }).then(r => r.data),
  getAuditLogs: (id) => request.get(`/work-orders/${id}/audit-logs`).then(r => r.data)
}

export const checkInApi = {
  checkIn: (data) => request.post('/checkin', data).then(r => r.data),
  checkOut: (data) => request.post('/checkin/checkout', data).then(r => r.data),
  getByWorkOrder: (workOrderId) => request.get(`/checkin/work-order/${workOrderId}`).then(r => r.data),
  getById: (id) => request.get(`/checkin/${id}`).then(r => r.data),
  getServiceRecord: (workOrderId) => request.get(`/checkin/work-order/${workOrderId}/service-record`).then(r => r.data)
}

export const familyApi = {
  confirm: (data) => request.post('/family-confirmation', data).then(r => r.data),
  getByWorkOrder: (workOrderId) => request.get(`/family-confirmation/work-order/${workOrderId}`).then(r => r.data),
  getById: (id) => request.get(`/family-confirmation/${id}`).then(r => r.data)
}

export const settlementApi = {
  list: (params) => request.get('/settlements', { params }).then(r => r.data),
  get: (id) => request.get(`/settlements/${id}`).then(r => r.data),
  getByWorkOrder: (workOrderId) => request.get(`/settlements/work-order/${workOrderId}`).then(r => r.data),
  create: (data) => request.post('/settlements', data).then(r => r.data),
  approve: (id, approver, remark) => request.put(`/settlements/${id}/approve`, null, { params: { approver, remark } }).then(r => r.data),
  createCompensation: (data) => request.post('/settlements/compensation', data).then(r => r.data),
  createInsuranceClaim: (data) => request.post('/settlements/insurance-claim', data).then(r => r.data),
  getAuditLogs: (id) => request.get(`/settlements/${id}/audit-logs`).then(r => r.data)
}

export const abnormalApi = {
  list: (params) => request.get('/abnormal-events', { params }).then(r => r.data),
  get: (id) => request.get(`/abnormal-events/${id}`).then(r => r.data),
  pending: () => request.get('/abnormal-events/pending').then(r => r.data),
  create: (data) => request.post('/abnormal-events', data).then(r => r.data),
  handle: (id, data) => request.post(`/abnormal-events/${id}/handle`, data).then(r => r.data),
  resolve: (id, params) => request.post(`/abnormal-events/${id}/resolve`, null, { params }).then(r => r.data),
  getByWorkOrder: (workOrderId) => request.get(`/abnormal-events/work-order/${workOrderId}`).then(r => r.data),
  getAuditLogs: (id) => request.get(`/abnormal-events/${id}/audit-logs`).then(r => r.data)
}

export const serviceRecordApi = {
  create: (data) => request.post('/service-records', data).then(r => r.data),
  getByWorkOrder: (workOrderId) => request.get(`/service-records/work-order/${workOrderId}`).then(r => r.data),
  get: (id) => request.get(`/service-records/${id}`).then(r => r.data),
  list: (params) => request.get('/service-records', { params }).then(r => r.data)
}

export const masterDataApi = {
  listElders: (params) => request.get('/master-data/elders', { params }).then(r => r.data),
  getElder: (id) => request.get(`/master-data/elders/${id}`).then(r => r.data),
  getElderContacts: (id) => request.get(`/master-data/elders/${id}/emergency-contacts`).then(r => r.data),
  listActiveElders: () => request.get('/master-data/elders/active').then(r => r.data),
  listNurses: (params) => request.get('/master-data/nurses', { params }).then(r => r.data),
  getNurse: (id) => request.get(`/master-data/nurses/${id}`).then(r => r.data),
  getNurseQualifications: (id) => request.get(`/master-data/nurses/${id}/qualifications`).then(r => r.data),
  listActiveNurses: () => request.get('/master-data/nurses/active').then(r => r.data),
  listNursingLevels: () => request.get('/master-data/nursing-levels').then(r => r.data),
  listServicePackages: () => request.get('/master-data/service-packages').then(r => r.data),
  getServicePackage: (id) => request.get(`/master-data/service-packages/${id}`).then(r => r.data),
  listContraindications: () => request.get('/master-data/contraindications').then(r => r.data),
  listSupplements: (approvalStatus) => request.get('/master-data/supplement-orders', { params: { approvalStatus } }).then(r => r.data),
  getSupplement: (id) => request.get(`/master-data/supplement-orders/${id}`).then(r => r.data),
  applySupplement: (data) => request.post('/master-data/supplement-orders', data).then(r => r.data),
  approveSupplement: (id, approved, approver, approvalRemark) => request.post(`/master-data/supplement-orders/${id}/approve`, null, { params: { approved, approver, approvalRemark } }).then(r => r.data)
}
