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
        
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              prefix-icon="Key"
              size="large"
              class="captcha-input"
              @keyup.enter="handleLogin"
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
              <span v-else class="captcha-loading">加载中...</span>
            </div>
          </div>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCaptcha } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')
const captchaRetryCount = ref(0)
const MAX_CAPTCHA_RETRY = 3

const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
})

// 用户名验证规则：长度3-20位，只允许字母、数字、下划线
const validateUsername = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度为3-20位'))
  } else if (!/^[a-zA-Z0-9_]+$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字和下划线'))
  } else {
    callback()
  }
}

const loginRules: FormRules = {
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位字符', trigger: 'blur' }
  ]
}

// 获取验证码（带自动重试机制）
const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    captchaImage.value = res.data.captchaImage
    captchaKey.value = res.data.captchaKey
    captchaRetryCount.value = 0 // 成功后重置计数器
  } catch (error) {
    captchaRetryCount.value++
    if (captchaRetryCount.value < MAX_CAPTCHA_RETRY) {
      // 自动重试
      ElMessage.warning(`获取验证码失败，正在重试(${captchaRetryCount.value}/${MAX_CAPTCHA_RETRY})...`)
      setTimeout(refreshCaptcha, 1000)
    } else {
      ElMessage.error('获取验证码失败，请刷新页面重试')
      // 重置计数器，允许用户手动重试
      captchaRetryCount.value = 0
    }
  }
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
  const valid = await loginFormRef.value?.validate()
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.loginAction(loginForm.username, loginForm.password, loginForm.captcha, captchaKey.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: any) {
    const errorMessage = getErrorMessage(error)
    ElMessage.error(errorMessage)
    // 登录失败刷新验证码
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
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

.captcha-row {
  display: flex;
  width: 100%;
  gap: 10px;
  
  .captcha-input {
    flex: 1;
  }
  
  .captcha-image {
    width: 120px;
    height: 40px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    cursor: pointer;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .captcha-loading {
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
