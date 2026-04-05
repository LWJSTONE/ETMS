<template>
  <div class="course-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.courseName" placeholder="请输入课程名称" clearable />
        </el-form-item>
        <el-form-item label="课程编码">
          <el-input v-model="searchForm.courseCode" placeholder="请输入课程编码" clearable />
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="searchForm.courseType" placeholder="请选择课程类型" clearable>
            <el-option label="视频" :value="1" />
            <el-option label="文档" :value="2" />
            <el-option label="直播" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已上架" :value="2" />
            <el-option label="已下架" :value="3" />
            <el-option label="审核驳回" :value="4" />
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
          <span>课程列表</span>
          <el-button v-if="canAdd" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="courseName" label="课程名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="courseCode" label="课程编码" width="120" />
        <el-table-column prop="courseType" label="课程类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getCourseTypeTag(row.courseType)">
              {{ getCourseTypeText(row.courseType) }}
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
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="collectCount" label="收藏" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <!-- 草稿状态: 编辑、删除、提交审核 -->
            <template v-if="row.status === 0">
              <el-button v-if="canEdit" type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="canDelete" type="danger" link @click="handleDelete(row)">删除</el-button>
              <el-button v-if="canEdit" type="warning" link @click="handleSubmitAudit(row)">提交审核</el-button>
            </template>
            <!-- 待审核状态: 审核通过、审核驳回 -->
            <template v-else-if="row.status === 1">
              <el-button v-if="canAudit" type="success" link @click="handleAudit(row, 2)">审核通过</el-button>
              <el-button v-if="canAudit" type="danger" link @click="handleAudit(row, 4)">审核驳回</el-button>
            </template>
            <!-- 已上架状态: 下架 -->
            <template v-else-if="row.status === 2">
              <el-button v-if="canPublish" type="warning" link @click="handleUnpublish(row)">下架</el-button>
            </template>
            <!-- 已下架状态: 上架 -->
            <template v-else-if="row.status === 3">
              <el-button v-if="canPublish" type="success" link @click="handlePublish(row)">上架</el-button>
            </template>
            <!-- 审核驳回状态: 编辑、删除、重新提交审核 -->
            <template v-else-if="row.status === 4">
              <el-button v-if="canEdit" type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="canDelete" type="danger" link @click="handleDelete(row)">删除</el-button>
              <el-button v-if="canEdit" type="warning" link @click="handleSubmitAudit(row)">重新提交</el-button>
            </template>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程名称" prop="courseName">
              <el-input v-model="form.courseName" placeholder="请输入课程名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程编码" prop="courseCode">
              <el-input v-model="form.courseCode" placeholder="请输入课程编码" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程类型" prop="courseType">
              <el-select v-model="form.courseType" placeholder="请选择课程类型" style="width: 100%">
                <el-option label="视频" :value="1" />
                <el-option label="文档" :value="2" />
                <el-option label="直播" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 100%">
                <el-option label="入门" :value="1" />
                <el-option label="初级" :value="2" />
                <el-option label="中级" :value="3" />
                <el-option label="高级" :value="4" />
                <el-option label="专家" :value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图片" prop="coverImage">
              <el-input v-model="form.coverImage" placeholder="请输入封面图片URL" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="时长(分钟)" prop="duration">
              <el-input-number v-model="form.duration" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学分" prop="credit">
              <el-input-number v-model="form.credit" :min="0" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程描述" prop="courseDesc">
          <el-input
            v-model="form.courseDesc"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 审核驳回对话框 -->
    <el-dialog v-model="auditDialogVisible" title="审核驳回" width="500px" @open="handleAuditDialogOpen">
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <el-form-item label="驳回原因" prop="auditRemark">
          <el-input
            ref="auditRemarkInput"
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="4"
            placeholder="请输入驳回原因（必填，至少5个字符）"
            maxlength="200"
            show-word-limit
            :class="{ 'is-error': auditRemarkError }"
          />
          <div v-if="auditRemarkError" class="error-tip">{{ auditRemarkError }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleAuditCancel">取消</el-button>
        <el-button type="primary" @click="confirmAudit" :loading="auditLoading">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getCourseList,
  getCourseDetail,
  createCourse,
  updateCourse,
  deleteCourse,
  submitCourseAudit,
  auditCourse,
  publishCourse,
  unpublishCourse
} from '@/api/course'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 获取友好的错误提示信息
const getFriendlyErrorMessage = (error: any, defaultMsg: string): string => {
  if (error.response?.data?.message) {
    return error.response.data.message
  }
  if (error.response?.data?.msg) {
    return error.response.data.msg
  }
  if (error.message) {
    if (error.message.includes('timeout')) {
      return '请求超时，请稍后重试'
    }
    if (error.message.includes('Network Error')) {
      return '网络连接失败，请检查网络设置'
    }
    return error.message
  }
  return defaultMsg
}

