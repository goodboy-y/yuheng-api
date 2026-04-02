export interface PageInfo {
  currentPage: number
  rowsInPage: number
  rowsPerPage: number
  totalRows: number
  totalPages: number
}

export interface PageData<T> {
  pageInfo: PageInfo
  sortInfos: any[]
  data: T[]
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}
