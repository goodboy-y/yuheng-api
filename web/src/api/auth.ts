import request from '../utils/request'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
}

export interface ChangePasswordData {
  oldPassword: string
  newPassword: string
}

export interface RefreshTokenResponse {
  accessToken: string
  refreshToken: string
}

export const login = (data: LoginData) => {
  return request.post<{ code: number; message: string; data: LoginResponse }>('/auth/login', data)
}

export const refreshToken = (refreshToken: string) => {
  return request.post<{ code: number; message: string; data: RefreshTokenResponse }>('/auth/refresh', { refreshToken })
}

export const logout = () => {
  return request.post('/auth/logout')
}

export const changePassword = (data: ChangePasswordData) => {
  return request.post('/auth/changePassword', data)
}
