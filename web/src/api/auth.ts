import request from '../utils/request'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
}

export interface ChangePasswordData {
  oldPassword: string
  newPassword: string
}

export const login = (data: LoginData) => {
  return request.post<{ code: number; message: string; data: LoginResponse }>('/auth/login', data)
}

export const logout = () => {
  return request.post('/auth/logout')
}

export const changePassword = (data: ChangePasswordData) => {
  return request.post('/auth/changePassword', data)
}
