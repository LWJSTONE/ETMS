<template>
  <div class="exam-taking">
    <!-- 考试信息头部 -->
    <el-card shadow="never" class="exam-header-card">
      <div class="exam-header">
        <div class="exam-info">
          <h2 class="exam-title">{{ examInfo.paperName }}</h2>
          <div class="exam-meta">
            <span class="meta-item">
              <el-icon><Timer /></el-icon>
              剩余时间: {{ formatTime(remainingTime) }}
            </span>
            <span class="meta-item">
              <el-icon><Document /></el-icon>
              总题数: {{ questions.length }}道
            </span>
            <span class="meta-item">
              <el-icon><DataLine /></el-icon>
              总分: {{ examInfo.totalScore }}分
            </span>
            <span class="meta-item">
              <el-icon><CircleCheck /></el-icon>
              及格分: {{ examInfo.passScore }}分
            </span>
          </div>
        </div>
        <div class="exam-actions">
          <el-button type="danger" @click="handleGiveUp" :disabled="submitting">
            <el-icon><Close /></el-icon>
            放弃考试
          </el-button>
          <el-button type="success" @click="handleSubmit" :loading="submitting">
            <el-icon><Check /></el-icon>
            提交试卷
          </el-button>
        </div>
      </div>
      <el-progress 
        :percentage="answerProgress" 
        :stroke-width="8"
        :format="(p: number) => `答题进度: ${p}%`"
      />
    </el-card>

    <!-- 题目区域 -->
    <el-row :gutter="20" class="exam-content">
      <!-- 左侧题目导航 -->
      <el-col :xs="24" :md="6" class="question-nav-col">
        <el-card shadow="never" class="question-nav-card">
          <template #header>
            <span>答题卡</span>
          </template>
          <div class="question-nav">
            <div 
              v-for="(q, index) in questions" 
              :key="q.id"
              class="nav-item"
              :class="{ 
                'answered': isAnswered(q.id),
                'current': currentIndex === index
              }"
              @click="goToQuestion(index)"
            >
              {{ index + 1 }}
            </div>
          </div>
          <div class="nav-legend">
            <span class="legend-item"><span class="dot answered"></span>已答</span>
            <span class="legend-item"><span class="dot"></span>未答</span>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧题目内容 -->
      <el-col :xs="24" :md="18" class="question-content-col">
        <el-card shadow="never" class="question-card" v-loading="loading">
          <template v-if="currentQuestion">
            <!-- 题目标题 -->
            <div class="question-header">
              <el-tag :type="getQuestionTypeTag(currentQuestion.questionType)" size="small">
                {{ getQuestionTypeName(currentQuestion.questionType) }}
              </el-tag>
              <span class="question-score">({{ currentQuestion.score }}分)</span>
              <span class="question-index">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
            </div>

            <!-- 题目内容 -->
            <div class="question-content">
              <p class="question-text">{{ currentQuestion.questionContent }}</p>
            </div>

            <!-- 答案选项 -->
            <div class="answer-section">
              <!-- 单选题 -->
              <el-radio-group 
                v-if="currentQuestion.questionType === 1" 
                v-model="answers[currentQuestion.id]"
                class="answer-options"
              >
                <el-radio 
                  v-for="option in getOptions(currentQuestion)" 
                  :key="option.key" 
                  :value="option.key"
                  class="answer-option"
                >
                  <span class="option-key">{{ option.key }}.</span>
                  <span class="option-text">{{ option.text }}</span>
                </el-radio>
              </el-radio-group>

              <!-- 多选题 -->
              <el-checkbox-group 
                v-else-if="currentQuestion.questionType === 2" 
                v-model="answers[currentQuestion.id]"
                class="answer-options"
              >
                <el-checkbox 
                  v-for="option in getOptions(currentQuestion)" 
                  :key="option.key" 
                  :value="option.key"
                  class="answer-option"
                >
                  <span class="option-key">{{ option.key }}.</span>
                  <span class="option-text">{{ option.text }}</span>
                </el-checkbox>
              </el-checkbox-group>

              <!-- 判断题 -->
              <!-- 修复：使用"A"/"B"作为答案值，后端会将"正确"/"错误"/"A"/"B"进行标准化处理 -->
              <el-radio-group 
                v-else-if="currentQuestion.questionType === 3" 
                v-model="answers[currentQuestion.id]"
                class="answer-options"
              >
                <el-radio value="A" class="answer-option">
                  <span class="option-key">T.</span>
                  <span class="option-text">正确</span>
                </el-radio>
                <el-radio value="B" class="answer-option">
                  <span class="option-key">F.</span>
                  <span class="option-text">错误</span>
                </el-radio>
              </el-radio-group>

              <!-- 填空题 -->
              <el-input
                v-else-if="currentQuestion.questionType === 4"
                v-model="answers[currentQuestion.id]"
                type="textarea"
                :rows="3"
                placeholder="请输入答案"
                class="answer-input"
              />

              <!-- 简答题 -->
              <el-input
                v-else-if="currentQuestion.questionType === 5"
                v-model="answers[currentQuestion.id]"
                type="textarea"
                :rows="6"
                placeholder="请输入详细答案"
                class="answer-input"
              />
            </div>

            <!-- 题目操作按钮 -->
            <div class="question-actions">
              <el-button 
                @click="prevQuestion" 
                :disabled="currentIndex === 0"
              >
                <el-icon><ArrowLeft /></el-icon>
                上一题
              </el-button>
              <el-button 
                @click="nextQuestion" 
                :disabled="currentIndex === questions.length - 1"
              >
                下一题
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-empty v-else description="题目加载中..." />
        </el-card>
      </el-col>
    </el-row>

    <!-- 提交确认对话框 -->
    <el-dialog v-model="submitDialogVisible" title="提交确认" width="400px">
      <p>您已完成 {{ answeredCount }} / {{ questions.length }} 道题目</p>
      <p v-if="answeredCount < questions.length" class="warning-text">
        还有 {{ questions.length - answeredCount }} 道题目未作答，确定要提交吗？
      </p>
      <template #footer>
        <el-button @click="submitDialogVisible = false">继续答题</el-button>
        <el-button type="primary" @click="confirmSubmit" :loading="submitting">
          确认提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Document, DataLine, CircleCheck, Close, Check, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getExamRecordDetail, submitExam, giveUpExam } from '@/api/exam'

