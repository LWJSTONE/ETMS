import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 白名单路由（无需登录即可访问）
const whiteList = ['/login', '/404']

// 需要管理员权限的路由
const adminRoutes = ['/system', '/training', '/attendance', '/exam', '/report']

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
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: { title: '部门管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'position',
        name: 'Position',
        component: () => import('@/views/system/position/index.vue'),
        meta: { title: '岗位管理', icon: 'Briefcase' }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理', icon: 'Collection' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '系统配置', icon: 'Tools' }
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '日志管理', icon: 'Document' }
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
        meta: { title: '课程管理', icon: 'Notebook' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/training/category/index.vue'),
        meta: { title: '课程分类', icon: 'Files' }
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('@/views/training/plan/index.vue'),
        meta: { title: '培训计划', icon: 'Calendar' }
      },
      {
        path: 'progress',
        name: 'Progress',
        component: () => import('@/views/training/progress/index.vue'),
        meta: { title: '学习进度', icon: 'DataLine' }
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
        meta: { title: '签到记录', icon: 'List' }
      },
      {
        path: 'apply',
        name: 'AttendanceApply',
        component: () => import('@/views/attendance/apply/index.vue'),
        meta: { title: '补签申请', icon: 'Edit' }
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
        meta: { title: '题库管理', icon: 'Collection' }
      },
      {
        path: 'paper',
        name: 'Paper',
        component: () => import('@/views/exam/paper/index.vue'),
        meta: { title: '试卷管理', icon: 'Document' }
      },
      {
        path: 'record',
        name: 'ExamRecord',
        component: () => import('@/views/exam/record/index.vue'),
        meta: { title: '考试记录', icon: 'Tickets' }
      },
      {
        path: 'result',
        name: 'ExamResult',
        component: () => import('@/views/exam/result/index.vue'),
        meta: { title: '成绩管理', icon: 'TrendCharts' }
      }
    ]
  },
  // 报表分析
  {
    path: '/report',
    name: 'Report',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/report/training',
    meta: { title: '报表分析', icon: 'DataAnalysis' },
    children: [
      {
        path: 'training',
        name: 'TrainingReport',
        component: () => import('@/views/report/training/index.vue'),
        meta: { title: '培训报表', icon: 'DataBoard' }
      },
      {
        path: 'exam',
        name: 'ExamReport',
        component: () => import('@/views/report/exam/index.vue'),
        meta: { title: '考核报表', icon: 'Histogram' }
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
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  
  const userStore = useUserStore()
  const token = userStore.token
  
  // 设置页面标题
  document.title = (to.meta?.title as string) || 'ETMS'
  
  // 白名单路由直接放行
  if (whiteList.some(path => to.path.startsWith(path))) {
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
    next({
      path: '/login',
      query: { redirect: to.fullPath }  // 保存原目标路由
    })
    return
  }
  
  // 已登录但无用户信息，尝试获取
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfoAction()
    } catch (error) {
      // 获取用户信息失败，清除token并重定向到登录页
      userStore.token = ''
      localStorage.removeItem('token')
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }
  
  // 权限检查
  const userInfo = userStore.userInfo
  const isAdmin = userInfo?.roleCode === 'admin' || userInfo?.roleCode === 'ADMIN'
  
  // 检查是否访问需要管理员权限的路由
  if (!isAdmin && adminRoutes.some(route => to.path.startsWith(route))) {
    ElMessage.warning('您没有权限访问该页面')
    next({ path: '/my/course' })  // 普通用户重定向到我的培训
    return
  }
  
  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
