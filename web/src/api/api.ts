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

export const getFieldMappings = (apiConfigId: string) => {
  return request.get<ApiResponse<ApiFieldMapping[]>>(`/apiConfig/field-mapping/${apiConfigId}`)
}

export const saveFieldMappings = (apiConfigId: string, mappings: ApiFieldMapping[]) => {
  return request.post<ApiResponse<any>>(`/apiConfig/field-mapping/${apiConfigId}`, mappings)
}

export interface ApiConfigWithMappings {
  apiConfig: ApiData
  fieldMappings: ApiFieldMapping[]
}

export const addApiWithMappings = (data: ApiConfigWithMappings) => {
  return request.post<ApiResponse<string>>('/apiConfig/add-with-mappings', data)
}

export const updateApiWithMappings = (data: ApiConfigWithMappings) => {
  return request.post<ApiResponse<any>>('/apiConfig/update-with-mappings', data)
}

export const parseSqlFields = (datasourceId: string, sql: string) => {
  return request.get<ApiResponse<string[]>>('/apiConfig/parse-sql-fields', {
    params: { datasourceId, sql }
  })
}