const route = useRoute()
const router = useRouter()

// 考试记录ID
const recordId = computed(() => Number(route.params.id))

// 本地存储key
const STORAGE_KEY = computed(() => `exam_answers_${recordId.value}`)

// 状态
const loading = ref(false)
const submitting = ref(false)
const submitDialogVisible = ref(false)

// 考试信息
const examInfo = reactive({
  id: 0,
  paperId: 0,
  paperName: '',
  totalScore: 100,
  passScore: 60,
  duration: 60,
  startTime: ''
})

// 题目列表
const questions = ref<any[]>([])

// 答案
const answers = reactive<Record<number, any>>({})

// 当前题目索引
const currentIndex = ref(0)

// 剩余时间（秒）
const remainingTime = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

// 防作弊：切屏次数
const switchCount = ref(0)
const MAX_SWITCH_COUNT = 3
let hiddenTime = ref(0)

// 当前题目
const currentQuestion = computed(() => questions.value[currentIndex.value])

// 已答题数量
const answeredCount = computed(() => {
  return questions.value.filter(q => {
    const answer = answers[q.id]
    if (answer === undefined || answer === null || answer === '') return false
    if (Array.isArray(answer) && answer.length === 0) return false
    return true
  }).length
})

// 答题进度
const answerProgress = computed(() => {
  if (questions.value.length === 0) return 0
  return Math.round((answeredCount.value / questions.value.length) * 100)
})

// 格式化时间
const formatTime = (seconds: number) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  if (hours > 0) {
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }
  return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 获取题目类型名称
const getQuestionTypeName = (type: number) => {
  const types: Record<number, string> = {
    1: '单选题',
    2: '多选题',
    3: '判断题',
    4: '填空题',
    5: '简答题'
  }
  return types[type] || '未知类型'
}

