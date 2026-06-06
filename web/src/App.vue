<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMenuStore } from './store/modules/menu'
import { useTabsStore } from './store/modules/tabs'
import { useThemeStore, themes } from './store/modules/theme'
import { ArrowLeft, ArrowRight, SwitchButton, User, Lock, Brush } from '@element-plus/icons-vue'
import { removeToken } from './utils/auth'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { logout, changePassword } from './api/auth'

const icons = {
  ArrowLeft,
  ArrowRight,
  SwitchButton,
  User,
  Lock,
  Brush
}

const themeStore = useThemeStore()

const currentTheme = computed(() => {
  return themeStore.currentTheme
})

const currentThemeName = computed({
  get: () => currentTheme.value.name,
  set: (name: string) => handleThemeChange(name)
})

const handleThemeChange = (themeName: string) => {
  themeStore.setTheme(themeName)
  ElMessage.success('主题切换成功')
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

const passwordDialogVisible = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordLoading = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handlePasswordCommand = (command: string) => {
  if (command === 'changePassword') {
    passwordDialogVisible.value = true
  } else if (command === 'logout') {
    handleLogout()
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await changePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
        passwordForm.value = {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
      } catch (error) {
        console.error('修改密码失败:', error)
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

const handlePasswordDialogClose = () => {
  passwordFormRef.value?.resetFields()
}

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

const handleTabsContextMenu = (event: MouseEvent) => {
  // 查找点击的 tab 元素
  const target = event.target as HTMLElement
  let tabElement = target.closest('.el-tabs__item')

  if (tabElement) {
    const tabName = (tabElement as HTMLElement).getAttribute('aria-controls')
    const tab = tabsStore.tabsList.find(t => t.path === tabName || t.path === tabName?.replace('pane-', ''))
    if (tab) {
      currentTab.value = tab
      contextMenuVisible.value = true
      contextMenuPosition.value = {
        x: event.clientX,
        y: event.clientY
      }
    }
  }
}

// 判断当前标签左侧是否有其他标签
const hasLeftTabs = computed(() => {
  if (!currentTab.value) return false
  const index = tabsStore.tabsList.findIndex(item => item.path === currentTab.value.path)
  return index > 0
})

// 判断当前标签右侧是否有其他标签
const hasRightTabs = computed(() => {
  if (!currentTab.value) return false
  const index = tabsStore.tabsList.findIndex(item => item.path === currentTab.value.path)
  return index < tabsStore.tabsList.length - 1
})

// 判断是否只有一个标签页
const isOnlyOneTab = computed(() => {
  return tabsStore.tabsList.length === 1
})

// 判断当前是否是首页
const isHomeTab = computed(() => {
  return currentTab.value?.path === '/home'
})

// 判断是否有其他标签页（除了当前和首页）
const hasOtherTabs = computed(() => {
  if (!currentTab.value) return false
  return tabsStore.tabsList.length > 1
})

const handleRefresh = () => {
  if (currentTab.value) {
    const currentPath = currentTab.value.path
    contextMenuVisible.value = false
    // 先移除当前标签，重新添加实现刷新
    tabsStore.removeTab(currentPath)
    tabsStore.addTab({
      path: currentPath,
      name: currentTab.value.name,
      title: currentTab.value.title,
      component: currentTab.value.component
    })
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

watch(currentTheme, (theme) => {
  document.documentElement.style.setProperty('--el-color-primary', theme.primaryColor)
}, { immediate: true })

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
    <div v-if="!isLoginPage" class="sidebar" :class="{ 'sidebar-collapsed': menuStore.isCollapse }" :style="{ background: currentTheme.gradient }">
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
          <el-select
            v-model="currentThemeName"
            size="small"
            class="theme-select"
            :style="{ '--theme-color': currentTheme.primaryColor }"
          >
            <el-option
              v-for="theme in themes"
              :key="theme.name"
              :label="theme.label"
              :value="theme.name"
            />
          </el-select>
          <el-dropdown @command="handlePasswordCommand">
            <el-button size="small" class="theme-button" :style="{ backgroundColor: currentTheme.primaryColor, borderColor: currentTheme.primaryColor }">
              <el-icon><User /></el-icon>
              个人中心
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="changePassword">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div class="tabs-container" @contextmenu.prevent="handleTabsContextMenu">
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
      <!-- 只有一个标签页时，只显示刷新 -->
      <template v-if="isOnlyOneTab">
        <div class="context-menu-item" @click.stop="handleRefresh">刷新页面</div>
      </template>
      <!-- 多个标签页时 -->
      <template v-else>
        <div class="context-menu-item" @click.stop="handleRefresh">刷新页面</div>
        <template v-if="!isHomeTab">
          <div class="context-menu-divider"></div>
          <div class="context-menu-item" @click.stop="handleCloseCurrent">关闭当前</div>
        </template>
        <div v-if="hasLeftTabs" class="context-menu-item" @click.stop="handleCloseLeft">关闭左侧</div>
        <div v-if="hasRightTabs" class="context-menu-item" @click.stop="handleCloseRight">关闭右侧</div>
        <div v-if="hasOtherTabs && !isHomeTab" class="context-menu-item" @click.stop="handleCloseOther">关闭其他</div>
      </template>
    </div>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="450px" @close="handlePasswordDialogClose">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">确定</el-button>
      </template>
    </el-dialog>

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
  background-color: rgba(255, 255, 255, 0.2) !important;
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
  gap: 12px;
}

.theme-select {
  width: 100px;
  border-radius: 4px;
  border-color: #e4e7ed;
}

.theme-select:hover {
  border-color: #409EFF;
}

.theme-button {
  color: #fff;
  border-radius: 4px;
  transition: all 0.3s;
}

.theme-button:hover {
  opacity: 0.9;
  color: #fff !important;
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

.context-menu-divider {
  height: 1px;
  background-color: #e4e7ed;
  margin: 4px 0;
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
