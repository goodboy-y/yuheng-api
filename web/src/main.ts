import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import router from './router'
import './style.css'
import 'vue3-json-viewer/dist/vue3-json-viewer.css'
import App from './App.vue'

const app = createApp(App)
app.use(createPinia())
app.use(ElementPlus, {
  locale: zhCn as any
})
app.use(router)
app.mount('#app')
