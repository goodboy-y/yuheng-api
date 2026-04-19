import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { isAuthenticated } from '../utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      public: true
    }
  },
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页',
      icon: 'House'
    }
  },
  {
    path: '/datasource',
    name: 'Datasource',
    component: () => import('../views/Datasource.vue'),
    meta: {
      title: '数据源管理',
      icon: 'Database'
    }
  },
  {
    path: '/api',
    name: 'Api',
    component: () => import('../views/Api.vue'),
    meta: {
      title: 'API管理',
      icon: 'Connection'
    }
  },
  {
    path: '/client',
    name: 'Client',
    component: () => import('../views/Client.vue'),
    meta: {
      title: '连接客户端管理',
      icon: 'Monitor'
    }
  },
  {
    path: '/api-config-access',
    name: 'ApiConfigAccess',
    component: () => import('../views/ApiConfigAccess.vue'),
    meta: {
      title: 'API授权管理',
      icon: 'Key'
    }
  },
  {
    path: '/api-access-log',
    name: 'ApiAccessLog',
    component: () => import('../views/ApiAccessLog.vue'),
    meta: {
      title: 'API访问日志',
      icon: 'Document'
    }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  if (to.meta.public) {
    next()
  } else if (!isAuthenticated()) {
    next('/login')
  } else {
    next()
  }
})

export default router