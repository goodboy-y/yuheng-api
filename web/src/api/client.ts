import request from '../utils/request'
import type { PageData, ApiResponse } from './common'

export interface ClientData {
  id: string
  name: string
  note: string
  clientId: string
  secret: string
  expireDesc: string
  expireDuration: string
  token: string
  expireAt: string
  accountId: string
}

export const getClientList = (page: number = 0, pageSize: number = 20, params: Record<string, any> = {}) => {
  return request.get<ApiResponse<PageData<ClientData>>>('/api-client/search', {
    params: { page, pageSize, ...params }
  })
}

export const addClient = (data: Omit<ClientData, 'id'>) => {
  return request.post<ApiResponse<any>>('/api-client/add', data)
}

export const deleteClient = (id: string) => {
  return request.delete<ApiResponse<any>>(`/api-client/delete/${id}`)
}

export const getClientDetail = (id: string) => {
  return request.get<ApiResponse<ClientData>>(`/api-client/detail/${id}`)
}

export const updateClient = (data: ClientData) => {
  return request.put<ApiResponse<any>>('/api-client/update', data)
}