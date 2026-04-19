import request from '../utils/request'
import type { PageData, ApiResponse } from './common'

export interface ApiAccessLogData {
  id: string
  accessTime: string
  path: string
  clientId: string
  params: string
  accountId: string
}

export const getApiAccessLogList = (page: number = 0, pageSize: number = 20, params: Record<string, any> = {}) => {
  return request.get<ApiResponse<PageData<ApiAccessLogData>>>('/apiAccessLog/search', {
    params: { page, pageSize, ...params }
  })
}

export const deleteApiAccessLog = (ids: string) => {
  return request.delete<ApiResponse<any>>('/apiAccessLog/delete', {
    params: { ids }
  })
}

export const archiveApiAccessLog = (beforeTime: string) => {
  return request.post<ApiResponse<any>>('/apiAccessLog/archive', null, {
    params: { beforeTime }
  })
}

export const getApiAccessLogArchiveList = (page: number = 0, pageSize: number = 20, params: Record<string, any> = {}) => {
  return request.get<ApiResponse<PageData<ApiAccessLogData>>>('/apiAccessLog/archive/search', {
    params: { page, pageSize, ...params }
  })
}