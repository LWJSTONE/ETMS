<template>
  <div class="learning-page">
    <!-- 课程信息头部 -->
    <el-card shadow="never" class="course-header-card">
      <div class="course-header">
        <div class="course-cover">
          <img :src="courseInfo.coverImage || defaultCover" :alt="courseInfo.courseName" />
        </div>
        <div class="course-info">
          <h2 class="course-title">{{ courseInfo.courseName || '加载中...' }}</h2>
          <p class="course-desc">{{ courseInfo.description || courseInfo.courseDesc || '暂无描述' }}</p>
          <div class="course-meta">
            <span class="meta-item">
              <el-icon><Timer /></el-icon>
              时长: {{ courseInfo.duration || 0 }}分钟
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              学分: {{ courseInfo.credit || 0 }}
            </span>
            <span class="meta-item">
              <el-icon><DataLine /></el-icon>
              进度: {{ progress }}%
            </span>
          </div>
          <div class="progress-bar">
            <el-progress :percentage="progress" :stroke-width="10" :show-text="false" />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 学习内容区域 -->
    <el-card shadow="never" class="learning-content-card">
      <template #header>
        <div class="content-header">
          <span>课程内容</span>
          <div class="learning-actions">
            <el-button type="primary" @click="handleCompleteLearning" :loading="completing">
              <el-icon><CircleCheck /></el-icon>
              标记完成
            </el-button>
            <el-button @click="handleBack">
              <el-icon><Back /></el-icon>
              返回课程列表
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="learning-content">
        <!-- 视频内容 -->
        <div v-if="courseInfo.courseType === 1 || courseInfo.videoUrl" class="video-section">
          <div class="video-container">
            <video
              v-if="courseInfo.videoUrl"
              ref="videoRef"
              :src="courseInfo.videoUrl"
              controls
              class="video-player"
              @timeupdate="handleVideoTimeUpdate"
              @ended="handleVideoEnded"
            />
            <el-empty v-else description="暂无视频内容">
              <el-button type="primary" @click="handleCompleteLearning">标记已学习</el-button>
            </el-empty>
          </div>
        </div>

        <!-- 文档内容 -->
        <div v-else-if="courseInfo.courseType === 2 || courseInfo.documentUrl" class="document-section">
          <div v-if="courseInfo.documentUrl" class="document-container">
            <iframe
              :src="courseInfo.documentUrl"
              class="document-viewer"
              frameborder="0"
            />
          </div>
          <el-empty v-else description="暂无文档内容">
            <el-button type="primary" @click="handleCompleteLearning">标记已学习</el-button>
          </el-empty>
        </div>

        <!-- 默认内容展示 -->
        <div v-else class="default-content">
          <el-empty description="课程内容加载中...">
            <el-button type="primary" @click="handleCompleteLearning">标记已学习</el-button>
          </el-empty>
        </div>

        <!-- 课程目标 -->
        <div v-if="courseInfo.courseObjective" class="course-objective">
          <h4>课程目标</h4>
          <p>{{ courseInfo.courseObjective }}</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Timer, Star, DataLine, CircleCheck, Back } from '@element-plus/icons-vue'
import { getMyProgress, updateProgress } from '@/api/training'
import { getCourseDetail } from '@/api/course'

const route = useRoute()
const router = useRouter()

// 默认封面
const defaultCover = 'https://via.placeholder.com/300x180?text=Course'

// 状态
const loading = ref(false)
const completing = ref(false)
const progress = ref(0)
const videoRef = ref<HTMLVideoElement>()

// 防抖定时器
let saveProgressTimer: ReturnType<typeof setTimeout> | null = null
const DEBOUNCE_DELAY = 3000 // 3秒防抖延迟

// 课程信息
const courseInfo = reactive({
  id: 0,
  courseId: 0,
  planId: 0,
  courseName: '',
  description: '',
  courseDesc: '',
  courseObjective: '',
  coverImage: '',
  videoUrl: '',
  documentUrl: '',
  duration: 0,
  credit: 0,
  courseType: 1
})

// 进度ID和计划ID
const progressId = computed(() => route.query.progressId ? Number(route.query.progressId) : null)
const courseId = computed(() => route.query.courseId ? Number(route.query.courseId) : null)
const planId = computed(() => route.query.planId ? Number(route.query.planId) : null)

