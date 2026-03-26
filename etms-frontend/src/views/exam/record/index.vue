<template>
  <div class="exam-record-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="考生姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入考生姓名" clearable />
        </el-form-item>
        <el-form-item label="试卷名称">
          <el-input v-model="searchForm.paperName" placeholder="请输入试卷名称" clearable />
        </el-form-item>
        <el-form-item label="考试状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="考试中" :value="0" />
            <el-option label="已提交" :value="1" />
            <el-option label="已超时" :value="2" />
            <el-option label="已批阅" :value="3" />
            <el-option label="已放弃" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>考试记录列表</span>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="userName" label="考生姓名" width="120" />
        <el-table-column prop="paperName" label="试卷名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="duration" label="考试时长" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.duration">{{ row.duration }}分钟</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="得分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.status >= 1">{{ row.userScore ?? row.totalScore ?? '-' }}</span>
            <span v-else class="text-gray">待完成</span>
          </template>
        </el-table-column>
        <el-table-column prop="passed" label="是否通过" width="100" align="center">
          <template #default="{ row }">
            <template v-if="row.status >= 3">
              <el-tag :type="row.passed === 1 ? 'success' : 'danger'">
                {{ row.passed === 1 ? '通过' : '未通过' }}
              </el-tag>
            </template>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="考试状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 考试详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="考试详情"
      width="900px"
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <!-- 考试基本信息 -->
        <el-descriptions :column="3" border class="exam-info">
          <el-descriptions-item label="考生姓名">{{ detailData.userName }}</el-descriptions-item>
          <el-descriptions-item label="试卷名称" :span="2">{{ detailData.paperName }}</el-descriptions-item>
          <el-descriptions-item label="考试时长">{{ detailData.duration }}分钟</el-descriptions-item>
          <el-descriptions-item label="考试得分">{{ detailData.totalScore ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="是否通过">
            <template v-if="detailData.status >= 3">
              <el-tag :type="detailData.passed === 1 ? 'success' : 'danger'">
                {{ detailData.passed === 1 ? '通过' : '未通过' }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(detailData.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(detailData.submitTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="考试状态">
            <el-tag :type="getStatusType(detailData.status)">
              {{ getStatusName(detailData.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 答题情况 -->
        <div class="answer-section" v-if="detailData.answers && detailData.answers.length > 0">
          <h4 class="section-title">答题情况</h4>
          <div class="answer-list">
            <div
              v-for="(answer, index) in detailData.answers"
              :key="answer.id"
              class="answer-item"
            >
              <div class="question-header">
                <span class="question-index">第{{ index + 1 }}题</span>
                <el-tag size="small" :type="getQuestionTypeTag(answer.questionType)">
                  {{ getQuestionTypeName(answer.questionType) }}
                </el-tag>
                <span class="question-score">
                  （{{ answer.score ?? 0 }}分）
                </span>
              </div>
              <div class="question-content">
                <p class="question-text">{{ answer.questionContent }}</p>
              </div>
              <div class="answer-content">
                <div class="answer-row">
                  <span class="label">考生答案：</span>
                  <span class="value">{{ answer.userAnswer || '未作答' }}</span>
                </div>
                <div class="answer-row">
                  <span class="label">正确答案：</span>
                  <span class="value correct">{{ answer.correctAnswer }}</span>
                </div>
                <div class="answer-row">
                  <span class="label">得分：</span>
                  <span class="value" :class="{ 'text-success': answer.isCorrect, 'text-danger': !answer.isCorrect && answer.userAnswer }">
                    {{ answer.userScore ?? 0 }}分
                    <el-icon v-if="answer.isCorrect" class="correct-icon"><Check /></el-icon>
                    <el-icon v-else-if="answer.userAnswer" class="wrong-icon"><Close /></el-icon>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无答题记录" />
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Check, Close } from '@element-plus/icons-vue'
import { getExamRecordList, getExamRecordDetail, exportExamRecords } from '@/api/exam'

// 定义类型
interface Answer {
  id: number
  questionId: number
  questionType: number
  questionContent: string
  userAnswer: string
  correctAnswer: string
  isCorrect: boolean
  score: number
  userScore: number
}

interface ExamRecord {
  id: number
  userId: number
  userName: string
  paperId: number
  paperName: string
  startTime: string
  submitTime: string
  duration: number
  totalScore: number
  userScore: number
  passScore: number
  passed: number
  status: number
  answers?: Answer[]
}

// 搜索表单
const searchForm = reactive({
  userName: '',
  paperName: '',
  status: null as number | null
})

// 表格数据
const tableData = ref<ExamRecord[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 详情对话框
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<ExamRecord>({} as ExamRecord)

// 获取状态类型 - 与后端状态定义一致
// 后端状态定义: 0-考试中 1-已提交 2-已超时 3-已批阅 4-已放弃
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'warning',   // 考试中
    1: 'info',      // 已提交
    2: 'danger',    // 已超时
    3: 'success',   // 已批阅
    4: 'info'       // 已放弃
  }
  return types[status] || 'info'
}

// 获取状态名称 - 与后端状态定义一致
const getStatusName = (status: number) => {
  const names: Record<number, string> = {
    0: '考试中',
    1: '已提交',
    2: '已超时',
    3: '已批阅',
    4: '已放弃'
  }
  return names[status] || '未知'
}

// 获取题目类型名称
const getQuestionTypeName = (type: number) => {
  const names: Record<number, string> = {
    1: '单选题',
    2: '多选题',
    3: '判断题',
    4: '填空题',
    5: '简答题'
  }
  return names[type] || '未知'
}

// 获取题目类型标签颜色
const getQuestionTypeTag = (type: number) => {
  const tags: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info',
    5: 'danger'
  }
  return tags[type] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ').substring(0, 16)
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getExamRecordList({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取考试记录列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    userName: '',
    paperName: '',
    status: null
  })
  handleSearch()
}

// 查看详情
const handleViewDetail = async (row: ExamRecord) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getExamRecordDetail(row.id)
    detailData.value = res || row
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 导出
const handleExport = async () => {
  try {
    // 构建导出参数，传递当前搜索条件
    const params: any = {}
    if (searchForm.userName) params.userName = searchForm.userName
    if (searchForm.paperName) params.paperName = searchForm.paperName
    if (searchForm.status !== null) params.status = searchForm.status
    
    // 使用考试记录导出API
    const blob = await exportExamRecords(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `考试记录_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '导出失败')
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.exam-record-management {
  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }

  .text-gray {
    color: #909399;
  }

  .text-success {
    color: #67c23a;
  }

  .text-danger {
    color: #f56c6c;
  }
}

.exam-info {
  margin-bottom: 20px;
}

.answer-section {
  margin-top: 20px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
    padding-bottom: 8px;
    border-bottom: 1px solid #ebeef5;
  }

  .answer-list {
    max-height: 500px;
    overflow-y: auto;
    padding-right: 10px;
  }

  .answer-item {
    padding: 16px;
    margin-bottom: 16px;
    background: #fafafa;
    border-radius: 8px;
    border: 1px solid #ebeef5;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .question-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .question-index {
      font-weight: 600;
      color: #409eff;
    }

    .question-score {
      color: #909399;
      font-size: 13px;
    }
  }

  .question-content {
    margin-bottom: 12px;

    .question-text {
      margin: 0;
      line-height: 1.6;
      color: #303133;
    }
  }

  .answer-content {
    .answer-row {
      display: flex;
      align-items: flex-start;
      margin-bottom: 8px;
      line-height: 1.6;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        flex-shrink: 0;
        width: 80px;
        color: #606266;
        font-size: 13px;
      }

      .value {
        flex: 1;
        color: #303133;
        font-size: 13px;

        &.correct {
          color: #67c23a;
          font-weight: 500;
        }
      }
    }
  }

  .correct-icon {
    color: #67c23a;
    margin-left: 4px;
    vertical-align: middle;
  }

  .wrong-icon {
    color: #f56c6c;
    margin-left: 4px;
    vertical-align: middle;
  }
}
</style>
