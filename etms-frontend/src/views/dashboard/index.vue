<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <el-row :gutter="20" class="welcome-section">
      <el-col :span="24">
        <el-card shadow="hover" class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎回来，{{ userStore.userInfo?.realName }}！</h2>
              <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
            </div>
            <div class="welcome-stats">
              <div class="stat-item">
                <span class="stat-value">{{ stats.courseCount }}</span>
                <span class="stat-label">待学习课程</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ stats.examCount }}</span>
                <span class="stat-label">待参加考试</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ stats.progress }}%</span>
                <span class="stat-label">学习进度</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 数据统计卡片 - 仅管理员可见 -->
    <el-row v-if="isAdmin" :gutter="20" class="stats-section">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon size="30"><User /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ overview.userCount }}</span>
            <span class="stat-title">系统用户</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon size="30"><Notebook /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ overview.courseCount }}</span>
            <span class="stat-title">课程总数</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon size="30"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ overview.planCount }}</span>
            <span class="stat-title">培训计划</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <el-icon size="30"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-number">{{ overview.examCount }}</span>
            <span class="stat-title">考试次数</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>我的学习进度统计</span>
            </div>
          </template>
          <div v-if="chartDataLoading" class="chart-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="progressChartData.length === 0" class="chart-empty">
            <el-empty description="暂无学习进度数据" :image-size="80" />
          </div>
          <div v-else ref="progressChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>我的考试通过率</span>
            </div>
          </template>
          <div v-if="chartDataLoading" class="chart-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="passRateData.total === 0" class="chart-empty">
            <el-empty description="暂无考试数据" :image-size="80" />
          </div>
          <div v-else ref="passRateChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最近活动和待办事项 -->
    <el-row :gutter="20" class="activity-section">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近培训</span>
              <el-button type="primary" link @click="goToMyTraining">查看更多</el-button>
            </div>
          </template>
          <el-timeline v-if="recentTrainings.length > 0">
            <el-timeline-item
              v-for="item in recentTrainings"
              :key="item.id"
              :timestamp="item.time"
              placement="top"
            >
              <el-card shadow="hover">
                <h4>{{ item.name }}</h4>
                <p>完成进度: {{ item.progress }}%</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无培训记录" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button type="primary" link @click="goToMyExam">查看更多</el-button>
            </div>
          </template>
          <div v-if="todos.length > 0" class="todo-list">
            <div v-for="item in todos" :key="item.id" class="todo-item">
              <el-icon :color="item.type === 'exam' ? '#f56c6c' : '#409eff'">
                <component :is="item.type === 'exam' ? 'Edit' : 'Reading'" />
              </el-icon>
              <span class="todo-text">{{ item.title }}</span>
              <el-tag :type="item.urgent ? 'danger' : 'info'" size="small">
                {{ item.deadline }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无待办事项" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import { Loading, User, Notebook, Calendar, TrendCharts, Edit, Reading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getUserList } from '@/api/user'
import { getCourseList } from '@/api/course'
import { getPlanList, getMyProgress } from '@/api/training'
import { getMyExamRecordList } from '@/api/exam'

const router = useRouter()
const userStore = useUserStore()

const currentDate = dayjs().format('YYYY年MM月DD日 dddd')

// 判断是否为管理员
const isAdmin = computed(() => {
  const roles = userStore.userInfo?.roleNames || []
  return roles.some(role => 
    role === '超级管理员' || 
    role === '系统管理员' || 
    role === '管理员' ||
    role === 'ADMIN' ||
    role === '培训管理员'
  )
})

// 统计数据
const stats = ref({
  courseCount: 0,
  examCount: 0,
  progress: 0
})

const overview = ref({
  userCount: 0,
  courseCount: 0,
  planCount: 0,
  examCount: 0
})

// 最近培训
const recentTrainings = ref<any[]>([])

// 待办事项
const todos = ref<any[]>([])

// 图表引用
const progressChartRef = ref<HTMLElement>()
const passRateChartRef = ref<HTMLElement>()

// 图表实例
let progressChart: echarts.ECharts | null = null
let passRateChart: echarts.ECharts | null = null

// 图表数据加载状态
const chartDataLoading = ref(false)

// 学习进度图表数据
const progressChartData = ref<any[]>([])

// 考试通过率数据
const passRateData = ref({
  passCount: 0,
  failCount: 0,
  total: 0
})

// resize 事件处理函数
const handleResize = () => {
  progressChart?.resize()
  passRateChart?.resize()
}

// 跳转到我的培训页面
const goToMyTraining = () => {
  router.push('/my/progress')
}

// 跳转到我的考试页面
const goToMyExam = () => {
  router.push('/my/exam')
}

// 获取管理员统计数据
const getAdminStatistics = async () => {
  try {
    // 获取用户总数
    const userRes = await getUserList({ current: 1, size: 1 })
    overview.value.userCount = userRes?.total || 0

    // 获取课程总数
    const courseRes = await getCourseList({ current: 1, size: 1, status: 2 })
    overview.value.courseCount = courseRes?.total || 0

    // 获取培训计划总数
    const planRes = await getPlanList({ current: 1, size: 1 })
    overview.value.planCount = planRes?.total || 0

    // 获取考试次数
    const examRes = await getMyExamRecordList({ current: 1, size: 1, status: 2 })
    overview.value.examCount = examRes?.total || 0
  } catch (error: any) {
    console.error('获取管理员统计数据失败:', error)
    // 权限不足时显示提示但不中断
  }
}

// 获取用户个人统计数据
const getUserStatistics = async () => {
  chartDataLoading.value = true
  try {
    // 获取我的待学习课程数和学习进度
    const progressRes = await getMyProgress({ current: 1, size: 100 })
    const progressRecords = progressRes?.records || []
    
    // 计算待学习课程数（状态为进行中）
    stats.value.courseCount = progressRecords.filter((r: any) => r.status === 1).length
    
    // 计算总体学习进度
    if (progressRecords.length > 0) {
      const totalProgress = progressRecords.reduce((sum: number, r: any) => sum + (r.progress || 0), 0)
      stats.value.progress = Math.round(totalProgress / progressRecords.length)
    }

    // 获取我的考试记录
    const myExamRes = await getMyExamRecordList({ current: 1, size: 100 })
    const examRecords = myExamRes?.records || []
    
    // 计算待参加考试数（未开始或进行中）
    stats.value.examCount = examRecords.filter((r: any) => r.status === 0 || r.status === 1).length

    // 设置最近培训数据
    recentTrainings.value = progressRecords
      .sort((a: any, b: any) => {
        const timeA = a.lastStudyTime || a.createTime || ''
        const timeB = b.lastStudyTime || b.createTime || ''
        return timeB.localeCompare(timeA)
      })
      .slice(0, 5)
      .map((item: any) => ({
        id: item.id,
        name: item.courseName || item.planName,
        time: item.lastStudyTime || item.createTime,
        progress: item.progress || 0
      }))

    // 设置待办事项
    const inProgressCourses = progressRecords.filter((r: any) => r.status === 1)
    const pendingExams = examRecords.filter((r: any) => r.status === 0 || r.status === 1)
    
    todos.value = [
      ...inProgressCourses.slice(0, 3).map((item: any) => ({
        id: `course-${item.id}`,
        title: `完成《${item.courseName}》课程学习`,
        type: 'course',
        deadline: '进行中',
        urgent: item.progress > 80
      })),
      ...pendingExams.slice(0, 2).map((item: any) => ({
        id: `exam-${item.id}`,
        title: `参加《${item.paperName}》考试`,
        type: 'exam',
        deadline: '待完成',
        urgent: true
      }))
    ].slice(0, 5)

    // 准备图表数据 - 学习进度分布
    progressChartData.value = progressRecords.map((item: any) => ({
      name: item.courseName || item.planName,
      progress: item.progress || 0
    }))

    // 准备图表数据 - 考试通过率
    const completedExams = examRecords.filter((r: any) => r.status === 2 || r.status === 3)
    passRateData.value = {
      passCount: completedExams.filter((r: any) => r.passed === 1).length,
      failCount: completedExams.filter((r: any) => r.passed !== 1).length,
      total: completedExams.length
    }

  } catch (error: any) {
    console.error('获取用户统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    chartDataLoading.value = false
  }
}

// 获取统计数据
const getStatistics = async () => {
  // 获取用户个人统计数据（所有用户都可以获取）
  await getUserStatistics()
  
  // 如果是管理员，额外获取管理员统计数据
  if (isAdmin.value) {
    await getAdminStatistics()
  }
}

onMounted(async () => {
  await getStatistics()
  // 初始化图表（数据加载完成后）
  initProgressChart()
  initPassRateChart()
  // 添加 resize 事件监听
  window.addEventListener('resize', handleResize)
})

// 初始化学习进度图表 - 使用真实数据
const initProgressChart = () => {
  if (!progressChartRef.value || progressChartData.value.length === 0) return
  progressChart = echarts.init(progressChartRef.value)
  
  // 取前5个课程展示进度
  const displayData = progressChartData.value.slice(0, 5)
  const courseNames = displayData.map(item => 
    item.name.length > 8 ? item.name.substring(0, 8) + '...' : item.name
  )
  const progressValues = displayData.map(item => item.progress)
  
  const option = {
    tooltip: { 
      trigger: 'axis',
      formatter: (params: any) => {
        const data = params[0]
        return `${displayData[data.dataIndex].name}<br/>学习进度: ${data.value}%`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: courseNames,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: { 
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' }
    },
    series: [
      {
        name: '学习进度',
        type: 'bar',
        data: progressValues.map(value => ({
          value,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: value >= 80 ? '#67c23a' : value >= 60 ? '#409eff' : '#e6a23c' },
              { offset: 1, color: value >= 80 ? '#95d475' : value >= 60 ? '#79bbff' : '#eebe77' }
            ])
          }
        })),
        barWidth: '50%',
        label: {
          show: true,
          position: 'top',
          formatter: '{c}%'
        }
      }
    ]
  }
  
  progressChart.setOption(option)
}

