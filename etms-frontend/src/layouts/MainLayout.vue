<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="logo" />
        <span v-show="!isCollapse">ETMS</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <!-- 有子菜单（多于1个可访问的子路由） -->
            <el-sub-menu v-if="getAccessibleChildren(route).length > 1" :index="route.path">
              <template #title>
                <el-icon><component :is="route.meta?.icon" /></el-icon>
                <span>{{ route.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in getAccessibleChildren(route)"
                :key="child.path"
                :index="`${route.path}/${child.path}`"
              >
                <el-icon><component :is="child.meta?.icon" /></el-icon>
                <span>{{ child.meta?.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <!-- 无子菜单（只有1个或0个可访问的子路由） -->
            <el-menu-item v-else :index="getFullPath(route.path, getAccessibleChildren(route)[0]?.path)">
              <el-icon><component :is="route.meta?.icon || getAccessibleChildren(route)[0]?.meta?.icon" /></el-icon>
              <span>{{ route.meta?.title || getAccessibleChildren(route)[0]?.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>
    
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="450px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码(至少6位)"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit" :loading="passwordLoading">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { updatePassword } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 是否折叠侧边栏
const isCollapse = ref(false)

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 获取完整菜单路径（正确拼接父路径）
const getFullPath = (parentPath: string, childPath?: string) => {
  if (!childPath) return parentPath
  // 处理根路径的情况，避免出现 //dashboard
  if (parentPath === '/') {
    return `/${childPath}`
  }
  return `${parentPath}/${childPath}`
}

// 面包屑
const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

// 检查用户是否有权限访问某个路由
const hasRoutePermission = (route: any): boolean => {
  // 获取用户信息
  const userInfo = userStore.userInfo
  const roleNames = userInfo?.roleNames || []
  const userPermissions = userInfo?.permissions || []

  // 检查是否为管理员角色（管理员拥有所有权限）
  const isAdmin = roleNames.some(name =>
    name === '超级管理员' || name === '管理员' || name.toLowerCase() === 'admin'
  )

  if (isAdmin) return true

  // 获取路由所需权限
  const requiredPermission = route.meta?.permission as string | undefined

  // 如果路由没有权限要求，则所有人都可以访问
  if (!requiredPermission) return true

  // 检查用户是否拥有所需权限
  return userPermissions.some((userPerm: string) => {
    // 完全匹配
    if (userPerm === requiredPermission) return true
    // 超级权限
    if (userPerm === '*') return true
    // 通配符匹配，如 system:* 匹配 system:user:view
    if (userPerm.endsWith(':*')) {
      const prefix = userPerm.slice(0, -1)
      return requiredPermission.startsWith(prefix)
    }
    return false
  })
}

// 获取可访问的子路由
const getAccessibleChildren = (route: any) => {
  if (!route.children) return []
  return route.children.filter((child: any) => {
    // 子路由隐藏的不显示在菜单中
    if (child.meta?.hidden) return false
    // 检查子路由权限
    return hasRoutePermission(child)
  })
}

// 菜单路由（根据权限过滤）
const menuRoutes = computed(() => {
  const routes = router.getRoutes()

  return routes.filter(route => {
    // 基本过滤：必须有标题、不能隐藏、必须有子路由
    if (!route.meta?.title || route.meta?.hidden || !route.children?.length) {
      return false
    }

    // 检查父路由权限
    if (!hasRoutePermission(route)) {
      return false
    }

    // 如果有子路由，检查是否有至少一个可访问的子路由
    const accessibleChildren = route.children.filter(child => {
      // 子路由隐藏的不显示在菜单中
      if (child.meta?.hidden) return false
      // 检查子路由权限
      return hasRoutePermission(child)
    })

    // 如果没有可访问的子路由，隐藏整个父菜单
    return accessibleChildren.length > 0
  })
})

// 切换折叠
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 修改密码相关
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 密码强度验证：长度6-20位，必须包含数字和字母
const validatePasswordStrength = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else if (value.length > 20) {
    callback(new Error('密码长度不能超过20位'))
  } else {
    const hasDigit = /\d/.test(value)
    const hasLetter = /[a-zA-Z]/.test(value)
    if (!hasDigit || !hasLetter) {
      callback(new Error('密码必须包含数字和字母'))
    } else {
      callback()
    }
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePasswordStrength, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'password':
      // 打开修改密码对话框
      Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
      passwordDialogVisible.value = true
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await userStore.logoutAction()
        ElMessage.success('退出成功')
        router.push('/login')
      }).catch(() => {})
      break
  }
}

// 提交修改密码
const handlePasswordSubmit = async () => {
  const valid = await passwordFormRef.value?.validate()
  if (!valid) return
  
  passwordLoading.value = true
  try {
    await updatePassword(userStore.userInfo?.id!, passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    // 退出登录
    await userStore.logoutAction()
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #2b3a4a;
    
    img {
      width: 32px;
      height: 32px;
    }
    
    span {
      margin-left: 10px;
      font-size: 20px;
      font-weight: bold;
      color: #fff;
    }
  }
  
  .el-menu {
    border-right: none;
  }
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      margin-right: 15px;
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .username {
        margin: 0 8px;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
