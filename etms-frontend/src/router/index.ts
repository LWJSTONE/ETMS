import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { STORAGE_KEYS } from '@/constants/storage'

// 白名单路由（无需登录即可访问）
const whiteList = ['/login', '/404']

// 权限常量定义
const PERMISSIONS = {
  // 系统管理
  USER_VIEW: 'system:user:view',
  ROLE_VIEW: 'system:role:view',
  DEPT_VIEW: 'system:dept:view',
  POSITION_VIEW: 'system:position:view',
  DICT_VIEW: 'system:dict:view',
  CONFIG_VIEW: 'system:config:view',
  LOG_VIEW: 'system:log:view',
  // 培训管理
  COURSE_VIEW: 'training:course:view',
  CATEGORY_VIEW: 'training:category:view',
  PLAN_VIEW: 'training:plan:view',
  PROGRESS_VIEW: 'training:progress:view',
  // 签到管理
  ATTENDANCE_VIEW: 'attendance:record:view',
  // 考核管理
  QUESTION_VIEW: 'exam:question:view',
  PAPER_VIEW: 'exam:paper:view',
  RECORD_VIEW: 'exam:record:view',
  RESULT_VIEW: 'exam:result:view',
  // 报表分析
  REPORT_VIEW: 'report:view'
} as const

// 需要管理员权限的路由前缀（用于兼容旧逻辑）
const adminRoutes = ['/system', '/training', '/attendance', '/exam', '/report']

/**
 * 检查用户是否有指定权限
 * @param userPermissions 用户权限列表
 * @param requiredPermission 需要的权限
 * @param isAdmin 是否为管理员
 */
