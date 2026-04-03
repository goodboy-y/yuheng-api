import request from '../utils/request'
import type { PageData, ApiResponse } from './common'

export interface Datasource {
  id: string
  name: string
  note: string
  url: string
  username: string
  password: string
  type: string
  className?: any
}

export const getDatasourceList = (page: number = 0, pageSize: number = 20, params: Record<string, any> = {}) => {
  return request.get<ApiResponse<PageData<Datasource>>>('/datasource/search', {
    params: { page, pageSize, ...params }
  })
}

export const addDatasource = (data: Omit<Datasource, 'id'>) => {
  return request.post<ApiResponse<any>>('/datasource/add', data)
}

export const deleteDatasource = (id: string) => {
  return request.delete<ApiResponse<any>>(`/datasource/delete/${id}`)
}

export const getDatasourceDetail = (id: string) => {
  return request.get<ApiResponse<Datasource>>(`/datasource/detail/${id}`)
}

export const updateDatasource = (data: Datasource) => {
  return request.put<ApiResponse<any>>('/datasource/update', data)
}

export const listDatasource = () => {
  return request.get<ApiResponse<Array<Datasource>>>('/datasource/list')
}

export const testDatasourceConnection = (data: Partial<Datasource>) => {
  return request.post<ApiResponse<string>>('/datasource/connect', data)
}