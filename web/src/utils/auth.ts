const ACCESS_TOKEN_KEY = 'dbapi_access_token'
const REFRESH_TOKEN_KEY = 'dbapi_refresh_token'

export const getAccessToken = (): string | null => {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export const setAccessToken = (token: string): void => {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export const getRefreshToken = (): string | null => {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

export const setRefreshToken = (token: string): void => {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

export const setTokens = (accessToken: string, refreshToken: string): void => {
  setAccessToken(accessToken)
  setRefreshToken(refreshToken)
}

export const removeTokens = (): void => {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export const isAuthenticated = (): boolean => {
  return !!getAccessToken()
}

export const getToken = (): string | null => {
  return getAccessToken()
}

export const setToken = (token: string): void => {
  setAccessToken(token)
}

export const removeToken = (): void => {
  removeTokens()
}