// 初始化考试通过率图表 - 使用真实数据
const initPassRateChart = () => {
  if (!passRateChartRef.value || passRateData.value.total === 0) return
  passRateChart = echarts.init(passRateChartRef.value)
  
  const { passCount, failCount } = passRateData.value
  
  const option = {
    tooltip: { 
      trigger: 'item',
      formatter: '{b}: {c}人次 ({d}%)'
    },
    legend: { 
      orient: 'vertical', 
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '考试结果',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'center',
          formatter: () => {
            const rate = passRateData.value.total > 0 
              ? Math.round((passCount / passRateData.value.total) * 100) 
              : 0
            return `{a|通过率}\n{b|${rate}%}`
          },
          rich: {
            a: {
              fontSize: 14,
              color: '#909399',
              lineHeight: 24
            },
            b: {
              fontSize: 28,
              fontWeight: 'bold',
              color: '#67c23a'
            }
          }
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: passCount, name: '通过', itemStyle: { color: '#67c23a' } },
          { value: failCount, name: '未通过', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  }
  
  passRateChart.setOption(option)
}

// 清理资源
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  progressChart?.dispose()
  passRateChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-section {
    margin-bottom: 20px;
    
    .welcome-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      
      .welcome-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        h2 {
          margin: 0 0 10px;
          font-size: 24px;
        }
        
        p {
          margin: 0;
          opacity: 0.8;
        }
        
        .welcome-stats {
          display: flex;
          gap: 40px;
          
          .stat-item {
            text-align: center;
            
            .stat-value {
              display: block;
              font-size: 28px;
              font-weight: bold;
            }
            
            .stat-label {
              font-size: 14px;
              opacity: 0.8;
            }
          }
        }
      }
    }
  }
  
  .stats-section {
    margin-bottom: 20px;
    
    .stat-card {
      :deep(.el-card__body) {
        display: flex;
        align-items: center;
        padding: 20px;
      }
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin-right: 15px;
      }
      
      .stat-info {
        display: flex;
        flex-direction: column;
        
        .stat-number {
          font-size: 28px;
          font-weight: bold;
          color: #333;
        }
        
        .stat-title {
          font-size: 14px;
          color: #999;
        }
      }
    }
  }
  
  .chart-section {
    margin-bottom: 20px;
    
    .chart-container {
      height: 300px;
    }
    
    .chart-loading,
    .chart-empty {
      height: 300px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;
      
      .el-icon {
        font-size: 32px;
        margin-bottom: 10px;
      }
    }
    
    .chart-loading {
      .el-icon {
        animation: rotating 1.5s linear infinite;
      }
    }
  }
  
  .activity-section {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .todo-list {
      .todo-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #eee;
        
        &:last-child {
          border-bottom: none;
        }
        
        .el-icon {
          margin-right: 10px;
        }
        
        .todo-text {
          flex: 1;
        }
      }
    }
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
