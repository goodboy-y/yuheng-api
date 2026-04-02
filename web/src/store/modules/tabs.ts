import { defineStore } from 'pinia'

export interface TabItem {
  path: string
  name: string
  title: string
  component: string
}

export const useTabsStore = defineStore('tabs', {
  state: () => ({
    tabsList: [] as TabItem[],
    activeTab: ''
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
      this.tabsList = this.tabsList.filter(item => item.path === currentPath)
      this.activeTab = currentPath
    },
    removeLeftTabs(currentPath: string) {
      const index = this.tabsList.findIndex(item => item.path === currentPath)
      if (index > -1) {
        this.tabsList = this.tabsList.slice(index)
        this.activeTab = currentPath
      }
    },
    removeRightTabs(currentPath: string) {
      const index = this.tabsList.findIndex(item => item.path === currentPath)
      if (index > -1) {
        this.tabsList = this.tabsList.slice(0, index + 1)
        this.activeTab = currentPath
      }
    },
    setActiveTab(path: string) {
      this.activeTab = path
    }
  }
})