// 获取题目类型标签
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

// 获取选项
const getOptions = (question: any) => {
  const options: { key: string; text: string }[] = []
  const optionKeys = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']
  
  optionKeys.forEach(key => {
    const optionField = `option${key}` as keyof typeof question
    if (question[optionField]) {
      options.push({ key, text: question[optionField] as string })
    }
  })
  
  return options
}

// 判断题目是否已作答（处理多选题数组类型）
const isAnswered = (questionId: number) => {
  const answer = answers[questionId]
  if (answer === undefined || answer === null) return false
  if (Array.isArray(answer)) return answer.length > 0
  return answer !== ''
}

// 上一题
const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

// 下一题
const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
  }
}

// 跳转到指定题目
const goToQuestion = (index: number) => {
  currentIndex.value = index
}

// 保存答案到本地存储
const saveAnswersToLocal = () => {
  try {
    const data = {
      answers: { ...answers },
      currentIndex: currentIndex.value,
      savedAt: new Date().toISOString()
    }
    localStorage.setItem(STORAGE_KEY.value, JSON.stringify(data))
  } catch (e) {
    console.warn('保存答案到本地存储失败:', e)
  }
}

// 从本地存储恢复答案
const restoreAnswersFromLocal = () => {
  try {
    const saved = localStorage.getItem(STORAGE_KEY.value)
    if (saved) {
      const data = JSON.parse(saved)
      // 恢复答案
      if (data.answers) {
        Object.keys(data.answers).forEach(key => {
          const questionId = Number(key)
          const savedAnswer = data.answers[key]
          // 查找对应的题目类型
          const question = questions.value.find(q => q.id === questionId)
          if (question) {
            // 多选题需要确保答案是数组格式
            if (question.questionType === 2) {
              if (Array.isArray(savedAnswer)) {
                // 确保数组元素是字符串类型
                answers[questionId] = savedAnswer.map(String)
              } else if (savedAnswer !== null && savedAnswer !== undefined && savedAnswer !== '') {
                // 修复：支持多种分隔符格式，将字符串转换为数组
                const str = String(savedAnswer)
                if (str.includes(',')) {
                  // 逗号分隔格式：A,B,C
                  answers[questionId] = str.split(',').map(s => s.trim()).filter(s => s)
                } else if (str.includes(' ')) {
                  // 空格分隔格式：A B C
                  answers[questionId] = str.split(' ').map(s => s.trim()).filter(s => s)
                } else {
                  // 无分隔符格式：ABC（每个字符是一个选项）
                  answers[questionId] = str.split('').filter(s => /[A-H]/i.test(s)).map(s => s.toUpperCase())
                }
              } else {
                answers[questionId] = []
              }
            } else {
              answers[questionId] = savedAnswer
            }
          }
        })
      }
      // 恢复当前题目索引
      if (typeof data.currentIndex === 'number') {
        currentIndex.value = data.currentIndex
      }
      return true
    }
  } catch (e) {
    console.warn('从本地存储恢复答案失败:', e)
  }
  return false
}

// 清除本地存储的答案
const clearLocalAnswers = () => {
  try {
    localStorage.removeItem(STORAGE_KEY.value)
  } catch (e) {
    console.warn('清除本地存储失败:', e)
  }
}

// 监听答案变化，自动保存到本地存储
watch(answers, () => {
  saveAnswersToLocal()
}, { deep: true })

// 防作弊：监听页面可见性变化
const handleVisibilityChange = () => {
  if (document.hidden) {
    // 仅在页面变为隐藏时记录时间，避免误判
    hiddenTime.value = Date.now()
  } else {
    // 页面重新可见时，检查是否之前记录了隐藏时间
    if (hiddenTime.value > 0) {
      switchCount.value++
      const recordedTime = hiddenTime.value
      hiddenTime.value = 0 // 重置时间，确保每次切屏只计数一次
      
      if (switchCount.value >= MAX_SWITCH_COUNT) {
        // 超过最大切屏次数，强制提交
        ElMessage.error('切屏次数过多，系统将自动提交试卷！')
        confirmSubmit()
      } else {
        ElMessage.warning(`您已切换页面${switchCount.value}次，超过${MAX_SWITCH_COUNT}次将自动提交试卷！`)
      }
    }
  }
}

