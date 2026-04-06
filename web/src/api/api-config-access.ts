import request from '../utils/request'
import type { PageData, ApiResponse } from './common'

export interface ApiConfigAccessData {
  id: string
  client: {
    id: string
    name: string
    clientId: string
  }
  apiConfig: {
    id: string
    name: string
    path: string
  }
  accountId: string
  createTime: string
}

// 分页查询授权列表
export const getApiConfigAccessList = (page: number = 0, pageSize: number = 20, params: Record<string, any> = {}) => {
  return request.get<ApiResponse<PageData<ApiConfigAccessData>>>('/apiConfigAccess/search', {
    params: { page, pageSize, ...params }
  })
}

// 授权
export const grantAccess = (clientId: string, apiConfigId: string) => {
  return request.post<ApiResponse<any>>('/apiConfigAccess/grant', {
    clientId,
    apiConfigId
  })
}

// 撤销授权
export const revokeAccess = (id: string) => {
  return request.delete<ApiResponse<any>>(`/apiConfigAccess/revoke/${id}`)
}

// 获取授权详情
export const getAccessDetail = (id: string) => {
  return request.get<ApiResponse<ApiConfigAccessData>>(`/apiConfigAccess/detail/${id}`)
}
