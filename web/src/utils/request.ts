import axios from 'axios'
import type { AxiosInstance, AxiosResponse, AxiosRequestConfig } from 'axios'
import { getAccessToken, getRefreshToken, setTokens, removeTokens } from './auth'
import { ElMessage } from 'element-plus'
import router from '../router'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.PROD ? '/yuheng-api' : 'http://localhost:8520/yuheng-api',
  timeout: 60000
})

let isRefreshing = false
let requestQueue: Array<{ resolve: (token: string) => void; reject: (error: any) => void }> = []

const refreshToken = async (): Promise<string> => {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('没有refreshToken')
  }

  try {
    const response = await axios.post(`${service.defaults.baseURL}/auth/refresh`, {
      refreshToken
    })
    if (response.data?.code === 200 && response.data?.data) {
      const { accessToken, refreshToken: newRefreshToken } = response.data.data
      setTokens(accessToken, newRefreshToken)
      return accessToken
    } else {
      throw new Error(response.data?.message || '刷新token失败')
    }
  } catch (error) {
    removeTokens()
    router.push('/login')
    throw error
  }
}

service.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
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
    if (response.data?.code === 401 || response.data?.code === 403) {
      return Promise.reject({
        response: {
          data: response.data,
          status: response.data.code
        }
      })
    }
    return response
  },
  async (error) => {
    console.error('请求错误:', error)
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean }

    if ((error.response?.status === 401 || error.response?.data?.code === 401 || error.response?.status === 403) && !originalRequest._retry) {
      originalRequest._retry = true

      if (!isRefreshing) {
        isRefreshing = true
        try {
          const newAccessToken = await refreshToken()
          requestQueue.forEach(({ resolve }) => resolve(newAccessToken))
          requestQueue = []
          originalRequest.headers = originalRequest.headers || {}
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return service(originalRequest)
        } catch (refreshError) {
          requestQueue.forEach(({ reject }) => reject(refreshError))
          requestQueue = []
          ElMessage.error('登录已过期，请重新登录')
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      } else {
        return new Promise<string>((resolve, reject) => {
          requestQueue.push({ resolve, reject })
        }).then((newAccessToken) => {
          originalRequest.headers = originalRequest.headers || {}
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return service(originalRequest)
        }).catch(() => {
          ElMessage.error('登录已过期，请重新登录')
          return Promise.reject(error)
        })
      }
    }

    if (error.response?.status === 403) {
      removeTokens()
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default service