// 权限检查计算属性
const canAdd = computed(() => userStore.hasPermission('training:course:add'))
const canEdit = computed(() => userStore.hasPermission('training:course:edit'))
const canDelete = computed(() => userStore.hasPermission('training:course:delete'))
const canAudit = computed(() => userStore.hasPermission('training:course:audit'))
const canPublish = computed(() => userStore.hasPermission('training:course:publish'))

// 搜索表单
const searchForm = reactive({
  courseName: '',
  courseCode: '',
  courseType: null as number | null,
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
const dialogTitle = computed(() => isEdit.value ? '编辑课程' : '新增课程')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表单数据
const form = reactive({
  id: null as number | null,
  courseName: '',
  courseCode: '',
  courseType: null as number | null,
  coverImage: '',
  courseDesc: '',  // 修复：与后端字段名一致
  difficulty: null as number | null,
  duration: 0,
  credit: 0
})

// 表单验证规则
const rules: FormRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { max: 100, message: '课程名称不能超过100个字符', trigger: 'blur' }
  ],
  courseCode: [
    { required: true, message: '请输入课程编码', trigger: 'blur' },
    { max: 50, message: '课程编码不能超过50个字符', trigger: 'blur' }
  ],
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

// 审核相关
const auditDialogVisible = ref(false)
const auditLoading = ref(false)
const auditFormRef = ref<FormInstance>()
const auditRemarkInput = ref<any>(null)
const auditRemarkError = ref('')
const auditForm = reactive({
  id: null as number | null,
  status: null as number | null,
  auditRemark: ''
})

// 审核表单验证规则
const auditRules: FormRules = {
  auditRemark: [
    { required: true, message: '请输入驳回原因', trigger: 'blur' },
    { min: 5, message: '驳回原因至少需要5个字符', trigger: 'blur' }
  ]
}

// 获取课程类型标签
const getCourseTypeTag = (type: number) => {
  const map: Record<number, string> = { 1: 'primary', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

// 获取课程类型文本
const getCourseTypeText = (type: number) => {
  const map: Record<number, string> = { 1: '视频', 2: '文档', 3: '直播' }
  return map[type] || '未知'
}

// 获取难度标签
const getDifficultyTag = (level: number) => {
  const map: Record<number, string> = { 1: 'success', 2: '', 3: 'warning', 4: 'danger', 5: 'danger' }
  return map[level] || 'info'
}

// 获取难度文本
const getDifficultyText = (level: number) => {
  const map: Record<number, string> = { 1: '入门', 2: '初级', 3: '中级', 4: '高级', 5: '专家' }
  return map[level] || '未知'
}

// 获取状态标签
const getStatusTag = (status: number) => {
  const map: Record<number, string> = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'danger' }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '草稿', 1: '待审核', 2: '已上架', 3: '已下架', 4: '审核驳回' }
  return map[status] || '未知'
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      current: pagination.current,
      size: pagination.size
    }
    if (searchForm.courseName) params.courseName = searchForm.courseName
    if (searchForm.courseCode) params.courseCode = searchForm.courseCode
    if (searchForm.courseType !== null) params.courseType = searchForm.courseType
    if (searchForm.status !== null) params.status = searchForm.status

    const res = await getCourseList(params)
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } catch (error: any) {
    console.error('获取课程列表失败:', error)
    ElMessage.error(getFriendlyErrorMessage(error, '获取课程列表失败'))
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
    courseName: '',
    courseCode: '',
    courseType: null,
    status: null
  })
  handleSearch()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    courseName: '',
    courseCode: '',
    courseType: null,
    coverImage: '',
    courseDesc: '',  // 修复：与后端字段名一致
    difficulty: null,
    duration: 0,
    credit: 0
  })
  formRef.value?.resetFields()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
  // 对话框打开后清除验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 编辑
