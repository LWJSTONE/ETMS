import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'

interface UserInfo {
  id: number
  username: string
  realName: string
  avatar: string
  deptName: string
  roles: string[]
  permissions: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 登录
  const loginAction = async (username: string, password: string, captcha?: string, captchaKey?: string) => {
    const res = await login({ username, password, captcha, captchaKey })
    token.value = res.data.accessToken
    localStorage.setItem('token', res.data.accessToken)
    userInfo.value = {
      id: res.data.userId,
      username: res.data.username,
      realName: res.data.realName,
      avatar: res.data.avatar,
      deptName: res.data.deptName,
      roles: res.data.roles,
      permissions: res.data.permissions
    }
    return res
  }

  // 获取用户信息
  const getUserInfoAction = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res
  }

  // 登出
  const logoutAction = async () => {
    await logout()
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    loginAction,
    getUserInfoAction,
    logoutAction
  }
})
