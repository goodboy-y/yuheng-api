import request from '../utils/request'

export interface LoginData {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
}

export const login = (data: LoginData) => {
  return request.post<{ code: number; message: string; data: LoginResponse }>('/auth/login', data)
}

export const logout = () => {
  return request.post('/auth/logout')
}
