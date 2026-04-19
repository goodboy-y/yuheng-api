import { defineStore } from 'pinia'
import { House, DataBoard, Connection, Monitor, Key, Document } from '@element-plus/icons-vue'

export const useMenuStore = defineStore('menu', {
  state: () => ({
    isCollapse: false,
    menuList: [
      {
        path: '/home',
        name: 'Home',
        meta: {
          title: '首页',
          icon: House
        }
      },
      {
        path: '/datasource',
        name: 'Datasource',
        meta: {
          title: '数据源管理',
          icon: DataBoard
        }
      },
      {
        path: '/api',
        name: 'Api',
        meta: {
          title: 'API管理',
          icon: Connection
        }
      },
      {
        path: '/client',
        name: 'Client',
        meta: {
          title: '连接客户端管理',
          icon: Monitor
        }
      },
      {
        path: '/api-config-access',
        name: 'ApiConfigAccess',
        meta: {
          title: 'API授权管理',
          icon: Key
        }
      },
      {
        path: '/api-access-log',
        name: 'ApiAccessLog',
        meta: {
          title: 'API访问日志',
          icon: Document
        }
      }
    ]
  }),
  actions: {
    toggleCollapse() {
      this.isCollapse = !this.isCollapse
    }
  }
})