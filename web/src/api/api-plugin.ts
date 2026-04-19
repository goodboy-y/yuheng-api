import request from '../utils/request'
import type { ApiResponse } from './common'

export interface ApiPluginData {
  id: string
  name: string
  description: string
  className: string
  accountId: string
}

export const getApiPluginList = () => {
  return request.get<ApiResponse<ApiPluginData[]>>('/apiPlugin/list')
}

export const getApiConfigPlugins = (apiConfigId: string) => {
  return request.get<ApiResponse<ApiPluginData[]>>(`/apiPlugin/apiConfig/${apiConfigId}`)
}

export const saveApiConfigPlugins = (apiConfigId: string, pluginIds: string[]) => {
  return request.post<ApiResponse<any>>(`/apiPlugin/apiConfig/${apiConfigId}`, pluginIds)
}
