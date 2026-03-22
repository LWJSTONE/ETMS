<template>
  <div class="question-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="题目内容">
          <el-input
            v-model="searchForm.questionContent"
            placeholder="请输入题目内容"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="题目类型">
          <el-select v-model="searchForm.questionType" placeholder="请选择题目类型" clearable style="width: 140px">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="判断题" :value="3" />
            <el-option label="填空题" :value="4" />
            <el-option label="简答题" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="searchForm.difficulty" placeholder="请选择难度" clearable style="width: 120px">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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
          <span>题目列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="questionContent" label="题目内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="question-content" v-html="row.questionContent"></div>
          </template>
        </el-table-column>
        <el-table-column prop="questionType" label="题目类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getQuestionTypeTag(row.questionType)">
              {{ getQuestionTypeText(row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTag(row.difficulty)" size="small">
              {{ getDifficultyText(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column prop="answer" label="正确答案" width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题目类型" prop="questionType">
              <el-select
                v-model="form.questionType"
                placeholder="请选择题目类型"
                style="width: 100%"
                @change="handleQuestionTypeChange"
              >
                <el-option label="单选题" :value="1" />
                <el-option label="多选题" :value="2" />
                <el-option label="判断题" :value="3" />
                <el-option label="填空题" :value="4" />
                <el-option label="简答题" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
                <el-option label="简单" :value="1" />
                <el-option label="中等" :value="2" />
                <el-option label="困难" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="form.score" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联课程" prop="courseId">
              <el-select
                v-model="form.courseId"
                placeholder="请选择关联课程"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="course in courseList"
                  :key="course.id"
                  :label="course.courseName"
                  :value="course.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题目内容" prop="questionContent">
          <el-input
            v-model="form.questionContent"
            type="textarea"
            :rows="4"
            placeholder="请输入题目内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>

        <!-- 选项区域：单选题、多选题显示 -->
        <template v-if="form.questionType === 1 || form.questionType === 2">
          <el-divider content-position="left">选项</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="选项A" prop="optionA">
                <el-input v-model="form.optionA" placeholder="请输入选项A" maxlength="500" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选项B" prop="optionB">
                <el-input v-model="form.optionB" placeholder="请输入选项B" maxlength="500" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="选项C" prop="optionC">
                <el-input v-model="form.optionC" placeholder="请输入选项C" maxlength="500" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="选项D" prop="optionD">
                <el-input v-model="form.optionD" placeholder="请输入选项D" maxlength="500" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="正确答案" prop="answer">
            <template v-if="form.questionType === 1">
              <el-radio-group v-model="form.answer">
                <el-radio value="A">A</el-radio>
                <el-radio value="B">B</el-radio>
                <el-radio value="C">C</el-radio>
                <el-radio value="D">D</el-radio>
              </el-radio-group>
            </template>
            <template v-else>
              <el-checkbox-group v-model="multiAnswer">
                <el-checkbox value="A">A</el-checkbox>
                <el-checkbox value="B">B</el-checkbox>
                <el-checkbox value="C">C</el-checkbox>
                <el-checkbox value="D">D</el-checkbox>
              </el-checkbox-group>
            </template>
          </el-form-item>
        </template>

        <!-- 判断题选项 -->
        <template v-if="form.questionType === 3">
          <el-divider content-position="left">选项</el-divider>
          <el-form-item label="正确答案" prop="answer">
            <el-radio-group v-model="form.answer">
              <el-radio value="正确">正确</el-radio>
              <el-radio value="错误">错误</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <!-- 填空题答案 -->
        <template v-if="form.questionType === 4">
          <el-divider content-position="left">答案</el-divider>
          <el-form-item label="正确答案" prop="answer">
            <el-input
              v-model="form.answer"
              type="textarea"
              :rows="2"
              placeholder="请输入正确答案，多个空用 | 分隔"
              maxlength="500"
            />
          </el-form-item>
        </template>

        <!-- 简答题答案 -->
        <template v-if="form.questionType === 5">
          <el-divider content-position="left">答案</el-divider>
          <el-form-item label="参考答案" prop="answer">
            <el-input
              v-model="form.answer"
              type="textarea"
              :rows="4"
              placeholder="请输入参考答案"
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
        </template>

        <el-divider content-position="left">其他</el-divider>
        <el-form-item label="答案解析" prop="analysis">
          <el-input
            v-model="form.analysis"
            type="textarea"
            :rows="3"
            placeholder="请输入答案解析"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getQuestionList,
  getQuestionDetail,
  addQuestion,
  updateQuestion,
  deleteQuestion
} from '@/api/exam'
import { getCourseListAll } from '@/api/course'

const route = useRoute()
const pageTitle = route.meta?.title || '题库管理'

// 搜索表单
const searchForm = reactive({
  questionContent: '',
  questionType: null as number | null,
  difficulty: null as number | null,
  status: null as number | null
})

// 表格数据
const tableData = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 课程列表
const courseList = ref<any[]>([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑题目' : '新增题目')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 多选题答案
const multiAnswer = ref<string[]>([])

// 表单数据
const form = reactive({
  id: null as number | null,
  questionContent: '',
  questionType: null as number | null,
  difficulty: null as number | null,
  courseId: null as number | null,
  answer: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  analysis: '',
  score: 10,
  status: 1
})

// 监听多选题答案变化
watch(multiAnswer, (val) => {
  if (form.questionType === 2) {
    form.answer = val.sort().join(',')
  }
})

// 表单验证规则
const rules = computed<FormRules>(() => ({
  questionContent: [
    { required: true, message: '请输入题目内容', trigger: 'blur' },
    { max: 1000, message: '题目内容不能超过1000个字符', trigger: 'blur' }
  ],
  questionType: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }],
  answer: [{ required: true, message: '请选择正确答案', trigger: 'change' }],
  optionA: form.questionType === 1 || form.questionType === 2
    ? [{ required: true, message: '请输入选项A', trigger: 'blur' }] as any
    : [],
  optionB: form.questionType === 1 || form.questionType === 2
    ? [{ required: true, message: '请输入选项B', trigger: 'blur' }] as any
    : []
}))

// 获取题目类型标签
const getQuestionTypeTag = (type: number) => {
  const map: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info',
    5: 'danger'
  }
  return map[type] || 'info'
}

// 获取题目类型文本
const getQuestionTypeText = (type: number) => {
  const map: Record<number, string> = {
    1: '单选题',
    2: '多选题',
    3: '判断题',
    4: '填空题',
    5: '简答题'
  }
  return map[type] || '未知'
}

// 获取难度标签
const getDifficultyTag = (level: number) => {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[level] || 'info'
}

// 获取难度文本
const getDifficultyText = (level: number) => {
  const map: Record<number, string> = { 1: '简单', 2: '中等', 3: '困难' }
  return map[level] || '未知'
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size
    }
    if (searchForm.questionContent) params.questionContent = searchForm.questionContent
    if (searchForm.questionType !== null) params.questionType = searchForm.questionType
    if (searchForm.difficulty !== null) params.difficulty = searchForm.difficulty
    if (searchForm.status !== null) params.status = searchForm.status

    const res = await getQuestionList(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('获取题目列表失败:', error)
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
    questionContent: '',
    questionType: null,
    difficulty: null,
    status: null
  })
  handleSearch()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    questionContent: '',
    questionType: null,
    difficulty: null,
    courseId: null,
    answer: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    analysis: '',
    score: 10,
    status: 1
  })
  multiAnswer.value = []
  formRef.value?.resetFields()
}

