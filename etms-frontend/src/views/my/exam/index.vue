<template>
  <div class="my-exam">
    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" class="exam-tabs">
      <!-- 可参加的考试 -->
      <el-tab-pane label="可参加的考试" name="available">
        <div class="exam-cards" v-loading="availableLoading">
          <template v-if="availableExams.length > 0">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="exam in availableExams" :key="exam.id">
                <el-card shadow="hover" class="exam-card">
                  <div class="exam-card-header">
                    <el-tag :type="getExamStatusType(exam)" effect="dark" size="small">
                      {{ getExamStatusText(exam) }}
                    </el-tag>
                    <span class="exam-code">{{ exam.paperCode }}</span>
                  </div>
                  <h3 class="exam-title">{{ exam.paperName }}</h3>
                  <div class="exam-info">
                    <div class="info-item">
                      <el-icon><Timer /></el-icon>
                      <span>时长: {{ exam.duration }}分钟</span>
                    </div>
                    <div class="info-item">
                      <el-icon><DataLine /></el-icon>
                      <span>总分: {{ exam.totalScore }}分</span>
                    </div>
                    <div class="info-item">
                      <el-icon><CircleCheck /></el-icon>
                      <span>及格: {{ exam.passScore }}分</span>
                    </div>
                    <div class="info-item">
                      <el-icon><Document /></el-icon>
                      <span>题数: {{ exam.questionCount }}道</span>
                    </div>
                  </div>
                  <div class="exam-time" v-if="exam.startTime && exam.endTime">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatDateTime(exam.startTime) }} 至 {{ formatDateTime(exam.endTime) }}</span>
                  </div>
                  <div class="exam-actions">
                    <el-button
                      type="primary"
                      :disabled="!canStartExam(exam)"
                      @click="handleStartExam(exam)"
                      :loading="exam.starting"
                    >
                      {{ getStartButtonText(exam) }}
                    </el-button>
                    <el-button @click="handleViewPaper(exam)">查看详情</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </template>
          <el-empty v-else description="暂无可参加的考试" />
        </div>
      </el-tab-pane>

      <!-- 考试历史记录 -->
      <el-tab-pane label="考试历史" name="history">
        <el-card shadow="never" class="history-card">
          <el-table :data="historyRecords" v-loading="historyLoading" stripe border>
            <el-table-column prop="paperName" label="试卷名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="paperCode" label="试卷编码" width="120" />
            <el-table-column label="考试状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getRecordStatusType(row.status)">
                  {{ getRecordStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="userScore" label="得分" width="80" align="center">
              <template #default="{ row }">
                <span :class="{ 'score-pass': row.userScore >= row.passScore, 'score-fail': row.userScore < row.passScore }">
                  {{ row.userScore ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总分" width="80" align="center" />
            <el-table-column label="是否通过" width="90" align="center">
              <template #default="{ row }">
                <template v-if="row.status >= 3">
                  <el-tag :type="(row.userScore ?? 0) >= (row.passScore ?? 0) ? 'success' : 'danger'" size="small">
                    {{ (row.userScore ?? 0) >= (row.passScore ?? 0) ? '通过' : '未通过' }}
                  </el-tag>
                </template>
                <span v-else class="text-gray">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.submitTime) || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="durationUsed" label="用时" width="100" align="center">
              <template #default="{ row }">
                {{ formatDuration(row.durationUsed) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleViewResult(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="historyPagination.current"
            v-model:page-size="historyPagination.size"
            :total="historyPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="getHistoryRecords"
            @current-change="getHistoryRecords"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 试卷详情对话框 -->
    <el-dialog
      v-model="paperDetailVisible"
      title="试卷详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="试卷名称" :span="2">{{ currentPaper.paperName }}</el-descriptions-item>
        <el-descriptions-item label="试卷编码">{{ currentPaper.paperCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getExamStatusType(currentPaper)" effect="dark">
            {{ getExamStatusText(currentPaper) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总分">{{ currentPaper.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="及格分数">{{ currentPaper.passScore }}分</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ currentPaper.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ currentPaper.questionCount }}道</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentPaper.startTime) || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(currentPaper.endTime) || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="试卷说明" :span="2">
          {{ currentPaper.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 考试结果详情对话框 -->
    <el-dialog
      v-model="resultDetailVisible"
      title="考试结果详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="试卷名称" :span="2">{{ currentResult.paperName }}</el-descriptions-item>
        <el-descriptions-item label="试卷编码">{{ currentResult.paperCode }}</el-descriptions-item>
        <el-descriptions-item label="考试状态">
          <el-tag :type="getRecordStatusType(currentResult.status)">
            {{ getRecordStatusText(currentResult.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="得分">
          <span :class="{ 'score-pass': currentResult.userScore >= currentResult.passScore, 'score-fail': currentResult.userScore < currentResult.passScore }">
            {{ currentResult.userScore ?? '-' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="总分">{{ currentResult.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="及格分数">{{ currentResult.passScore }}分</el-descriptions-item>
        <el-descriptions-item label="是否通过">
          <el-tag v-if="currentResult.status >= 3" :type="currentResult.userScore >= currentResult.passScore ? 'success' : 'danger'">
            {{ currentResult.userScore >= currentResult.passScore ? '通过' : '未通过' }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="正确题数">{{ currentResult.correctCount ?? '-' }}道</el-descriptions-item>
        <el-descriptions-item label="总题数">{{ currentResult.totalCount ?? '-' }}道</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(currentResult.startTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(currentResult.submitTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用时">{{ formatDuration(currentResult.durationUsed) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, DataLine, CircleCheck, Document, Clock } from '@element-plus/icons-vue'
import { getAvailableExams, getMyExamRecordList, startExam, getExamRecordDetail } from '@/api/exam'

const router = useRouter()

// 标签页
const activeTab = ref('available')

// 可参加的考试
const availableExams = ref<any[]>([])
const availableLoading = ref(false)

// 考试历史
const historyRecords = ref<any[]>([])
const historyLoading = ref(false)
const historyPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 试卷详情
const paperDetailVisible = ref(false)
const currentPaper = ref<any>({})

// 考试结果详情
const resultDetailVisible = ref(false)
const currentResult = ref<any>({})

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ').substring(0, 16)
}

// 格式化用时
const formatDuration = (minutes: number) => {
  if (!minutes) return '-'
  if (minutes < 60) {
    return `${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
}

// 获取考试状态类型（根据时间判断）
const getExamStatusType = (exam: any) => {
  const now = new Date().getTime()
  const startTime = exam.startTime ? new Date(exam.startTime).getTime() : null
  const endTime = exam.endTime ? new Date(exam.endTime).getTime() : null

  if (startTime && now < startTime) {
    return 'info' // 未开始
  }
  if (endTime && now > endTime) {
    return 'danger' // 已结束
  }
  return 'success' // 进行中
}

// 获取考试状态文本
const getExamStatusText = (exam: any) => {
  const now = new Date().getTime()
  const startTime = exam.startTime ? new Date(exam.startTime).getTime() : null
  const endTime = exam.endTime ? new Date(exam.endTime).getTime() : null

  if (startTime && now < startTime) {
    return '未开始'
  }
  if (endTime && now > endTime) {
    return '已结束'
  }
  return '进行中'
}

// 判断是否可以开始考试
// 修复：考虑用户已有进行中考试的情况
const canStartExam = (exam: any) => {
  const now = new Date().getTime()
  const startTime = exam.startTime ? new Date(exam.startTime).getTime() : null
  const endTime = exam.endTime ? new Date(exam.endTime).getTime() : null

  // 如果有开始时间，当前时间必须大于开始时间
  if (startTime && now < startTime) {
    return false
  }
  // 如果有结束时间，当前时间必须小于结束时间
  if (endTime && now > endTime) {
    return false
  }
  // 试卷必须是已发布状态
  if (exam.status !== 1) {
    return false
  }
  
  // 修复：检查是否有进行中的考试记录
  const hasOngoingExam = historyRecords.value.some(record => 
    record.paperId === exam.id && record.status === 0
  )
  if (hasOngoingExam) {
    return false
  }
  
  return true
}

// 获取开始按钮文本
const getStartButtonText = (exam: any) => {
  const now = new Date().getTime()
  const startTime = exam.startTime ? new Date(exam.startTime).getTime() : null
  const endTime = exam.endTime ? new Date(exam.endTime).getTime() : null

  if (startTime && now < startTime) {
    return '未开始'
  }
  if (endTime && now > endTime) {
    return '已结束'
  }
  return '开始考试'
}

// 获取记录状态类型 - 与后端状态定义一致
// 后端状态定义: 0-考试中 1-已提交 2-已超时 3-已批阅
const getRecordStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'warning',   // 考试中
    1: 'info',      // 已提交
    2: 'danger',    // 已超时
    3: 'success'    // 已批阅
  }
  return types[status] || 'info'
}

// 获取记录状态文本 - 与后端状态定义一致
const getRecordStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '考试中',
    1: '已提交',
    2: '已超时',
    3: '已批阅'
  }
  return texts[status] || '未知'
}

// 获取可参加的考试列表
const fetchAvailableExams = async () => {
  availableLoading.value = true
  try {
    // 修复：使用可参加考试列表接口，避免权限问题
    const res = await getAvailableExams({
      current: 1,
      size: 100
    })
    availableExams.value = (res.records || []).map((item: any) => ({
      ...item,
      starting: false
    }))
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取可参加考试列表失败')
  } finally {
    availableLoading.value = false
  }
}

// 获取考试历史记录
const getHistoryRecords = async () => {
  historyLoading.value = true
  try {
    const res = await getMyExamRecordList({
      current: historyPagination.current,
      size: historyPagination.size
    })
    historyRecords.value = res.records || []
    historyPagination.total = res.total || 0
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取考试历史记录失败')
  } finally {
    historyLoading.value = false
  }
}

// 开始考试
const handleStartExam = async (exam: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要开始考试「${exam.paperName}」吗？考试时长为 ${exam.duration} 分钟，开始后请认真作答。`,
      '开始考试',
      {
        confirmButtonText: '开始考试',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    exam.starting = true
    // 修复：传递planId参数，用于考试次数限制校验
    const planId = exam.planId || null
    const res = await startExam(exam.id, planId || undefined)
    ElMessage.success('考试已开始，请认真作答！')
    
    // 跳转到考试答题页面
    const examRecordId = res.id
    if (examRecordId) {
      router.push(`/my/exam/taking/${examRecordId}`)
    } else {
      ElMessage.error('考试启动失败，请重试')
    }
    
    getHistoryRecords()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '开始考试失败')
    }
  } finally {
    exam.starting = false
  }
}

// 查看试卷详情
const handleViewPaper = (exam: any) => {
  currentPaper.value = exam
  paperDetailVisible.value = true
}

// 查看考试结果
const handleViewResult = async (record: any) => {
  try {
    const res = await getExamRecordDetail(record.id)
    currentResult.value = res
    resultDetailVisible.value = true
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取考试结果详情失败')
  }
}

// 初始化
onMounted(async () => {
  await getHistoryRecords() // 先获取历史记录
  fetchAvailableExams() // 再获取可参加考试
})
</script>

<style lang="scss" scoped>
.my-exam {
  .exam-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }
  }

  .exam-cards {
    min-height: 300px;

    .exam-card {
      margin-bottom: 20px;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-5px);
      }

      .exam-card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .exam-code {
          font-size: 12px;
          color: #909399;
        }
      }

      .exam-title {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 16px;
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .exam-info {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px;
        margin-bottom: 16px;

        .info-item {
          display: flex;
          align-items: center;
          font-size: 13px;
          color: #606266;

          .el-icon {
            margin-right: 6px;
            color: #409eff;
          }
        }
      }

      .exam-time {
        display: flex;
        align-items: flex-start;
        font-size: 12px;
        color: #909399;
        margin-bottom: 16px;
        padding: 10px;
        background-color: #f5f7fa;
        border-radius: 4px;

        .el-icon {
          margin-right: 6px;
          margin-top: 2px;
          flex-shrink: 0;
        }

        span {
          line-height: 1.5;
        }
      }

      .exam-actions {
        display: flex;
        gap: 10px;

        .el-button {
          flex: 1;
        }
      }
    }
  }

  .history-card {
    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }

  .score-pass {
    color: #67c23a;
    font-weight: bold;
  }

  .score-fail {
    color: #f56c6c;
    font-weight: bold;
  }

  .text-gray {
    color: #909399;
  }
}
</style>
