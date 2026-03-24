<template>
  <div class="result-management">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.totalCount }}</div>
              <div class="stats-label">总考试人次</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon pass">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.passCount }}</div>
              <div class="stats-label">通过人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon fail">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.failCount }}</div>
              <div class="stats-label">未通过人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon avg">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.avgScore }}分</div>
              <div class="stats-label">平均分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="考生姓名">
          <el-input
            v-model="searchForm.userName"
            placeholder="请输入考生姓名"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="试卷名称">
          <el-input
            v-model="searchForm.paperName"
            placeholder="请输入试卷名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="通过状态">
          <el-select v-model="searchForm.passed" placeholder="请选择" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="已通过" :value="1" />
            <el-option label="未通过" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试时间">
          <el-date-picker
            v-model="searchForm.examTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
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

    <!-- 表格区域 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>成绩列表</span>
          <div class="header-buttons">
            <el-button type="success" @click="handleExport" :loading="exportLoading">
              <el-icon><Download /></el-icon>导出成绩
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="userName" label="考生姓名" width="120" align="center">
          <template #default="{ row }">
            {{ row.userName || row.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="所属部门" width="140" show-overflow-tooltip />
        <el-table-column prop="paperName" label="试卷名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userScore" label="得分" width="100" align="center">
          <template #default="{ row }">
            <span :class="getScoreClass(row)">{{ row.userScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="满分" width="80" align="center" />
        <el-table-column prop="passScore" label="及格分" width="80" align="center" />
        <el-table-column prop="passed" label="是否通过" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.passed === 1 ? 'success' : 'danger'" effect="dark">
              {{ row.passed === 1 ? '通过' : '未通过' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="retakeCount" label="补考次数" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.retakeCount > 0" type="warning" size="small">
              {{ row.retakeCount }}次
            </el-tag>
            <span v-else>0次</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="考试时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="examDuration" label="用时" width="90" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.examDuration) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
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

    <!-- 答题详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="答题详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="detailData" class="detail-container">
        <!-- 基本信息 -->
        <el-descriptions :column="3" border class="detail-info">
          <el-descriptions-item label="考生姓名">{{ detailData.userName || detailData.realName }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ detailData.deptName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="试卷名称">{{ detailData.paperName }}</el-descriptions-item>
          <el-descriptions-item label="考试得分">
            <span :class="getScoreClass(detailData)">{{ detailData.userScore }} / {{ detailData.totalScore }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="及格分数">{{ detailData.passScore }}分</el-descriptions-item>
          <el-descriptions-item label="考试结果">
            <el-tag :type="detailData.passed === 1 ? 'success' : 'danger'" effect="dark">
              {{ detailData.passed === 1 ? '通过' : '未通过' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="考试时间">{{ formatDateTime(detailData.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="考试用时">{{ formatDuration(detailData.examDuration) }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(detailData.startTime) }}</el-descriptions-item>
        </el-descriptions>

        <!-- 答题详情 -->
        <div class="answer-detail" v-if="answerList.length > 0">
          <div class="section-title">
            <el-icon><List /></el-icon>
            <span>答题详情</span>
          </div>
          <el-scrollbar max-height="400px">
            <div class="answer-list">
              <div
                v-for="(item, index) in answerList"
                :key="index"
                class="answer-item"
                :class="{ correct: item.isCorrect, wrong: !item.isCorrect && item.userAnswer }"
              >
                <div class="question-header">
                  <span class="question-index">第{{ index + 1 }}题</span>
                  <el-tag :type="getQuestionTypeName(item.questionType).type" size="small">
                    {{ getQuestionTypeName(item.questionType).name }}
                  </el-tag>
                  <span class="question-score">（{{ item.score }}分）</span>
                  <el-tag v-if="item.isCorrect" type="success" size="small">正确</el-tag>
                  <el-tag v-else-if="item.userAnswer" type="danger" size="small">错误</el-tag>
                  <el-tag v-else type="info" size="small">未作答</el-tag>
                </div>
                <div class="question-content">{{ item.questionContent }}</div>
                <div v-if="item.questionType <= 3" class="question-options">
                  <div
                    v-for="option in getQuestionOptions(item)"
                    :key="option.label"
                    class="option-item"
                    :class="{
                      selected: isOptionSelected(item.userAnswer, option.label),
                      correct: isCorrectOption(item.answer, option.label),
                      wrong: isOptionSelected(item.userAnswer, option.label) && !isCorrectOption(item.answer, option.label)
                    }"
                  >
                    <span class="option-label">{{ option.label }}.</span>
                    <span class="option-content">{{ option.content }}</span>
                  </div>
                </div>
                <div class="answer-info">
                  <div class="user-answer">
                    <span class="label">考生答案：</span>
                    <span class="value">{{ item.userAnswer || '未作答' }}</span>
                  </div>
                  <div class="correct-answer">
                    <span class="label">正确答案：</span>
                    <span class="value">{{ item.answer }}</span>
                  </div>
                  <div v-if="item.answerAnalysis" class="analysis">
                    <span class="label">答案解析：</span>
                    <span class="value">{{ item.answerAnalysis }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-scrollbar>
        </div>
        <el-empty v-else description="暂无答题详情" />
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
import {
  Document,
  CircleCheck,
  CircleClose,
  TrendCharts,
  Search,
  Refresh,
  Download,
  View,
  List
} from '@element-plus/icons-vue'
import { getResultList, getResultDetail, getResultStats, exportResults } from '@/api/exam'
import type { ResultStats } from '@/api/exam'

// 统计数据
const stats = reactive({
  totalCount: 0,
  passCount: 0,
  failCount: 0,
  passRate: '0',
  avgScore: '0'
})

// 搜索表单
const searchForm = reactive({
  userName: '',
  paperName: '',
  passed: null as number | null,
  examTimeRange: [] as string[]
})

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 导出loading
const exportLoading = ref(false)

// 详情对话框
const detailVisible = ref(false)
const detailData = ref<any>(null)
const answerList = ref<any[]>([])

// 获取成绩列表
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size,
      userName: searchForm.userName || undefined,
      paperName: searchForm.paperName || undefined,
      passed: searchForm.passed
    }
    if (searchForm.examTimeRange && searchForm.examTimeRange.length === 2) {
      params.startTime = searchForm.examTimeRange[0]
      params.endTime = searchForm.examTimeRange[1]
    }
    const res = await getResultList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    
    // 获取统计数据
    getStats()
  } catch (error) {
    console.error(error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStats = async () => {
  try {
    const params: any = {}
    if (searchForm.examTimeRange && searchForm.examTimeRange.length === 2) {
      params.startTime = searchForm.examTimeRange[0]
      params.endTime = searchForm.examTimeRange[1]
    }
    const res = await getResultStats(params)
    if (res.data) {
      stats.totalCount = res.data.totalCount || 0
      stats.passCount = res.data.passCount || 0
      stats.failCount = res.data.failCount || 0
      stats.passRate = (res.data.passRate || 0).toFixed(1)
      stats.avgScore = (res.data.avgScore || 0).toFixed(1)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 如果后端API不可用，使用前端计算作为备选
    calculateStats()
  }
}

// 计算统计数据（备选方案）
const calculateStats = () => {
  const data = tableData.value
  stats.totalCount = pagination.total
  
  if (data.length > 0) {
    const passCount = data.filter(item => item.passed === 1).length
    stats.passCount = passCount
    stats.failCount = data.length - passCount
    stats.passRate = data.length > 0 ? ((passCount / data.length) * 100).toFixed(1) : '0'
    const totalScore = data.reduce((sum, item) => sum + (item.userScore || 0), 0)
    stats.avgScore = (totalScore / data.length).toFixed(1)
  } else {
    stats.passCount = 0
    stats.failCount = 0
    stats.passRate = '0'
    stats.avgScore = '0'
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
    passed: null,
    examTimeRange: []
  })
  handleSearch()
}

// 导出成绩
const handleExport = async () => {
  exportLoading.value = true
  try {
    // 构建导出参数
    const params: any = {}
    if (searchForm.userName) params.userName = searchForm.userName
    if (searchForm.paperName) params.paperName = searchForm.paperName
    if (searchForm.passed !== null) params.passed = searchForm.passed
    if (searchForm.examTimeRange && searchForm.examTimeRange.length === 2) {
      params.startTime = searchForm.examTimeRange[0]
      params.endTime = searchForm.examTimeRange[1]
    }
    
    // 调用后端导出接口并正确处理Blob下载
    const blob = await exportResults(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `成绩数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 查看详情
const handleViewDetail = async (row: any) => {
  detailVisible.value = true
  detailData.value = row
  answerList.value = []
  
  try {
    // 调用成绩详情API获取完整信息
    const res = await getResultDetail(row.id)
    const detailInfo = res.data
    
    // 更新详情数据
    if (detailInfo) {
      detailData.value = {
        ...row,
        ...detailInfo
      }
      
      // 解析答题详情
      if (detailInfo.answerDetail) {
        try {
          answerList.value = typeof detailInfo.answerDetail === 'string' 
            ? JSON.parse(detailInfo.answerDetail) 
            : detailInfo.answerDetail
        } catch {
          answerList.value = []
        }
      } else if (detailInfo.answers) {
        // 处理answers字段
        answerList.value = detailInfo.answers.map((ans: any, index: number) => ({
          questionContent: ans.questionContent || `题目${index + 1}`,
          questionType: ans.questionType || 1,
          userAnswer: ans.userAnswer || '',
          answer: ans.correctAnswer || ans.answer || '',
          score: ans.score || 0,
          isCorrect: ans.isCorrect,
          answerAnalysis: ans.answerAnalysis || '',
          optionA: ans.optionA,
          optionB: ans.optionB,
          optionC: ans.optionC,
          optionD: ans.optionD,
          optionE: ans.optionE
        }))
      }
    }
  } catch (error) {
    console.warn('获取详情失败:', error)
    ElMessage.error('获取成绩详情失败')
    // 如果API调用失败，使用行数据
    if (row.answerDetail) {
      try {
        answerList.value = typeof row.answerDetail === 'string' 
          ? JSON.parse(row.answerDetail) 
          : row.answerDetail
      } catch {
        answerList.value = []
      }
    }
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 16)
}

// 格式化用时
const formatDuration = (minutes: number) => {
  if (minutes === null || minutes === undefined) return '-'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
}

// 获取分数样式类
const getScoreClass = (row: any) => {
  if (row.passed === 1) return 'score-pass'
  return 'score-fail'
}

// 获取题目类型名称
const getQuestionTypeName = (type: number) => {
  const types: Record<number, { name: string; type: string }> = {
    1: { name: '单选题', type: 'primary' },
    2: { name: '多选题', type: 'success' },
    3: { name: '判断题', type: 'warning' },
    4: { name: '填空题', type: 'info' },
    5: { name: '简答题', type: 'danger' }
  }
  return types[type] || { name: '未知', type: 'info' }
}

// 获取题目选项
const getQuestionOptions = (item: any) => {
  const options = []
  const labels = ['A', 'B', 'C', 'D', 'E']
  const optionKeys = ['optionA', 'optionB', 'optionC', 'optionD', 'optionE'] as const
  
  optionKeys.forEach((key, index) => {
    if (item[key]) {
      options.push({
        label: labels[index],
        content: item[key]
      })
    }
  })
  return options
}

// 判断选项是否被选中
const isOptionSelected = (userAnswer: string, optionLabel: string) => {
  if (!userAnswer) return false
  // 支持多种格式："A,B,C" 或 "ABC"
  const answers = userAnswer.includes(',') ? userAnswer.split(',') : userAnswer.split('')
  return answers.includes(optionLabel)
}

// 判断是否是正确选项
const isCorrectOption = (answer: string, optionLabel: string) => {
  if (!answer) return false
  // 支持多种格式："A,B,C" 或 "ABC"
  const correctAnswers = answer.includes(',') ? answer.split(',') : answer.split('')
  return correctAnswers.includes(optionLabel)
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.result-management {
  .stats-row {
    margin-bottom: 20px;

    .stats-card {
      .stats-item {
        display: flex;
        align-items: center;
        padding: 10px 0;

        .stats-icon {
          width: 60px;
          height: 60px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .el-icon {
            font-size: 28px;
            color: #fff;
          }

          &.total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.pass {
            background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
          }

          &.fail {
            background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
          }

          &.avg {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }
        }

        .stats-info {
          flex: 1;

          .stats-value {
            font-size: 28px;
            font-weight: bold;
            color: #303133;
            line-height: 1.2;
          }

          .stats-label {
            font-size: 14px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
  }

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-buttons {
        display: flex;
        gap: 10px;
      }
    }

    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }

    .score-pass {
      color: #67c23a;
      font-weight: bold;
    }

    .score-fail {
      color: #f56c6c;
      font-weight: bold;
    }
  }

  .detail-container {
    .detail-info {
      margin-bottom: 24px;
    }

    .answer-detail {
      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 16px;
        padding-bottom: 10px;
        border-bottom: 1px solid #ebeef5;
      }

      .answer-list {
        .answer-item {
          padding: 16px;
          margin-bottom: 16px;
          border-radius: 8px;
          background: #fafafa;
          border: 1px solid #ebeef5;

          &.correct {
            border-left: 4px solid #67c23a;
          }

          &.wrong {
            border-left: 4px solid #f56c6c;
          }

          .question-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 12px;

            .question-index {
              font-weight: bold;
              color: #303133;
            }

            .question-score {
              color: #909399;
              font-size: 13px;
            }
          }

          .question-content {
            font-size: 15px;
            color: #303133;
            line-height: 1.6;
            margin-bottom: 12px;
          }

          .question-options {
            margin-bottom: 12px;

            .option-item {
              padding: 8px 12px;
              margin-bottom: 6px;
              border-radius: 4px;
              background: #fff;
              border: 1px solid #e4e7ed;
              display: flex;

              .option-label {
                font-weight: bold;
                margin-right: 8px;
                min-width: 20px;
              }

              .option-content {
                flex: 1;
              }

              &.selected {
                border-color: #409eff;
                background: #ecf5ff;
              }

              &.correct {
                border-color: #67c23a;
                background: #f0f9eb;

                .option-label {
                  color: #67c23a;
                }
              }

              &.wrong {
                border-color: #f56c6c;
                background: #fef0f0;

                .option-label {
                  color: #f56c6c;
                }
              }
            }
          }

          .answer-info {
            padding: 12px;
            background: #fff;
            border-radius: 4px;
            font-size: 14px;

            .user-answer,
            .correct-answer,
            .analysis {
              margin-bottom: 8px;
              display: flex;

              &:last-child {
                margin-bottom: 0;
              }

              .label {
                color: #909399;
                min-width: 80px;
              }

              .value {
                flex: 1;
                color: #303133;
              }
            }

            .user-answer .value {
              color: #409eff;
            }

            .correct-answer .value {
              color: #67c23a;
              font-weight: bold;
            }

            .analysis {
              margin-top: 8px;
              padding-top: 8px;
              border-top: 1px dashed #e4e7ed;
            }
          }
        }
      }
    }
  }
}
</style>