// 题目类型变化处理
const handleQuestionTypeChange = () => {
  form.answer = ''
  form.optionA = ''
  form.optionB = ''
  form.optionC = ''
  form.optionD = ''
  multiAnswer.value = []
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: any) => {
  isEdit.value = true
  try {
    // 获取详情
    const res = await getQuestionDetail(row.id)
    const data = res.data || row
    Object.assign(form, {
      id: data.id,
      questionContent: data.questionContent,
      questionType: data.questionType,
      difficulty: data.difficulty,
      courseId: data.courseId,
      answer: data.answer,
      optionA: data.optionA || '',
      optionB: data.optionB || '',
      optionC: data.optionC || '',
      optionD: data.optionD || '',
      analysis: data.analysis || '',
      score: data.score || 10,
      status: data.status ?? 1
    })
    // 处理多选题答案
    if (form.questionType === 2 && form.answer) {
      multiAnswer.value = form.answer.split(',')
    }
    dialogVisible.value = true
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = { ...form }
    // 判断题设置固定选项
    if (submitData.questionType === 3) {
      submitData.optionA = '正确'
      submitData.optionB = '错误'
      submitData.optionC = ''
      submitData.optionD = ''
    }
    // 填空题和简答题清空选项
    if (submitData.questionType === 4 || submitData.questionType === 5) {
      submitData.optionA = ''
      submitData.optionB = ''
      submitData.optionC = ''
      submitData.optionD = ''
    }

    if (isEdit.value) {
      await updateQuestion(form.id!, submitData)
      ElMessage.success('更新成功')
    } else {
      await addQuestion(submitData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定要删除该题目吗？', '提示', { type: 'warning' })
  try {
    await deleteQuestion(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 获取课程列表
const getCourses = async () => {
  try {
    const res = await getCourseListAll()
    courseList.value = res.data || []
  } catch (error) {
    console.error('获取课程列表失败:', error)
  }
}

onMounted(() => {
  getList()
  getCourses()
})
</script>

<style lang="scss" scoped>
.question-management {
  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .question-content {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .el-pagination {
      margin-top: 20px;
      justify-content: flex-end;
    }
  }

  :deep(.el-divider__text) {
    font-weight: 500;
    color: #606266;
  }
}
</style>