const hasPermission = (
  userPermissions: string[],
  requiredPermission?: string,
  isAdmin?: boolean
): boolean => {
  // 管理员拥有所有权限
  if (isAdmin) return true
  // 无权限要求
  if (!requiredPermission) return true
  // 检查用户是否拥有所需权限（支持通配符匹配）
  return userPermissions.some(p => {
    // 完全匹配
    if (p === requiredPermission) return true
    // 通配符匹配，如 system:* 匹配 system:user:view
    if (p.endsWith(':*')) {
      const prefix = p.slice(0, -1) // 移除 *
      return requiredPermission.startsWith(prefix)
    }
    // 超级权限 * 匹配所有
    if (p === '*') return true
    return false
  })
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  },
  // 系统管理
  {
    path: '/system',
    name: 'System',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', permission: PERMISSIONS.USER_VIEW }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: PERMISSIONS.ROLE_VIEW }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding', permission: PERMISSIONS.DEPT_VIEW }
      },
      {
        path: 'position',
        name: 'Position',
        component: () => import('@/views/system/position/index.vue'),
        meta: { title: '岗位管理', icon: 'Briefcase', permission: PERMISSIONS.POSITION_VIEW }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection', permission: PERMISSIONS.DICT_VIEW }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '系统配置', icon: 'Tools', permission: PERMISSIONS.CONFIG_VIEW }
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '日志管理', icon: 'Document', permission: PERMISSIONS.LOG_VIEW }
      }
    ]
  },
  // 培训管理
  {
    path: '/training',
    name: 'Training',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/training/course',
    meta: { title: '培训管理', icon: 'Reading' },
    children: [
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/training/course/index.vue'),
        meta: { title: '课程管理', icon: 'Notebook', permission: PERMISSIONS.COURSE_VIEW }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/training/category/index.vue'),
        meta: { title: '课程分类', icon: 'Files', permission: PERMISSIONS.CATEGORY_VIEW }
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('@/views/training/plan/index.vue'),
        meta: { title: '培训计划', icon: 'Calendar', permission: PERMISSIONS.PLAN_VIEW }
      },
      {
        path: 'progress',
        name: 'Progress',
        component: () => import('@/views/training/progress/index.vue'),
        meta: { title: '学习进度', icon: 'DataLine', permission: PERMISSIONS.PROGRESS_VIEW }
      }
    ]
  },
  // 签到管理
  {
    path: '/attendance',
    name: 'Attendance',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/attendance/record',
    meta: { title: '签到管理', icon: 'Clock' },
    children: [
      {
        path: 'record',
        name: 'AttendanceRecord',
        component: () => import('@/views/attendance/record/index.vue'),
        meta: { title: '签到记录', icon: 'List', permission: PERMISSIONS.ATTENDANCE_VIEW }
      },
      {
        path: 'apply',
        name: 'AttendanceApply',
        component: () => import('@/views/attendance/apply/index.vue'),
        meta: { title: '补签申请', icon: 'Edit', permission: PERMISSIONS.ATTENDANCE_VIEW }
      }
    ]
  },
  // 考核管理
  {
    path: '/exam',
    name: 'Exam',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/exam/question',
    meta: { title: '考核管理', icon: 'Edit' },
    children: [
      {
        path: 'question',
        name: 'Question',
        component: () => import('@/views/exam/question/index.vue'),
        meta: { title: '题库管理', icon: 'Collection', permission: PERMISSIONS.QUESTION_VIEW }
      },
      {
        path: 'paper',
        name: 'Paper',
        component: () => import('@/views/exam/paper/index.vue'),
        meta: { title: '试卷管理', icon: 'Document', permission: PERMISSIONS.PAPER_VIEW }
      },
      {
        path: 'record',
        name: 'ExamRecord',
        component: () => import('@/views/exam/record/index.vue'),
        meta: { title: '考试记录', icon: 'Tickets', permission: PERMISSIONS.RECORD_VIEW }
      },
      {
        path: 'result',
        name: 'ExamResult',
        component: () => import('@/views/exam/result/index.vue'),
        meta: { title: '成绩管理', icon: 'TrendCharts', permission: PERMISSIONS.RESULT_VIEW }
      }
    ]
  },
  // 报表分析
  {
    path: '/report',
    name: 'Report',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/report/training',
    meta: { title: '报表分析', icon: 'DataAnalysis', permission: PERMISSIONS.REPORT_VIEW },
    children: [
      {
        path: 'training',
        name: 'TrainingReport',
        component: () => import('@/views/report/training/index.vue'),
        meta: { title: '培训报表', icon: 'DataBoard', permission: PERMISSIONS.REPORT_VIEW }
      },
      {
        path: 'exam',
        name: 'ExamReport',
        component: () => import('@/views/report/exam/index.vue'),
        meta: { title: '考核报表', icon: 'Histogram', permission: PERMISSIONS.REPORT_VIEW }
      }
    ]
  },
  // 我的培训（员工端）
  {
    path: '/my',
    name: 'My',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/my/course',
    meta: { title: '我的培训', icon: 'UserFilled' },
    children: [
      {
        path: 'course',
        name: 'MyCourse',
        component: () => import('@/views/my/course/index.vue'),
        meta: { title: '我的课程', icon: 'Reading' }
      },
      {
        path: 'learning',
        name: 'MyLearning',
        component: () => import('@/views/my/learning/index.vue'),
        meta: { title: '课程学习', icon: 'Reading', hidden: true }
      },
      {
        path: 'exam',
        name: 'MyExam',
        component: () => import('@/views/my/exam/index.vue'),
        meta: { title: '我的考试', icon: 'Edit' }
      },
      {
        path: 'exam/taking/:id',
        name: 'ExamTaking',
        component: () => import('@/views/my/exam/taking.vue'),
        meta: { title: '考试答题', icon: 'Edit', hidden: true }
      },
      {
        path: 'progress',
        name: 'MyProgress',
        component: () => import('@/views/my/progress/index.vue'),
        meta: { title: '学习记录', icon: 'DataLine' }
      },
      {
        path: 'result',
        name: 'MyResult',
        component: () => import('@/views/my/result/index.vue'),
        meta: { title: '我的成绩', icon: 'TrendCharts' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
// 防止无限重定向的标志
let hasTriedGetUserInfo = false

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  const token = userStore.token
  
  // 设置页面标题
  document.title = (to.meta?.title as string) || 'ETMS'
  
  // 白名单路由直接放行 - 使用精确匹配避免误匹配
  if (whiteList.includes(to.path) || to.path === '/404') {
    // 已登录用户访问登录页，重定向到首页
    if (to.path === '/login' && token) {
      next({ path: '/' })
      return
    }
    next()
    return
  }
  
  // 未登录，保存目标路由并重定向到登录页
  if (!token) {
    // 重置标志，允许下次登录时重新获取用户信息
    hasTriedGetUserInfo = false
    next({
      path: '/login',
      query: { redirect: to.fullPath }  // 保存原目标路由
    })
    return
  }
  
  // 已登录但无用户信息，尝试获取
  if (!userStore.userInfo && !hasTriedGetUserInfo) {
    hasTriedGetUserInfo = true
    try {
      await userStore.getUserInfoAction()
      // 获取成功后重置标志
      hasTriedGetUserInfo = false
    } catch (error) {
      // 获取用户信息失败，清除token并重定向到登录页
      userStore.token = ''
      userStore.userInfo = null
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
      localStorage.removeItem(STORAGE_KEYS.USER_INFO)
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }
  
  // 权限检查
  const userInfo = userStore.userInfo
  // 检查是否为管理员（admin角色拥有所有权限）
  const isAdmin = userInfo?.roleNames?.some(r => r.toLowerCase() === 'admin') ?? false
  // 获取用户权限列表
  const userPermissions = userInfo?.permissions || []
  // 获取路由所需权限
  const requiredPermission = to.meta?.permission as string | undefined

  // 细粒度权限检查
  if (!hasPermission(userPermissions, requiredPermission, isAdmin)) {
    ElMessage.warning('您没有权限访问该页面')
    // 普通用户重定向到我的培训
    next({ path: '/my/course' })
    return
  }
  
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
