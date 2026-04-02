import request from '../utils/request'
import type { PageData, ApiResponse } from './common'

export interface ApiSqlParam {
  name: string
  type: string
}

export interface SqlParam {
  sql: string
  params: ApiSqlParam[]
}

export interface ApiData {
  id: string
  name: string
  note: string
  path: string
  datasource: {
    id: string
    name: string
  }
  datasourceId: string
  sqlParam: SqlParam
  status: number
}

export const getApiList = (page: number = 0, pageSize: number = 20, params: Record<string, any> = {}) => {
  return request.get<ApiResponse<PageData<ApiData>>>('/apiConfig/search', {
    params: { page, pageSize, ...params }
  })
}

export const addApi = (data: Omit<ApiData, 'id'>) => {
  return request.post<ApiResponse<any>>('/apiConfig/add', data)
}

export const deleteApi = (id: string) => {
  return request.delete<ApiResponse<any>>(`/apiConfig/delete/${id}`)
}

export const getApiDetail = (id: string) => {
  return request.get<ApiResponse<ApiData>>(`/apiConfig/detail/${id}`)
}

export const updateApi = (data: ApiData) => {
  return request.put<ApiResponse<any>>('/apiConfig/update', data)
}