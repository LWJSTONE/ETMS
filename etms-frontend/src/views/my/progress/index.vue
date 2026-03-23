<template>
  <div class="my-progress">
    <!-- 学习统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card stat-total">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="28"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.totalCourses }}</span>
              <span class="stat-label">总课程数</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card stat-completed">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.completedCourses }}</span>
              <span class="stat-label">已完成</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card stat-progress">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="28"><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.inProgressCourses }}</span>
              <span class="stat-label">进行中</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card stat-duration">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="28"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ formatDuration(stats.totalDuration) }}</span>
              <span class="stat-label">总学习时长</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索和筛选区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入课程名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="培训计划">
          <el-select
            v-model="searchForm.planId"
            placeholder="全部计划"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="plan in planList"
              :key="plan.id"
              :label="plan.planName"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="完成状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 进度列表 -->
    <el-card shadow="never" class="progress-card">
      <template #header>
        <div class="card-header">
          <span>我的学习进度</span>
          <span class="progress-count">共 {{ pagination.total }} 条记录</span>
        </div>
      </template>

      <div v-loading="loading" class="progress-list">
        <template v-if="progressList.length > 0">
          <div
            v-for="item in progressList"
            :key="item.id"
            class="progress-item"
          >
            <div class="progress-main">
              <!-- 课程信息 -->
              <div class="course-info">
                <div class="course-header">
                  <h3 class="course-name">{{ item.courseName }}</h3>
                  <el-tag :type="getStatusType(item.status)" size="small">
                    {{ getStatusName(item.status) }}
                  </el-tag>
                </div>
                <div class="course-meta">
                  <span class="meta-item">
                    <el-icon><Collection /></el-icon>
                    {{ item.planName || '未分配培训计划' }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Timer /></el-icon>
                    学习时长: {{ formatDuration(item.duration) }}
                  </span>
                  <span v-if="item.lastStudyTime" class="meta-item">
                    <el-icon><Clock /></el-icon>
                    最后学习: {{ item.lastStudyTime }}
                  </span>
                </div>
              </div>

              <!-- 进度条 -->
              <div class="progress-section">
                <div class="progress-header">
                  <span>学习进度</span>
                  <span class="progress-value" :class="getProgressClass(item.progress)">
                    {{ item.progress || 0 }}%
                  </span>
                </div>
                <el-progress
                  :percentage="item.progress || 0"
                  :stroke-width="10"
                  :show-text="false"
                  :color="getProgressColor(item.progress, item.status)"
                />
              </div>

              <!-- 操作按钮 -->
              <div class="progress-actions">
                <el-button
                  :type="item.status === 2 ? 'default' : 'primary'"
                  size="small"
                  @click="handleContinueLearning(item)"
                >
                  <el-icon>
                    <component :is="item.status === 0 ? 'VideoPlay' : item.status === 1 ? 'VideoPlay' : 'RefreshRight'" />
                  </el-icon>
                  {{ getActionText(item.status) }}
                </el-button>
              </div>
            </div>
          </div>
        </template>

        <!-- 空状态 -->
        <el-empty v-else description="暂无学习进度数据">
          <el-button type="primary" @click="handleReset">刷新</el-button>
        </el-empty>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="pagination.total > 0"
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="getProgressList"
        @current-change="getProgressList"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Reading, 
  CircleCheck, 
  Loading, 
  Timer, 
  Collection, 
  Clock, 
  Search, 
  Refresh,
  VideoPlay,
  RefreshRight
} from '@element-plus/icons-vue'
import { getMyProgress, getPlanList } from '@/api/training'

const router = useRouter()

// 定义进度数据类型
interface ProgressItem {
  id: number
  planId: number
  planName: string
  courseId: number
  courseName: string
  progress: number
  duration: number
  status: number
  lastStudyTime: string
  createTime: string
}

// 统计数据
const stats = ref({
  totalCourses: 0,
  completedCourses: 0,
  inProgressCourses: 0,
  totalDuration: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  planId: null as number | null,
  status: null as number | null
})

// 进度列表
const progressList = ref<ProgressItem[]>([])
const loading = ref(false)

// 培训计划列表
const planList = ref<any[]>([])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success'
  }
  return types[status] || 'info'
}

// 获取状态名称
const getStatusName = (status: number) => {
  const names: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已完成'
  }
  return names[status] || '未知'
}

// 获取操作按钮文本
const getActionText = (status: number) => {
  const texts: Record<number, string> = {
    0: '开始学习',
    1: '继续学习',
    2: '再次学习'
  }
  return texts[status] || '开始学习'
}

// 获取进度条颜色
const getProgressColor = (progress: number, status: number) => {
  if (status === 2 || progress >= 100) return '#67c23a'
  if (progress >= 50) return '#409eff'
  if (progress >= 20) return '#e6a23c'
  return '#909399'
}