// 获取课程信息
const fetchCourseInfo = async () => {
  loading.value = true
  try {
    // 获取课程详情
    if (courseId.value) {
      const res = await getCourseDetail(courseId.value)
      Object.assign(courseInfo, {
        id: res.data.id,
        courseId: res.data.id,
        courseName: res.data.courseName,
        description: res.data.courseDesc,
        courseDesc: res.data.courseDesc,
        courseObjective: res.data.courseObjective,
        coverImage: res.data.coverImage,
        videoUrl: res.data.videoUrl,
        documentUrl: res.data.documentUrl,
        duration: res.data.duration,
        credit: res.data.credit,
        courseType: res.data.courseType || 1
      })
    }

    // 获取学习进度
    if (progressId.value) {
      const progressRes = await getMyProgress({ current: 1, size: 1000 })
      const progressData = progressRes.data?.records?.find((r: any) => r.id === progressId.value)
      if (progressData) {
        progress.value = progressData.progress || 0
        // 从进度数据中获取 planId
        if (progressData.planId) {
          courseInfo.planId = progressData.planId
        }
      }
    }
  } catch (error) {
    console.error('获取课程信息失败:', error)
    ElMessage.error('获取课程信息失败')
  } finally {
    loading.value = false
  }
}

// 视频时间更新（带防抖）
const handleVideoTimeUpdate = () => {
  if (videoRef.value) {
    const currentTime = videoRef.value.currentTime
    const duration = videoRef.value.duration
    if (duration > 0) {
      const currentProgress = Math.min(Math.round((currentTime / duration) * 100), 100)
      if (currentProgress > progress.value) {
        progress.value = currentProgress
        // 使用防抖保存进度
        debouncedSaveProgress(currentProgress)
      }
    }
  }
}

// 视频播放结束
const handleVideoEnded = () => {
  progress.value = 100
  // 立即保存完成进度（不用防抖）
  saveProgressImmediate(100)
  ElMessage.success('视频学习完成！')
}

// 防抖保存进度
const debouncedSaveProgress = (newProgress: number) => {
  // 清除之前的定时器
  if (saveProgressTimer) {
    clearTimeout(saveProgressTimer)
  }
  // 设置新的定时器
  saveProgressTimer = setTimeout(() => {
    saveProgressImmediate(newProgress)
    saveProgressTimer = null
  }, DEBOUNCE_DELAY)
}

// 立即保存进度
const saveProgressImmediate = async (newProgress: number) => {
  if (!progressId.value) return
  
  try {
    await updateProgress({
      planId: planId.value || courseInfo.planId || 0,
      courseId: courseId.value || 0,
      progress: newProgress
    })
  } catch (error) {
    console.warn('保存进度失败:', error)
  }
}

// 保存进度（保留兼容性）
const saveProgress = debouncedSaveProgress

// 标记完成
const handleCompleteLearning = async () => {
  if (completing.value) return
  
  completing.value = true
  try {
    progress.value = 100
    // 立即保存完成进度（使用 saveProgressImmediate 而不是防抖函数）
    await saveProgressImmediate(100)
    ElMessage.success('学习完成！')
    
    // 返回课程列表
    setTimeout(() => {
      router.push('/my/course')
    }, 1000)
  } catch (error) {
    console.error('标记完成失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    completing.value = false
  }
}

// 返回课程列表
const handleBack = () => {
  router.push('/my/course')
}

// 页面离开时保存进度
onUnmounted(() => {
  // 清除防抖定时器
  if (saveProgressTimer) {
    clearTimeout(saveProgressTimer)
    saveProgressTimer = null
  }
  // 立即保存当前进度
  if (progress.value > 0 && progressId.value) {
    saveProgressImmediate(progress.value)
  }
})

onMounted(() => {
  fetchCourseInfo()
})
</script>

<style lang="scss" scoped>
.learning-page {
  .course-header-card {
    margin-bottom: 20px;

    .course-header {
      display: flex;
      gap: 20px;

      @media screen and (max-width: 768px) {
        flex-direction: column;
      }

      .course-cover {
        width: 280px;
        flex-shrink: 0;

        @media screen and (max-width: 768px) {
          width: 100%;
        }

        img {
          width: 100%;
          height: 160px;
          object-fit: cover;
          border-radius: 8px;
        }
      }

      .course-info {
        flex: 1;

        .course-title {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 10px;
        }

        .course-desc {
          font-size: 14px;
          color: #606266;
          margin: 0 0 15px;
          line-height: 1.6;
        }

        .course-meta {
          display: flex;
          gap: 20px;
          margin-bottom: 15px;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 14px;
            color: #909399;
          }
        }

        .progress-bar {
          max-width: 400px;
        }
      }
    }
  }

  .learning-content-card {
    .content-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .learning-actions {
        display: flex;
        gap: 10px;
      }
    }

    .learning-content {
      min-height: 400px;

      .video-section {
        .video-container {
          background: #000;
          border-radius: 8px;
          overflow: hidden;

          .video-player {
            width: 100%;
            max-height: 500px;
          }
        }
      }

      .document-section {
        .document-container {
          border-radius: 8px;
          overflow: hidden;
          border: 1px solid #ebeef5;

          .document-viewer {
            width: 100%;
            height: 600px;
          }
        }
      }

      .course-objective {
        margin-top: 24px;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 8px;

        h4 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 10px;
        }

        p {
          font-size: 14px;
          color: #606266;
          margin: 0;
          line-height: 1.6;
        }
      }
    }
  }
}
</style>