// 获取考试详情
const fetchExamDetail = async () => {
  loading.value = true
  try {
    const res = await getExamRecordDetail(recordId.value)
    const data = res
    
    // 填充考试信息
    Object.assign(examInfo, {
      id: data.id,
      paperId: data.paperId,
      paperName: data.paperName,
      totalScore: data.totalScore,
      passScore: data.passScore,
      duration: data.duration,
      startTime: data.startTime
    })
    
    // 填充题目列表
    if (data.questions && data.questions.length > 0) {
      questions.value = data.questions
    } else if (data.paperQuestions && data.paperQuestions.length > 0) {
      questions.value = data.paperQuestions.map((pq: any) => pq.question || pq)
    }
    
    // 初始化答案（多选题需要数组）
    questions.value.forEach(q => {
      if (q.questionType === 2) {
        answers[q.id] = []
      }
    })
    
    // 尝试从本地存储恢复答案
    const restored = restoreAnswersFromLocal()
    if (restored) {
      ElMessage.success('已恢复之前保存的答题进度')
    }
    
    // 计算剩余时间
    if (data.startTime && data.duration) {
      const startTime = new Date(data.startTime).getTime()
      const endTime = startTime + data.duration * 60 * 1000
      const now = Date.now()
      remainingTime.value = Math.max(0, Math.floor((endTime - now) / 1000))
      
      // 启动计时器
      startTimer()
    }
  } catch (error: any) {
    console.error('获取考试详情失败:', error)
    ElMessage.error(error.message || '获取考试详情失败')
  } finally {
    loading.value = false
  }
}

// 启动计时器
const startTimer = () => {
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
      // 最后5分钟提醒
      if (remainingTime.value === 300) {
        ElMessage.warning('距离考试结束还有5分钟，请抓紧时间！')
      }
      // 最后1分钟提醒
      if (remainingTime.value === 60) {
        ElMessage.warning('距离考试结束还有1分钟，请尽快提交！')
      }
    } else {
      // 时间到，自动提交
      clearInterval(timer!)
      timer = null
      ElMessage.warning('考试时间到，系统将自动提交试卷！')
      // 自动提交时使用特殊标记，失败时保留本地存储
      confirmSubmit(true)
    }
  }, 1000)
}

// 放弃考试
const handleGiveUp = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要放弃本次考试吗？放弃后将无法继续答题，已作答的内容将不会保存。',
      '放弃考试',
      {
        confirmButtonText: '确定放弃',
        cancelButtonText: '继续答题',
        type: 'warning'
      }
    )
    
    // 调用后端API放弃考试
    await giveUpExam(recordId.value)
    
    // 清除计时器
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    
    // 清除本地存储
    clearLocalAnswers()
    
    ElMessage.info('已放弃考试')
    router.push('/my/exam')
  } catch (error: any) {
    // 用户取消或API调用失败
    if (error !== 'cancel' && error.message) {
      console.error('放弃考试失败:', error)
      ElMessage.error(error.message || '放弃考试失败')
    }
  }
}

// 提交试卷
const handleSubmit = () => {
  submitDialogVisible.value = true
}

// 确认提交
const confirmSubmit = async (isAutoSubmit = false) => {
  // 防止重复提交
  if (submitting.value) {
    return
  }
  submitting.value = true
  try {
    // 构建提交数据
    const submitData = {
      recordId: recordId.value,
      answers: Object.entries(answers).map(([questionId, answer]) => ({
        questionId: Number(questionId),
        userAnswer: Array.isArray(answer) ? answer.join(',') : String(answer)
      }))
    }
    
    await submitExam(submitData)
    ElMessage.success('试卷提交成功！')
    
    // 清除计时器
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    
    // 清除本地存储
    clearLocalAnswers()
    
    submitDialogVisible.value = false
    
    // 跳转到成绩页面
    router.push('/my/result')
  } catch (error: any) {
    console.error('提交试卷失败:', error)
    
    if (isAutoSubmit) {
      // 自动提交失败时，保留本地存储并提示用户手动重试
      ElMessage.error('自动提交失败，您的答案已保存，请手动点击提交按钮重试！')
      // 重新启动计时器（给用户一些时间重试）
      remainingTime.value = 60 // 给予1分钟缓冲时间
      startTimer()
    } else {
      ElMessage.error(error.message || '提交试卷失败，答案已保存，请重试提交')
      // 提交失败时保留本地存储，用户可以重试
    }
  } finally {
    submitting.value = false
  }
}

