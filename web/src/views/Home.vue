<template>
  <div class="home-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统信息</span>
        </div>
      </template>
      <div class="system-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="系统名称">API接口管理系统</el-descriptions-item>
          <el-descriptions-item label="版本">1.0.0</el-descriptions-item>
          <el-descriptions-item label="运行环境">开发环境</el-descriptions-item>
          <el-descriptions-item label="当前时间">{{ currentTime }}</el-descriptions-item>
          <el-descriptions-item label="功能模块" :span="2">首页、数据源管理、API管理、客户端管理</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>项目文档</span>
        </div>
      </template>
      <div class="system-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="文档地址">
            <el-link type="primary" href="https://goodboy-y.github.io/yuheng-api/" target="_blank">
              https://goodboy-y.github.io/yuheng-api/
            </el-link>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const currentTime = ref('')
let timer: number | null = null

const updateTime = () => {
  currentTime.value = new Date().toLocaleString()
}

onMounted(() => {
  updateTime()
  timer = window.setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.home-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.system-info {
  margin-top: 20px;
}
</style>