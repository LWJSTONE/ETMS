<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>企业员工培训管理系统</h2>
        <p>Enterprise Training Management System</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { resetRedirectFlag } from '@/utils/request'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

// 用户名验证规则：长度3-20位，必须以字母开头，只允许字母、数字、下划线
const validateUsername = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度为3-20位'))
  } else if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(value)) {
    callback(new Error('用户名必须以字母开头，只能包含字母、数字和下划线'))
  } else {
    callback()
  }
}

const loginRules: FormRules = {
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ]
}

// 获取友好的错误消息
const getErrorMessage = (error: any): string => {
  // 检查是否有响应数据
  if (error.response?.data?.message) {
    return error.response.data.message
  }
  // 检查是否有错误消息
  if (error.message) {
    // 处理常见网络错误
    if (error.message.includes('timeout')) {
      return '请求超时，请稍后重试'
    }
    if (error.message.includes('Network Error')) {
      return '网络连接失败，请检查网络'
    }
    return error.message
  }
  return '登录失败，请稍后重试'
}

const handleLogin = async () => {
  try {
    const valid = await loginFormRef.value?.validate()
    if (!valid) return
  } catch {
    return
  }
  
  loading.value = true
  try {
    await userStore.loginAction(loginForm.username, loginForm.password)
    // 重置401重定向标志位，允许后续的401错误能够正常处理
    resetRedirectFlag()
    ElMessage.success('登录成功')
    // 登录成功后跳转到原目标页面或首页
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (error: any) {
    const errorMessage = getErrorMessage(error)
    ElMessage.error(errorMessage)
    // 登录失败后清空密码（安全考虑）
    loginForm.password = ''
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    margin: 0 0 10px;
    font-size: 24px;
    color: #333;
  }
  
  p {
    margin: 0;
    font-size: 14px;
    color: #999;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .login-btn {
    width: 100%;
  }
}

</style>