// 页面离开前提醒
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  e.preventDefault()
  e.returnValue = ''
}

onMounted(() => {
  fetchExamDetail()
  window.addEventListener('beforeunload', handleBeforeUnload)
  // 添加防作弊监听
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  window.removeEventListener('beforeunload', handleBeforeUnload)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style lang="scss" scoped>
.exam-taking {
  .exam-header-card {
    margin-bottom: 20px;

    .exam-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;

      @media screen and (max-width: 768px) {
        flex-direction: column;
        gap: 16px;
      }

      .exam-info {
        .exam-title {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 12px;
        }

        .exam-meta {
          display: flex;
          flex-wrap: wrap;
          gap: 16px;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 14px;
            color: #606266;

            .el-icon {
              color: #409eff;
            }
          }
        }
      }

      .exam-actions {
        display: flex;
        gap: 10px;
      }
    }
  }

  .exam-content {
    .question-nav-col {
      margin-bottom: 20px;

      @media screen and (min-width: 992px) {
        margin-bottom: 0;
      }

      .question-nav-card {
        position: sticky;
        top: 20px;

        .question-nav {
          display: grid;
          grid-template-columns: repeat(5, 1fr);
          gap: 8px;

          .nav-item {
            width: 100%;
            aspect-ratio: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            transition: all 0.2s;

            &:hover {
              border-color: #409eff;
              color: #409eff;
            }

            &.answered {
              background-color: #67c23a;
              border-color: #67c23a;
              color: #fff;
            }

            &.current {
              border-color: #409eff;
              color: #409eff;
              font-weight: bold;
            }
          }
        }

        .nav-legend {
          display: flex;
          gap: 16px;
          margin-top: 16px;
          padding-top: 16px;
          border-top: 1px solid #ebeef5;

          .legend-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            color: #909399;

            .dot {
              width: 12px;
              height: 12px;
              border: 1px solid #dcdfe6;
              border-radius: 2px;

              &.answered {
                background-color: #67c23a;
                border-color: #67c23a;
              }
            }
          }
        }
      }
    }

    .question-content-col {
      .question-card {
        min-height: 400px;

        .question-header {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 20px;
          padding-bottom: 16px;
          border-bottom: 1px solid #ebeef5;

          .question-score {
            font-size: 14px;
            color: #e6a23c;
            font-weight: 500;
          }

          .question-index {
            margin-left: auto;
            font-size: 14px;
            color: #909399;
          }
        }

        .question-content {
          margin-bottom: 24px;

          .question-text {
            font-size: 16px;
            color: #303133;
            line-height: 1.8;
            margin: 0;
          }
        }

        .answer-section {
          margin-bottom: 24px;

          .answer-options {
            display: flex;
            flex-direction: column;
            gap: 16px;

            .answer-option {
              display: flex;
              align-items: flex-start;
              padding: 12px 16px;
              background-color: #f5f7fa;
              border-radius: 8px;
              transition: all 0.2s;

              &:hover {
                background-color: #ecf5ff;
              }

              .option-key {
                font-weight: 600;
                color: #409eff;
                margin-right: 8px;
              }

              .option-text {
                flex: 1;
                color: #606266;
                line-height: 1.6;
              }
            }
          }

          .answer-input {
            width: 100%;
          }
        }

        .question-actions {
          display: flex;
          justify-content: center;
          gap: 20px;
          padding-top: 20px;
          border-top: 1px solid #ebeef5;
        }
      }
    }
  }

  .warning-text {
    color: #e6a23c;
    font-weight: 500;
  }
}
</style>
