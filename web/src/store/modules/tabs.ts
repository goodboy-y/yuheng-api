import { defineStore } from 'pinia'

export interface TabItem {
  path: string
  name: string
  title: string
  component: string
}

const HOME_TAB: TabItem = {
  path: '/home',
  name: 'Home',
  title: '首页',
  component: 'Home'
}

export const useTabsStore = defineStore('tabs', {
  state: () => ({
    tabsList: [HOME_TAB] as TabItem[],
    activeTab: '/home',
    refreshCounter: 0
  }),
  actions: {
    addTab(tab: TabItem) {
      const isExist = this.tabsList.some(item => item.path === tab.path)
      if (!isExist) {
        this.tabsList.push(tab)
      }
      this.activeTab = tab.path
    },
    removeTab(path: string) {
      // 禁止删除首页
      if (path === '/home') {
        return
      }
      const index = this.tabsList.findIndex(item => item.path === path)
      if (index > -1) {
        this.tabsList.splice(index, 1)
      }
      if (this.activeTab === path && this.tabsList.length > 0) {
        const lastTab = this.tabsList[this.tabsList.length - 1]
        if (lastTab) {
          this.activeTab = lastTab.path
        }
      }
    },
    removeOtherTabs(currentPath: string) {
      // 保留首页和当前标签
      this.tabsList = this.tabsList.filter(item => item.path === '/home' || item.path === currentPath)
      this.activeTab = currentPath
    },
    removeLeftTabs(currentPath: string) {
      const currentIndex = this.tabsList.findIndex(item => item.path === currentPath)
      const homeIndex = this.tabsList.findIndex(item => item.path === '/home')
      if (currentIndex > -1) {
        // 找到首页
        const homeTab = this.tabsList.find(item => item.path === '/home')
        if (!homeTab) return

        // 关闭左侧：保留首页 + 当前标签 + 当前标签右侧
        if (homeIndex < currentIndex) {
          // 首页在左侧被关闭，需要重新构建列表：首页 + 当前及右侧
          const rightTabs = this.tabsList.slice(currentIndex)
          this.tabsList = [homeTab, ...rightTabs]
        }
        // 如果首页在右侧或就是当前标签，不需要任何操作
        this.activeTab = currentPath
      }
    },
    removeRightTabs(currentPath: string) {
      const currentIndex = this.tabsList.findIndex(item => item.path === currentPath)
      const homeIndex = this.tabsList.findIndex(item => item.path === '/home')
      if (currentIndex > -1) {
        // 找到首页
        const homeTab = this.tabsList.find(item => item.path === '/home')
        if (!homeTab) return

        // 关闭右侧：保留首页 + 当前标签 + 当前标签左侧
        if (homeIndex > currentIndex) {
          // 首页在右侧被关闭，需要重新构建列表：左侧 + 当前标签
          const leftTabs = this.tabsList.slice(0, currentIndex + 1)
          this.tabsList = [...leftTabs, homeTab]
        } else {
          // 首页在左侧或就是当前标签，直接截断
          this.tabsList = this.tabsList.slice(0, currentIndex + 1)
        }
        this.activeTab = currentPath
      }
    },
    setActiveTab(path: string) {
      this.activeTab = path
    },
    refreshTab(tab: TabItem) {
      const index = this.tabsList.findIndex(item => item.path === tab.path)
      if (index > -1) {
        // 在原来的位置替换标签，保持位置不变
        this.tabsList.splice(index, 1, tab)
        // 保持当前激活状态
        this.activeTab = tab.path
      }
      // 增加刷新计数器，触发组件重新渲染
      this.refreshCounter++
    }
  }
})
