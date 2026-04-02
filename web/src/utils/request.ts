import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { getToken, removeToken } from './auth'
import { ElMessage } from 'element-plus'
import router from '../router'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.PROD ? '/yuheng-api' : 'http://localhost:8520/yuheng-api',
  timeout: 60000
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 检查响应体中的code是否为401
    if (response.data?.code === 401) {
      removeToken()
      ElMessage.error(response.data?.message || '登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error('未授权'))
    }
    return response
  },
  (error) => {
    console.error('请求错误:', error)
    if (error.response?.status === 401) {
      removeToken()
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default service