<template>
  <div class="my-course">
    <!-- 搜索和筛选区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入课程名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="学习状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="未开始" :value="0" />
            <el-option label="学习中" :value="1" />
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

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card stat-total">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="28"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.total }}</span>
              <span class="stat-label">全部课程</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6">
        <el-card shadow="hover" class="stat-card stat-pending">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats.notStarted }}</span>
              <span class="stat-label">未开始</span>
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
              <span class="stat-value">{{ stats.inProgress }}</span>
              <span class="stat-label">学习中</span>
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
              <span class="stat-value">{{ stats.completed }}</span>
              <span class="stat-label">已完成</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 课程卡片列表 -->
    <el-card shadow="never" class="course-list-card">
      <template #header>
        <div class="card-header">
          <span>我的课程</span>
          <span class="course-count">共 {{ pagination.total }} 门课程</span>
        </div>
      </template>

      <div v-loading="loading" class="course-list">
        <template v-if="courseList.length > 0">
          <el-row :gutter="20">
            <el-col
              v-for="course in courseList"
              :key="course.id"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
            >
              <el-card
                shadow="hover"
                class="course-card"
                :body-style="{ padding: 0 }"
                @click="handleStartLearning(course)"
              >
                <!-- 课程封面 -->
                <div class="course-cover">
                  <img
                    :src="course.coverImage || defaultCover"
                    :alt="course.courseName"
                    @error="handleImageError"
                  />
                  <div class="course-type-tag">
                    <el-tag :type="getCourseTypeTag(course.courseType)" size="small">
                      {{ getCourseTypeText(course.courseType) }}
                    </el-tag>
                  </div>
                  <div class="course-status-tag">
                    <el-tag :type="getStatusTag(course.learnStatus)" size="small">
                      {{ getStatusText(course.learnStatus) }}
                    </el-tag>
                  </div>
                </div>

                <!-- 课程信息 -->
                <div class="course-info">
                  <h3 class="course-name" :title="course.courseName">{{ course.courseName }}</h3>
                  <p class="course-desc" :title="course.description">{{ course.description || '暂无描述' }}</p>

                  <div class="course-meta">
                    <span class="meta-item">
                      <el-icon><Timer /></el-icon>
                      {{ course.duration || 0 }}分钟
                    </span>
                    <span class="meta-item">
                      <el-icon><Star /></el-icon>
                      {{ course.credit || 0 }}学分
                    </span>
                  </div>

                  <!-- 学习进度 -->
                  <div class="progress-section">
                    <div class="progress-header">
                      <span>学习进度</span>
                      <span class="progress-value">{{ course.progress || 0 }}%</span>
                    </div>
                    <el-progress
                      :percentage="course.progress || 0"
                      :stroke-width="8"
                      :show-text="false"
                      :color="getProgressColor(course.progress)"
                    />
                  </div>

                  <!-- 操作按钮 -->
                  <div class="course-actions">
                    <el-button
                      :type="course.learnStatus === 2 ? 'default' : 'primary'"
                      size="small"
                      @click.stop="handleStartLearning(course)"
                    >
                      <el-icon>
                        <component :is="course.learnStatus === 0 ? 'VideoPlay' : course.learnStatus === 1 ? 'VideoPlay' : 'View'" />
                      </el-icon>
                      {{ getActionText(course.learnStatus) }}
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>

        <!-- 空状态 -->
        <el-empty v-else description="暂无课程数据">
          <el-button type="primary" @click="handleReset">刷新</el-button>
        </el-empty>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="pagination.total > 0"
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[8, 12, 16, 24]"
        layout="total, sizes, prev, pager, next"
        background
        @size-change="getCourseListData"
        @current-change="getCourseListData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Reading, 
  Clock, 
  Loading, 
  CircleCheck, 
  Timer, 
  Star,
  VideoPlay,
  View
} from '@element-plus/icons-vue'
import { getMyProgress } from '@/api/training'
import { getCourseList as getCourseListApi } from '@/api/course'

const router = useRouter()

// 默认封面图片
const defaultCover = 'https://via.placeholder.com/300x180?text=Course'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null as number | null
})

// 统计数据
const stats = ref({
  total: 0,
  notStarted: 0,
  inProgress: 0,
  completed: 0
})

// 课程列表
const courseList = ref<any[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  size: 8,
  total: 0
})

// 获取课程类型标签
const getCourseTypeTag = (type: number) => {
  const map: Record<number, string> = { 1: 'primary', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

// 获取课程类型文本
const getCourseTypeText = (type: number) => {
  const map: Record<number, string> = { 1: '视频', 2: '文档', 3: '直播' }
  return map[type] || '其他'
}

// 获取学习状态标签
const getStatusTag = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[status] || 'info'
}

// 获取学习状态文本
const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '未开始', 1: '学习中', 2: '已完成' }
  return map[status] || '未知'
}