const handleEdit = async (row: any) => {
  isEdit.value = true
  try {
    // 修复：调用详情接口获取最新数据，而非直接使用列表数据
    const res = await getCourseDetail(row.id)
    const data = res
    resetForm()
    Object.assign(form, {
      id: data.id,
      courseName: data.courseName,
      courseCode: data.courseCode,
      courseType: data.courseType,
      coverImage: data.coverImage,
      courseDesc: data.courseDesc,
      difficulty: data.difficulty,
      duration: data.duration || 0,
      credit: data.credit || 0
    })
    dialogVisible.value = true
    // 对话框打开后清除验证状态
    nextTick(() => {
      formRef.value?.clearValidate()
    })
  } catch (error: any) {
    console.error('获取课程详情失败:', error)
    ElMessage.error(getFriendlyErrorMessage(error, '获取课程详情失败'))
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    const valid = await formRef.value?.validate()
    if (!valid) return
  } catch {
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCourse(form.id!, form)
      ElMessage.success('更新成功')
    } else {
      await createCourse(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(getFriendlyErrorMessage(error, '保存课程失败'))
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该课程吗？', '提示', { type: 'warning' })
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(getFriendlyErrorMessage(error, '删除课程失败'))
    }
  }
}

// 提交审核
const handleSubmitAudit = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要提交审核吗？', '提示', { type: 'info' })
    await submitCourseAudit(row.id)
    ElMessage.success('提交成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('提交审核失败:', error)
      ElMessage.error(getFriendlyErrorMessage(error, '提交审核失败'))
    }
  }
}

// 审核
const handleAudit = async (row: any, status: number) => {
  if (status === 4) {
    // 审核驳回，打开对话框输入原因
    auditForm.id = row.id
    auditForm.status = status
    auditForm.auditRemark = ''
    auditRemarkError.value = ''
    auditDialogVisible.value = true
  } else {
    // 审核通过
    try {
      await ElMessageBox.confirm('确定审核通过吗？', '提示', { type: 'info' })
      await auditCourse(row.id, { status })
      ElMessage.success('审核成功')
      getList()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('审核失败:', error)
        ElMessage.error(getFriendlyErrorMessage(error, '审核操作失败'))
      }
    }
  }
}

// 审核对话框打开时的处理
const handleAuditDialogOpen = () => {
  // 清除之前的错误状态
  auditRemarkError.value = ''
  // 延迟聚焦，确保DOM已渲染
  setTimeout(() => {
    auditRemarkInput.value?.focus()
  }, 100)
}

// 取消审核驳回
const handleAuditCancel = () => {
  auditDialogVisible.value = false
  auditFormRef.value?.resetFields()
  auditRemarkError.value = ''
}

// 确认审核驳回
const confirmAudit = async () => {
  // 表单验证
  const valid = await auditFormRef.value?.validate().catch(() => false)
  if (!valid) {
    auditRemarkError.value = '请输入驳回原因（至少5个字符）'
    return
  }
  
  // 额外验证驳回原因长度
  if (!auditForm.auditRemark || auditForm.auditRemark.trim().length < 5) {
    auditRemarkError.value = '驳回原因至少需要5个字符'
    return
  }
  
  auditRemarkError.value = ''
  auditLoading.value = true
  try {
    await auditCourse(auditForm.id!, { status: auditForm.status!, auditRemark: auditForm.auditRemark })
    ElMessage.success('审核驳回成功')
    auditDialogVisible.value = false
    auditFormRef.value?.resetFields()
    getList()
  } catch (error: any) {
    console.error('审核失败:', error)
    ElMessage.error(getFriendlyErrorMessage(error, '审核驳回失败'))
  } finally {
    auditLoading.value = false
  }
}

// 上架
const handlePublish = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要上架该课程吗？', '提示', { type: 'info' })
    await publishCourse(row.id)
    ElMessage.success('上架成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('上架失败:', error)
      ElMessage.error(getFriendlyErrorMessage(error, '上架课程失败'))
    }
  }
}

// 下架
const handleUnpublish = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要下架该课程吗？', '提示', { type: 'warning' })
    await unpublishCourse(row.id)
    ElMessage.success('下架成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
      ElMessage.error(getFriendlyErrorMessage(error, '下架课程失败'))
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.course-management {
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
}

.error-tip {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1;
  padding-top: 4px;
}

:deep(.is-error) {
  .el-textarea__inner {
    border-color: #f56c6c;
  }
}
</style>