// 获取进度值样式类
const getProgressClass = (progress: number) => {
  if (progress >= 100) return 'completed'
  if (progress >= 50) return 'halfway'
  return ''
}

// 格式化学习时长
const formatDuration = (minutes: number) => {
  if (!minutes || minutes === 0) return '0分钟'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${mins}分钟`
  }
}

// 获取进度列表
const getProgressList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword,
      planId: searchForm.planId,
      status: searchForm.status
    }

    const res = await getMyProgress(params)
    
    if (res.data?.records) {
      progressList.value = res.data.records.map((item: any) => ({
        ...item,
        courseName: item.courseName || item.course?.courseName || '未知课程',
        planName: item.planName || item.trainingPlan?.planName || '',
        progress: item.progress ?? 0,
        duration: item.duration ?? 0,
        status: item.status ?? 0
      }))
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取进度列表失败:', error)
    ElMessage.error('获取学习进度失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStats = async () => {
  try {
    // 获取所有进度数据用于统计
    const res = await getMyProgress({ current: 1, size: 1000 })
    
    if (res.data?.records) {
      const records = res.data.records
      stats.value = {
        totalCourses: records.length,
        completedCourses: records.filter((r: any) => r.status === 2).length,
        inProgressCourses: records.filter((r: any) => r.status === 1).length,
        totalDuration: records.reduce((sum: number, r: any) => sum + (r.duration || 0), 0)
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取培训计划列表
const getPlanOptions = async () => {
  try {
    const res = await getPlanList({ current: 1, size: 1000, status: 1 })
    planList.value = res.data?.records || []
  } catch (error) {
    console.error('获取培训计划列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getProgressList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    planId: null,
    status: null
  })
  pagination.current = 1
  getProgressList()
}

// 继续学习
const handleContinueLearning = (item: ProgressItem) => {
  // 跳转到学习页面
  router.push({
    path: '/my/learning',
    query: {
      courseId: item.courseId,
      progressId: item.id
    }
  })
  
  ElMessage.info(`即将进入课程：${item.courseName}`)
}

// 初始化
onMounted(() => {
  getProgressList()
  getStats()
  getPlanOptions()
})
</script>

<style lang="scss" scoped>
.my-progress {
  // 统计卡片
  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      border-radius: 8px;
      overflow: hidden;
      cursor: default;

      :deep(.el-card__body) {
        padding: 16px 20px;
      }

      .stat-content {
        display: flex;
        align-items: center;

        .stat-icon {
          width: 50px;
          height: 50px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 15px;
          color: #fff;
        }

        .stat-info {
          display: flex;
          flex-direction: column;

          .stat-value {
            font-size: 22px;
            font-weight: 600;
            color: #333;
          }

          .stat-label {
            font-size: 13px;
            color: #999;
            margin-top: 2px;
          }
        }
      }

      &.stat-total .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.stat-completed .stat-icon {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }

      &.stat-progress .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.stat-duration .stat-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
    }
  }

  // 搜索卡片
  .search-card {
    margin-bottom: 20px;
  }

  // 进度卡片
  .progress-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .progress-count {
        font-size: 14px;
        color: #909399;
      }
    }

    .progress-list {
      min-height: 300px;
    }

    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }

  // 进度项
  .progress-item {
    padding: 20px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    margin-bottom: 16px;
    transition: all 0.3s ease;
    background: #fff;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.1);
    }

    .progress-main {
      display: flex;
      align-items: center;
      gap: 20px;

      @media screen and (max-width: 768px) {
        flex-direction: column;
        align-items: stretch;
      }
    }

    .course-info {
      flex: 1;
      min-width: 0;

      .course-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;

        .course-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }

      .course-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #909399;

          .el-icon {
            font-size: 14px;
          }
        }
      }
    }

    .progress-section {
      width: 250px;
      flex-shrink: 0;

      @media screen and (max-width: 768px) {
        width: 100%;
      }

      .progress-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        font-size: 13px;
        color: #606266;

        .progress-value {
          font-weight: 600;
          color: #409eff;

          &.completed {
            color: #67c23a;
          }

          &.halfway {
            color: #e6a23c;
          }
        }
      }
    }

    .progress-actions {
      flex-shrink: 0;

      @media screen and (max-width: 768px) {
        align-self: flex-end;
        margin-top: 12px;
      }
    }
  }

  // 响应式适配 - 移动端样式
  @media screen and (max-width: 768px) {
    .stats-row {
      .stat-card {
        margin-bottom: 10px;

        .stat-content {
          .stat-icon {
            width: 40px;
            height: 40px;
          }

          .stat-info {
            .stat-value {
              font-size: 18px;
            }
          }
        }
      }
    }

    .progress-item {
      padding: 16px;
    }
  }
}
</style>
