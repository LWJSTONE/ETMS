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
    
    <!-- 数据统计卡片 -->
    <el-row :gutter="20" class="stats-section">
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
              <span>培训进度统计</span>
            </div>
          </template>
          <div ref="progressChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>考试通过率</span>
            </div>
          </template>
          <div ref="passRateChartRef" class="chart-container"></div>
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
              <el-button type="primary" link>查看更多</el-button>
            </div>
          </template>
          <el-timeline>
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
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button type="primary" link>查看更多</el-button>
            </div>
          </template>
          <div class="todo-list">
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
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getUserList } from '@/api/user'
import { getCourseList } from '@/api/course'
import { getPlanList, getMyProgress } from '@/api/training'
import { getPaperList, getExamRecordList } from '@/api/exam'

const userStore = useUserStore()

const currentDate = dayjs().format('YYYY年MM月DD日 dddd')

// 统计数据
const stats = ref({
  courseCount: 0,
  examCount: 0,
  progress: 68
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

// 获取统计数据
const getStatistics = async () => {
  try {
    // 获取用户总数
    const userRes = await getUserList({ current: 1, size: 1 })
    overview.value.userCount = userRes.data?.total || 0

    // 获取课程总数
    const courseRes = await getCourseList({ current: 1, size: 1, status: 2 })
    overview.value.courseCount = courseRes.data?.total || 0

    // 获取培训计划总数
    const planRes = await getPlanList({ current: 1, size: 1 })
    overview.value.planCount = planRes.data?.total || 0

    // 获取考试记录总数
    const examRes = await getExamRecordList({ current: 1, size: 1, status: 2 })
    overview.value.examCount = examRes.data?.total || 0

    // 获取我的待学习课程数
    const progressRes = await getMyProgress({ current: 1, size: 100, status: 1 })
    stats.value.courseCount = progressRes.data?.records?.filter((r: any) => r.status === 1).length || 0

    // 获取待参加考试数
    const paperRes = await getPaperList({ current: 1, size: 100, status: 1 })
    stats.value.examCount = paperRes.data?.records?.length || 0

    // 设置最近培训数据
    if (progressRes.data?.records) {
      recentTrainings.value = progressRes.data.records.slice(0, 5).map((item: any) => ({
        id: item.id,
        name: item.courseName || item.planName,
        time: item.lastStudyTime || item.createTime,
        progress: item.progress || 0
      }))
    }

    // 设置待办事项
    const inProgressCourses = progressRes.data?.records?.filter((r: any) => r.status === 1) || []
    todos.value = inProgressCourses.slice(0, 5).map((item: any) => ({
      id: item.id,
      title: `完成《${item.courseName}》课程学习`,
      type: 'course',
      deadline: '进行中',
      urgent: item.progress > 80
    }))
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

onMounted(() => {
  getStatistics()
  initProgressChart()
  initPassRateChart()
})

// 初始化培训进度图表
const initProgressChart = () => {
  if (!progressChartRef.value) return
  const chart = echarts.init(progressChartRef.value)
  
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['计划人数', '完成人数'] },
    xAxis: {
      type: 'category',
      data: ['技术部', '市场部', '人事部', '财务部', '运营部']
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '计划人数',
        type: 'bar',
        data: [30, 25, 15, 12, 20],
        itemStyle: { color: '#409eff' }
      },
      {
        name: '完成人数',
        type: 'bar',
        data: [28, 22, 13, 10, 18],
        itemStyle: { color: '#67c23a' }
      }
    ]
  }
  
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

// 初始化考试通过率图表
const initPassRateChart = () => {
  if (!passRateChartRef.value) return
  const chart = echarts.init(passRateChartRef.value)
  
  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        name: '考试通过率',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        data: [
          { value: 85, name: '通过', itemStyle: { color: '#67c23a' } },
          { value: 15, name: '未通过', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  }
  
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}
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
</style>
