import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

service.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200 || res.status === 'success') {
      return res
    }
    ElMessage.error(res.message || res.msg || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  error => {
    console.error('Response error:', error)
    ElMessage.error(error.response?.data?.message || error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default service
