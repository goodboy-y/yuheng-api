import request from '../utils/request'
import type { PageData, ApiResponse } from './common'

export interface ApiSqlParam {
  name: string
  type: string
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
  sqlParam: {
    sql: string
    params: ApiSqlParam[]
  }
  status: number
  plugins?: Array<{
    apiPlugin: {
      id: string
      name: string
      description: string
      className: string
      accountId: string
    }
  }>
}

export const getApiList = (page: number = 0, pageSize: number = 20, params: Record<string, any> = {}) => {
  return request.get<ApiResponse<PageData<ApiData>>>('/apiConfig/search', {
    params: { page, pageSize, ...params }
  })
}

export const deleteApi = (id: string) => {
  return request.delete<ApiResponse<any>>(`/apiConfig/delete/${id}`)
}

export const testApi = (apiId: string, params: Record<string, any>) => {
  return request.post<ApiResponse<any>>(`/apiConfig/test/${apiId}`, params)
}

export const exportApiTestData = (apiId: string, params: Record<string, any>) => {
  return request.post(`/apiConfig/export/${apiId}`, params, {
    responseType: 'blob'
  })
}

export interface ApiFieldMapping {
  id?: string
  apiConfigId: string
  fieldName: string
  displayName: string
  columnWidth?: number
}

export interface ApiConfigWithMappings {
  apiConfig: ApiData
  fieldMappings: ApiFieldMapping[]
  pluginIds: string[]
}

export const addApiWithMappings = (data: ApiConfigWithMappings) => {
  return request.post<ApiResponse<string>>('/apiConfig/add', data)
}

export const updateApiWithMappings = (data: ApiConfigWithMappings) => {
  return request.post<ApiResponse<string>>('/apiConfig/update', data)
}

export const getApiDetailFull = (apiId: string) => {
  return request.get<ApiResponse<ApiConfigDetail>>(`/apiConfig/detail-full/${apiId}`)
}

export interface ApiConfigDetail {
  apiConfig: ApiData
  fieldMappings: ApiFieldMapping[]
}

export const parseSqlFields = (datasourceId: string, sql: string) => {
  return request.get<ApiResponse<string[]>>('/apiConfig/parse-sql-fields', {
    params: { datasourceId, sql }
  })
}

// 获取未授权的API列表
export const getUnauthorizedApis = (clientId: string, page: number = 0, pageSize: number = 20, name: string = '') => {
  return request.get<ApiResponse<PageData<ApiData>>>('/apiConfig/unauthorized', {
    params: { clientId, pageNum: page, pageSize, name }
  })
}