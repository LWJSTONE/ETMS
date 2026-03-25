<template>
  <div class="my-result">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.examCount }}</div>
              <div class="stats-label">考试次数</div>
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
              <div class="stats-label">通过次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon rate">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.passRate }}%</div>
              <div class="stats-label">通过率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card">
          <div class="stats-item">
            <div class="stats-icon avg">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.avgScore }}</div>
              <div class="stats-label">平均分</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="试卷名称">
          <el-input
            v-model="searchForm.paperName"
            placeholder="请输入试卷名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="是否通过">
          <el-select v-model="searchForm.passed" placeholder="全部" clearable style="width: 140px">
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
          <span>我的成绩</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="paperName" label="试卷名称" min-width="200" show-overflow-tooltip />
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
        <el-table-column prop="examTime" label="考试时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="durationUsed" label="用时" width="100" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.durationUsed) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
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
          <el-descriptions-item label="试卷名称" :span="3">{{ detailData.paperName }}</el-descriptions-item>
          <el-descriptions-item label="考试得分">
            <span :class="getScoreClass(detailData)">{{ detailData.userScore }} / {{ detailData.totalScore }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="及格分数">{{ detailData.passScore }}分</el-descriptions-item>
          <el-descriptions-item label="考试结果">
            <el-tag :type="detailData.passed === 1 ? 'success' : 'danger'" effect="dark">
              {{ detailData.passed === 1 ? '通过' : '未通过' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客观题得分">{{ detailData.objectiveScore ?? '-' }}分</el-descriptions-item>
          <el-descriptions-item label="主观题得分">{{ detailData.subjectiveScore ?? '-' }}分</el-descriptions-item>
          <el-descriptions-item label="补考次数">{{ detailData.retakeCount || 0 }}次</el-descriptions-item>
          <el-descriptions-item label="考试时间">{{ formatDateTime(detailData.submitTime) }}</el-descriptions-item>
          <el-descriptions-item label="考试用时">{{ formatDuration(detailData.durationUsed) }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDateTime(detailData.submitTime) }}</el-descriptions-item>
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
                    <span class="label">我的答案：</span>
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
  TrendCharts,
  DataAnalysis,
  Search,
  Refresh,
  View,
  List
} from '@element-plus/icons-vue'
import { getMyResults } from '@/api/exam'

// 统计数据
const stats = reactive({
  examCount: 0,
  passCount: 0,
  passRate: '0',
  avgScore: '0'
})

// 搜索表单
const searchForm = reactive({
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
      paperName: searchForm.paperName || undefined,
      passed: searchForm.passed
    }
    if (searchForm.examTimeRange && searchForm.examTimeRange.length === 2) {
      params.examStartTime = searchForm.examTimeRange[0]
      params.examEndTime = searchForm.examTimeRange[1]
    }
    const res = await getMyResults(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
    
    // 获取完整统计数据（不依赖当前页数据）
    await getStatsSummary()
  } catch (error) {
    console.error(error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 获取汇总统计数据
const getStatsSummary = async () => {
  try {
    // 使用相同筛选条件但大分页获取统计数据
    const params: any = {
      current: 1,
      size: 10000,
      paperName: searchForm.paperName || undefined,
      passed: searchForm.passed
    }
    if (searchForm.examTimeRange && searchForm.examTimeRange.length === 2) {
      params.examStartTime = searchForm.examTimeRange[0]
      params.examEndTime = searchForm.examTimeRange[1]
    }
    
    const res = await getMyResults(params)
    const allRecords = res.data?.records || []
    
    // 计算统计数据
    stats.examCount = allRecords.length
    const passCount = allRecords.filter((r: any) => r.passed === 1).length
    stats.passCount = passCount
    stats.passRate = allRecords.length > 0 
      ? ((passCount / allRecords.length) * 100).toFixed(1) 
      : '0'
    
    const totalScore = allRecords.reduce((sum: number, r: any) => sum + (r.userScore || 0), 0)
    stats.avgScore = allRecords.length > 0 
      ? (totalScore / allRecords.length).toFixed(1) 
      : '0'
  } catch (error: any) {
    console.error('获取统计数据失败:', error)
    ElMessage.error(error.message || '获取统计数据失败')
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
    paperName: '',
    passed: null,
    examTimeRange: []
  })
  handleSearch()
}

// 查看详情
const handleViewDetail = (row: any) => {
  detailData.value = row
  
  // 解析答题详情
  if (row.answerDetail) {
    try {
      answerList.value = typeof row.answerDetail === 'string' 
        ? JSON.parse(row.answerDetail) 
        : row.answerDetail
    } catch {
      answerList.value = []
    }
  } else {
    answerList.value = []
  }
  
  detailVisible.value = true
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 16)
}

// 格式化用时
const formatDuration = (minutes: number) => {
  if (!minutes && minutes !== 0) return '-'
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
  return userAnswer.split('').includes(optionLabel)
}

// 判断是否是正确选项
const isCorrectOption = (answer: string, optionLabel: string) => {
  if (!answer) return false
  return answer.split('').includes(optionLabel)
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.my-result {
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

          &.rate {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          }

          &.avg {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
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
