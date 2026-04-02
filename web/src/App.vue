<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMenuStore } from './store/modules/menu'
import { useTabsStore } from './store/modules/tabs'
import { ArrowLeft, ArrowRight, SwitchButton } from '@element-plus/icons-vue'
import { removeToken } from './utils/auth'
import { ElMessage } from 'element-plus'
import { logout } from './api/auth'

const icons = {
  ArrowLeft,
  ArrowRight,
  SwitchButton
}

const handleLogout = async () => {
  try {
    await logout()
    removeToken()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
    removeToken()
    router.push('/login')
  }
}

const router = useRouter()
const route = useRoute()
const menuStore = useMenuStore()
const tabsStore = useTabsStore()
const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const currentTab = ref<any>(null)

const isLoginPage = computed(() => route.path === '/login')

const handleMenuClick = (path: string, name: string, title: string) => {
  tabsStore.addTab({
    path,
    name,
    title,
    component: name
  })
  router.push(path)
}

const handleContextMenu = (event: MouseEvent, tab: any) => {
  event.preventDefault()
  currentTab.value = tab
  contextMenuVisible.value = true
  contextMenuPosition.value = {
    x: event.clientX,
    y: event.clientY
  }
}

const handleCloseCurrent = () => {
  if (currentTab.value) {
    tabsStore.removeTab(currentTab.value.path)
    contextMenuVisible.value = false
  }
}

const handleCloseOther = () => {
  if (currentTab.value) {
    tabsStore.removeOtherTabs(currentTab.value.path)
    contextMenuVisible.value = false
  }
}

const handleCloseLeft = () => {
  if (currentTab.value) {
    tabsStore.removeLeftTabs(currentTab.value.path)
    contextMenuVisible.value = false
  }
}

const handleCloseRight = () => {
  if (currentTab.value) {
    tabsStore.removeRightTabs(currentTab.value.path)
    contextMenuVisible.value = false
  }
}

const handleTabChange = (tab: string) => {
  router.push(tab)
}

const handleCloseTab = (tab: string) => {
  tabsStore.removeTab(tab)
  if (tabsStore.tabsList.length > 0) {
    router.push(tabsStore.activeTab)
  }
}

const handleClickOutside = () => {
  contextMenuVisible.value = false
}

watch(
  () => route.path,
  (newPath) => {
    const routeMeta = route.meta
    if (routeMeta.title) {
      tabsStore.addTab({
        path: newPath,
        name: route.name as string,
        title: routeMeta.title as string,
        component: route.name as string
      })
    }
  },
  { immediate: true }
)

onMounted(() => {
  tabsStore.addTab({
    path: '/home',
    name: 'Home',
    title: '首页',
    component: 'Home'
  })
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="app-container" :class="{ 'login-page': isLoginPage }">
    <div v-if="!isLoginPage" class="sidebar" :class="{ 'sidebar-collapsed': menuStore.isCollapse }">
      <div class="sidebar-header">
        <h1>API管理系统</h1>
        <el-button class="collapse-btn" @click="menuStore.toggleCollapse">
          <el-icon :size="20">
            <component :is="menuStore.isCollapse ? icons.ArrowRight : icons.ArrowLeft" />
          </el-icon>
        </el-button>
      </div>
      <div class="sidebar-menu">
        <el-menu
          :collapse="menuStore.isCollapse"
          :default-active="route.path"
          class="el-menu-vertical-demo"
          @select="(key: string) => handleMenuClick(key, menuStore.menuList.find(item => item.path === key)?.name || '', menuStore.menuList.find(item => item.path === key)?.meta.title || '')"
        >
          <el-menu-item
            v-for="menu in menuStore.menuList"
            :key="menu.path"
            :index="menu.path"
          >
            <el-icon><component :is="menu.meta.icon" /></el-icon>
            <template #title>{{ menu.meta.title }}</template>
          </el-menu-item>
        </el-menu>
      </div>
    </div>
    <div v-if="!isLoginPage" class="main-content">
      <div class="header">
        <div class="header-title">API接口管理系统</div>
        <div class="header-actions">
          <el-button type="danger" size="small" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </div>
      <div class="tabs-container">
        <el-tabs
          v-model="tabsStore.activeTab"
          class="demo-tabs"
          @tab-click="(tab: any) => handleTabChange(tab.props.name)"
          @tab-remove="(tab: string) => handleCloseTab(tab)"
          type="card"
          closable
        >
          <el-tab-pane
            v-for="tab in tabsStore.tabsList"
            :key="tab.path"
            :label="tab.title"
            :name="tab.path"
            :closable="tab.path !== '/home'"
            @contextmenu="(event: MouseEvent) => handleContextMenu(event, tab)"
          >
          </el-tab-pane>
        </el-tabs>
      </div>
      <div class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
    <div
      v-if="!isLoginPage && contextMenuVisible"
      class="context-menu"
      :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
    >
      <div class="context-menu-item" @click.stop="handleCloseCurrent">关闭当前页</div>
      <div class="context-menu-item" @click.stop="handleCloseOther">关闭其他页签</div>
      <div class="context-menu-item" @click.stop="handleCloseLeft">关闭左侧页签</div>
      <div class="context-menu-item" @click.stop="handleCloseRight">关闭右侧页签</div>
    </div>
    <router-view v-if="isLoginPage" />
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background-color: #303133;
  color: #fff;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.sidebar.sidebar-collapsed {
  width: 60px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #404040;
}

.sidebar-header h1 {
  font-size: 16px;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-header .collapse-btn {
  color: #fff;
  padding: 8px;
  background-color: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  min-width: 32px;
  height: 32px;
}

.sidebar-header .collapse-btn:hover {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.2);
  border-color: #409EFF;
}

.sidebar-collapsed .sidebar-header {
  justify-content: center;
  padding: 0;
}

.sidebar-collapsed .sidebar-header h1 {
  display: none;
}

.sidebar-menu {
  flex: 1;
  padding-top: 20px;
  overflow-x: hidden;
}

.el-menu-vertical-demo {
  background-color: transparent;
  border-right: none;
}

.el-menu-item {
  color: #fff;
}

.el-menu-item.is-active {
  background-color: #409EFF;
}

.sidebar-collapsed .el-menu-item {
  padding: 0 20px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  font-size: 18px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  align-items: center;
}

.tabs-container {
  height: 40px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.content {
  flex: 1;
  overflow: auto;
  padding: 20px;
  background-color: #f5f7fa;
}

.context-menu {
  position: fixed;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 9999;
  min-width: 120px;
}

.context-menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.context-menu-item:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-page .router-view {
  width: 100%;
}
</style>