// 获取操作按钮文本
const getActionText = (status: number) => {
  const map: Record<number, string> = { 0: '开始学习', 1: '继续学习', 2: '再次学习' }
  return map[status] || '开始学习'
}

// 获取进度条颜色
const getProgressColor = (progress: number) => {
  if (progress >= 100) return '#67c23a'
  if (progress >= 50) return '#409eff'
  if (progress >= 20) return '#e6a23c'
  return '#909399'
}

// 图片加载错误处理
const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.src = defaultCover
}

// 获取课程列表
const getCourseListData = async () => {
  loading.value = true
  try {
    // 获取我的学习进度
    const progressRes = await getMyProgress({
      current: pagination.current,
      size: pagination.size,
      keyword: searchForm.keyword,
      status: searchForm.status
    })

    if (progressRes.data?.records) {
      courseList.value = progressRes.data.records.map((item: any) => ({
        ...item,
        courseName: item.courseName || item.course?.courseName,
        courseType: item.courseType || item.course?.courseType,
        coverImage: item.coverImage || item.course?.coverImage,
        description: item.description || item.course?.description,
        duration: item.duration || item.course?.duration,
        credit: item.credit || item.course?.credit,
        learnStatus: item.status ?? 0,
        progress: item.progress ?? 0
      }))
      pagination.total = progressRes.data.total || 0
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    // 如果进度接口失败，尝试获取课程列表
    try {
      const courseRes = await getCourseListApi({
        current: pagination.current,
        size: pagination.size,
        courseName: searchForm.keyword,
        status: 2 // 已上架的课程
      })

      if (courseRes.data?.records) {
        courseList.value = courseRes.data.records.map((item: any) => ({
          ...item,
          courseId: item.id, // 确保courseId正确设置
          learnStatus: 0,
          progress: 0
        }))
        pagination.total = courseRes.data.total || 0
      }
    } catch (err) {
      console.error('获取课程列表失败:', err)
      ElMessage.error('获取课程列表失败')
    }
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStats = async () => {
  try {
    const res = await getMyProgress({ current: 1, size: 1000 })
    if (res.data?.records) {
      const records = res.data.records
      stats.value = {
        total: records.length,
        notStarted: records.filter((r: any) => r.status === 0).length,
        inProgress: records.filter((r: any) => r.status === 1).length,
        completed: records.filter((r: any) => r.status === 2).length
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getCourseListData()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pagination.current = 1
  getCourseListData()
}

// 开始学习
const handleStartLearning = (course: any) => {
  // 跳转到学习页面
  router.push({
    path: '/my/learning',
    query: {
      courseId: course.courseId || course.id,
      progressId: course.id,
      planId: course.planId
    }
  })
}

onMounted(() => {
  getCourseListData()
  getStats()
})
</script>

<style lang="scss" scoped>
.my-course {
  .search-card {
    margin-bottom: 20px;
  }

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
            font-size: 24px;
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

      &.stat-pending .stat-icon {
        background: linear-gradient(135deg, #909399 0%, #b4b7bd 100%);
      }

      &.stat-progress .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.stat-completed .stat-icon {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }
  }

  .course-list-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .course-count {
        font-size: 14px;
        color: #909399;
      }
    }

    .course-list {
      min-height: 300px;
      margin-bottom: 20px;
    }

    .el-pagination {
      justify-content: flex-end;
    }
  }

  .course-card {
    margin-bottom: 20px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
    }

    .course-cover {
      position: relative;
      width: 100%;
      height: 150px;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
      }

      &:hover img {
        transform: scale(1.05);
      }

      .course-type-tag {
        position: absolute;
        top: 10px;
        left: 10px;
      }

      .course-status-tag {
        position: absolute;
        top: 10px;
        right: 10px;
      }
    }

    .course-info {
      padding: 16px;

      .course-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .course-desc {
        font-size: 13px;
        color: #909399;
        margin: 0 0 12px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
        line-height: 1.5;
        height: 39px;
      }

      .course-meta {
        display: flex;
        gap: 16px;
        margin-bottom: 12px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #909399;

          .el-icon {
            font-size: 14px;
          }
        }
      }

      .progress-section {
        margin-bottom: 12px;

        .progress-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 6px;
          font-size: 12px;
          color: #606266;

          .progress-value {
            font-weight: 600;
            color: #409eff;
          }
        }
      }

      .course-actions {
        display: flex;
        justify-content: flex-end;

        .el-button {
          width: 100%;
        }
      }
    }
  }
}

// 响应式适配
@media screen and (max-width: 768px) {
  .my-course {
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
              font-size: 20px;
            }
          }
        }
      }
    }

    .course-card {
      .course-info {
        .course-name {
          font-size: 14px;
        }

        .course-desc {
          font-size: 12px;
        }
      }
    }
  }
}
</style>
