import { defineStore } from 'pinia'

const THEME_STORAGE_KEY = 'dbapi_theme_name'

export interface ThemeConfig {
  name: string
  label: string
  gradient: string
  primaryColor: string
  sidebarHeaderBg: string
  activeMenuBg: string
}

export const themes: ThemeConfig[] = [
  {
    name: 'purple',
    label: '蓝紫色',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    primaryColor: '#667eea',
    sidebarHeaderBg: 'rgba(255, 255, 255, 0.1)',
    activeMenuBg: '#409EFF'
  },
  {
    name: 'green',
    label: '青绿色',
    gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    primaryColor: '#11998e',
    sidebarHeaderBg: 'rgba(255, 255, 255, 0.1)',
    activeMenuBg: '#11998e'
  },
  {
    name: 'orange',
    label: '橙红色',
    gradient: 'linear-gradient(135deg, #fc4a1a 0%, #f7b733 100%)',
    primaryColor: '#fc4a1a',
    sidebarHeaderBg: 'rgba(255, 255, 255, 0.1)',
    activeMenuBg: '#fc4a1a'
  },
  {
    name: 'blue',
    label: '深蓝色',
    gradient: 'linear-gradient(135deg, #0c3483 0%, #a2b6df 100%)',
    primaryColor: '#0c3483',
    sidebarHeaderBg: 'rgba(255, 255, 255, 0.1)',
    activeMenuBg: '#0c3483'
  },
  {
    name: 'pink',
    label: '粉紫色',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    primaryColor: '#f093fb',
    sidebarHeaderBg: 'rgba(255, 255, 255, 0.1)',
    activeMenuBg: '#f093fb'
  },
  {
    name: 'dark',
    label: '经典黑',
    gradient: 'linear-gradient(135deg, #2c3e50 0%, #34495e 100%)',
    primaryColor: '#2c3e50',
    sidebarHeaderBg: '#404040',
    activeMenuBg: '#409EFF'
  }
]

function getStoredTheme(): ThemeConfig {
  const storedThemeName = localStorage.getItem(THEME_STORAGE_KEY)
  if (storedThemeName) {
    const theme = themes.find(t => t.name === storedThemeName)
    if (theme) {
      return theme
    }
  }
  return themes[0]!
}

export const useThemeStore = defineStore('theme', {
  state: () => ({
    currentTheme: getStoredTheme() as ThemeConfig
  }),
  actions: {
    setTheme(themeName: string) {
      const theme = themes.find(t => t.name === themeName)
      if (theme) {
        this.currentTheme = theme
        localStorage.setItem(THEME_STORAGE_KEY, themeName)
      }
    },
    getThemeByName(name: string): ThemeConfig | undefined {
      return themes.find(t => t.name === name)
    }
  }
})
