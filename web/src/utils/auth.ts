const TOKEN_KEY = 'dbapi_token'

export const getToken = (): string | null => {
  const token = localStorage.getItem(TOKEN_KEY)
  console.log('getToken - 从localStorage读取:', token)
  return token
}

export const setToken = (token: string): void => {
  console.log('setToken - 准备保存token到localStorage:', token)
  localStorage.setItem(TOKEN_KEY, token)
  console.log('setToken - token已保存')
  console.log('setToken - 验证保存结果:', localStorage.getItem(TOKEN_KEY))
}

export const removeToken = (): void => {
  console.log('removeToken - 删除token')
  localStorage.removeItem(TOKEN_KEY)
}

export const isAuthenticated = (): boolean => {
  const token = getToken()
  console.log('isAuthenticated - 检查登录状态:', !!token)
  return !!token
}
