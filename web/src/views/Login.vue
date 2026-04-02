<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="login-header">
          <h2>系统登录</h2>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '../api/auth'
import { setToken, getToken } from '../utils/auth'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = ref({
  username: 'admin',
  password: 'admin'
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await login(loginForm.value)
        console.log('登录完整响应:', response)
        console.log('response.data:', response.data)
        
        // 尝试多种可能的token路径
        let token = response.data.data
        
        console.log('提取到的token:', token)
        
        if (!token) {
          console.error('无法从响应中提取token，响应结构:', response.data)
          ElMessage.error('登录失败：无法获取token')
          return
        }
        
        // 提取token字符串
        const tokenString = typeof token === 'string' ? token : token.token
        
        if (!tokenString) {
          console.error('无法从响应中提取token字符串，响应结构:', response.data)
          ElMessage.error('登录失败：无法获取token')
          return
        }
        
        setToken(tokenString)
        console.log('token已保存到localStorage:', getToken())
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请检查用户名和密码')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.login-header {
  text-align: center;
}

.login-header h2 {
  margin: 0;
  color: #333;
}
</style>
