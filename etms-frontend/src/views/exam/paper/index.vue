<template>
  <div class="paper-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="试卷名称">
          <el-input v-model="searchForm.paperName" placeholder="请输入试卷名称" clearable />
        </el-form-item>
        <el-form-item label="试卷编码">
          <el-input v-model="searchForm.paperCode" placeholder="请输入试卷编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已停用" :value="2" />
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
          <span>试卷列表</span>
          <el-button v-if="canAdd" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="paperName" label="试卷名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="paperCode" label="试卷编码" width="120" />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column prop="passScore" label="及格分" width="80" align="center" />
        <el-table-column prop="duration" label="考试时长" width="100" align="center">
          <template #default="{ row }">
            {{ row.duration }}分钟
          </template>
        </el-table-column>
        <el-table-column prop="questionCount" label="题目数量" width="90" align="center" />
        <el-table-column label="考试时间" width="200">
          <template #default="{ row }">
            <template v-if="row.startTime && row.endTime">
              {{ formatDateTime(row.startTime) }}<br/>至 {{ formatDateTime(row.endTime) }}
            </template>
            <span v-else class="text-gray">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 0 && canEdit"
              type="primary"
              link
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button
              v-if="row.status === 0 && canPublish"
              type="success"
              link
              @click="handlePublish(row)"
            >发布</el-button>
            <el-button
              v-if="row.status === 1 && canPublish"
              type="warning"
              link
              @click="handleDisable(row)"
            >停用</el-button>
            <el-button
              v-if="(row.status === 0 || row.status === 2) && canDelete"
              type="danger"
              link
              @click="handleDelete(row)"
            >删除</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="试卷名称" prop="paperName">
              <el-input v-model="form.paperName" placeholder="请输入试卷名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试卷编码" prop="paperCode">
              <el-input v-model="form.paperCode" placeholder="请输入试卷编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="总分" prop="totalScore">
              <el-input-number
                v-model="form.totalScore"
                :min="1"
                :max="200"
                placeholder="请输入总分"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分数" prop="passScore">
              <el-input-number
                v-model="form.passScore"
                :min="0"
                :max="form.totalScore"
                placeholder="请输入及格分数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number
                v-model="form.duration"
                :min="1"
                :max="300"
                placeholder="分钟"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="考试时间" prop="timeRange">
              <el-date-picker
                v-model="form.timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="试卷说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入试卷说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="试卷详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="试卷名称" :span="2">{{ detailData.paperName }}</el-descriptions-item>
        <el-descriptions-item label="试卷编码">{{ detailData.paperCode }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)">
            {{ getStatusName(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总分">{{ detailData.totalScore }}分</el-descriptions-item>
        <el-descriptions-item label="及格分数">{{ detailData.passScore }}分</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ detailData.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ detailData.questionCount }}道</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(detailData.startTime) || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(detailData.endTime) || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="试卷说明" :span="2">
          {{ detailData.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getPaperList,
  getPaperDetail,
  createPaper,
  updatePaper,
  deletePaper,
  publishPaper,
  disablePaper
} from '@/api/exam'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 权限检查函数
const hasPermission = (permission: string): boolean => {
  const permissions = userStore.userInfo?.permissions || []
  const roleNames = userStore.userInfo?.roleNames || []
  // 管理员角色拥有所有权限（检查角色名称）
  if (roleNames.some(name => name === '超级管理员' || name === '管理员' || name === 'admin' || name === 'ADMIN')) return true
  return permissions.includes(permission)
}

// 检查是否有试卷管理相关权限
const canAdd = computed(() => hasPermission('exam:paper:add'))
const canEdit = computed(() => hasPermission('exam:paper:edit'))
const canDelete = computed(() => hasPermission('exam:paper:delete'))
const canPublish = computed(() => hasPermission('exam:paper:publish'))

// 搜索表单
const searchForm = reactive({
  paperName: '',
  paperCode: '',
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

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑试卷' : '新增试卷')
const formRef = ref<FormInstance>()

// 详情对话框
const detailVisible = ref(false)
const detailData = ref<any>({})

// 表单数据
const form = reactive({
  id: null as number | null,
  paperName: '',
  paperCode: '',
  totalScore: 100,
  passScore: 60,
  duration: 60,
  timeRange: [] as string[],
  description: ''
})

// 监听总分变化，自动调整及格分数
watch(() => form.totalScore, (newTotal) => {
  if (form.passScore > newTotal) {
    form.passScore = newTotal
  }
})

// 自定义验证规则：及格分不能大于总分且必须大于0
const validatePassScore = (rule: any, value: number, callback: any) => {
  if (value === undefined || value === null) {
    callback(new Error('请输入及格分数'))
  } else if (value <= 0) {
    callback(new Error('及格分数必须大于0'))
  } else if (value > form.totalScore) {
    callback(new Error('及格分数不能大于总分'))
  } else {
    callback()
  }
}

// 自定义验证规则：时间范围验证
const validateTimeRange = (rule: any, value: string[], callback: any) => {
  if (!value || value.length === 0) {
    callback() // 时间范围可选，不强制验证
  } else if (value.length !== 2) {
    callback(new Error('请选择完整的考试时间范围'))
  } else {
    const startTime = new Date(value[0]).getTime()
    const endTime = new Date(value[1]).getTime()
    if (isNaN(startTime) || isNaN(endTime)) {
      callback(new Error('时间格式不正确'))
    } else if (startTime >= endTime) {
      callback(new Error('开始时间必须早于结束时间'))
    } else {
      callback()
    }
  }
}

// 表单验证规则
const rules: FormRules = {
  paperName: [
    { required: true, message: '请输入试卷名称', trigger: 'blur' },
    { max: 100, message: '试卷名称不能超过100个字符', trigger: 'blur' }
  ],
  paperCode: [
    { required: true, message: '请输入试卷编码', trigger: 'blur' },
    { max: 50, message: '试卷编码不能超过50个字符', trigger: 'blur' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'blur' }
  ],
  passScore: [
    { required: true, message: '请输入及格分数', trigger: 'blur' },
    { validator: validatePassScore, trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入考试时长', trigger: 'blur' }
  ],
  timeRange: [
    { validator: validateTimeRange, trigger: 'change' }
  ]
}

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态名称
const getStatusName = (status: number) => {
  const names: Record<number, string> = {
    0: '草稿',
    1: '已发布',
    2: '已停用'
  }
  return names[status] || '未知'
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
    const res = await getPaperList({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取试卷列表失败')
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
    paperName: '',
    paperCode: '',
    status: null
  })
  handleSearch()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    paperName: '',
    paperCode: '',
    totalScore: 100,
    passScore: 60,
    duration: 60,
    timeRange: [],
    description: ''
  })
  formRef.value?.resetFields()
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
    const res = await getPaperDetail(row.id)
    const data = res.data
    Object.assign(form, {
      id: data.id,
      paperName: data.paperName,
      paperCode: data.paperCode,
      totalScore: data.totalScore || 100,
      passScore: data.passScore || 60,
      duration: data.duration || 60,
      timeRange: data.startTime && data.endTime ? [data.startTime, data.endTime] : [],
      description: data.description || ''
    })
    dialogVisible.value = true
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取试卷详情失败')
  }
}

// 查看详情
const handleView = async (row: any) => {
  try {
    const res = await getPaperDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取试卷详情失败')
  }
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该试卷吗？删除后无法恢复。', '提示', { type: 'warning' })
    await deletePaper(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 发布
const handlePublish = async (row: any) => {
  // 验证试卷是否已添加题目
  if (!row.questionCount || row.questionCount === 0) {
    ElMessage.warning('试卷未添加题目，无法发布')
    return
  }
  // 验证及格分数是否有效
  if (!row.passScore || row.passScore <= 0) {
    ElMessage.warning('及格分数必须大于0，请先编辑试卷设置及格分数')
    return
  }
  try {
    await ElMessageBox.confirm('确定要发布该试卷吗？发布后考生可以参加考试。', '提示', { type: 'warning' })
    await publishPaper(row.id)
    ElMessage.success('发布成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '发布失败')
    }
  }
}

// 停用
const handleDisable = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要停用该试卷吗？停用后考生将无法继续考试。', '提示', { type: 'warning' })
    await disablePaper(row.id)
    ElMessage.success('停用成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '停用失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    const data = {
      paperName: form.paperName,
      paperCode: form.paperCode,
      totalScore: form.totalScore,
      passScore: form.passScore,
      duration: form.duration,  // 使用duration字段名，后端通过@JsonProperty映射到examDuration
      startTime: form.timeRange?.[0] || null,
      endTime: form.timeRange?.[1] || null,
      description: form.description,
      status: 0
    }

    if (isEdit.value) {
      await updatePaper(form.id!, data)
      ElMessage.success('更新成功')
    } else {
      await createPaper(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.paper-management {
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
}
</style>
