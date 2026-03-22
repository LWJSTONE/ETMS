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
          <el-button type="primary" @click="handleAdd">
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
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              <el-button type="warning" link @click="handleSubmitAudit(row)">提交审核</el-button>
            </template>
            <!-- 待审核状态: 审核通过、审核驳回 -->
            <template v-else-if="row.status === 1">
              <el-button type="success" link @click="handleAudit(row, 2)">审核通过</el-button>
              <el-button type="danger" link @click="handleAudit(row, 4)">审核驳回</el-button>
            </template>
            <!-- 已上架状态: 下架 -->
            <template v-else-if="row.status === 2">
              <el-button type="warning" link @click="handleUnpublish(row)">下架</el-button>
            </template>
            <!-- 已下架状态: 上架 -->
            <template v-else-if="row.status === 3">
              <el-button type="success" link @click="handlePublish(row)">上架</el-button>
            </template>
            <!-- 审核驳回状态: 编辑、删除、重新提交审核 -->
            <template v-else-if="row.status === 4">
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              <el-button type="warning" link @click="handleSubmitAudit(row)">重新提交</el-button>
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
          <el-col :span="12">
            <el-form-item label="课程分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryTreeOptions"
                :props="{ value: 'id', label: 'categoryName', children: 'children' }"
                value-key="id"
                placeholder="请选择课程分类"
                check-strictly
                clearable
                :render-after-expand="false"
                style="width: 100%"
              />
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
    <el-dialog v-model="auditDialogVisible" title="审核驳回" width="500px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="驳回原因">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit" :loading="auditLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getCourseList,
  createCourse,
  updateCourse,
  deleteCourse,
  submitCourseAudit,
  auditCourse,
  publishCourse,
  unpublishCourse
} from '@/api/course'
import { getCategoryTree, type Category } from '@/api/category'

const route = useRoute()
const pageTitle = route.meta?.title || '课程管理'

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

// 分类树选项
const categoryTreeOptions = ref<Category[]>([])

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
  categoryId: null as number | null,
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
const auditForm = reactive({
  id: null as number | null,
  status: null as number | null,
  auditRemark: ''
})

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
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('获取课程列表失败:', error)
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
    categoryId: null,
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
}

// 编辑
const handleEdit = async (row: any) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    courseName: row.courseName,
    courseCode: row.courseCode,
    categoryId: row.categoryId,
    courseType: row.courseType,
    coverImage: row.coverImage,
    courseDesc: row.courseDesc || row.description,  // 修复：兼容两种字段名
    difficulty: row.difficulty,
    duration: row.duration,
    credit: row.credit
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

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
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定要删除该课程吗？', '提示', { type: 'warning' })
  try {
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 提交审核
const handleSubmitAudit = async (row: any) => {
  await ElMessageBox.confirm('确定要提交审核吗？', '提示', { type: 'info' })
  try {
    await submitCourseAudit(row.id)
    ElMessage.success('提交成功')
    getList()
  } catch (error) {
    console.error('提交审核失败:', error)
  }
}

// 审核
const handleAudit = async (row: any, status: number) => {
  if (status === 4) {
    // 审核驳回，打开对话框输入原因
    auditForm.id = row.id
    auditForm.status = status
    auditForm.auditRemark = ''
    auditDialogVisible.value = true
  } else {
    // 审核通过
    await ElMessageBox.confirm('确定审核通过吗？', '提示', { type: 'info' })
    try {
      await auditCourse(row.id, { status })
      ElMessage.success('审核成功')
      getList()
    } catch (error) {
      console.error('审核失败:', error)
    }
  }
}

// 确认审核驳回
const confirmAudit = async () => {
  if (!auditForm.auditRemark) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  auditLoading.value = true
  try {
    await auditCourse(auditForm.id!, { status: auditForm.status!, auditRemark: auditForm.auditRemark })
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    getList()
  } catch (error) {
    console.error('审核失败:', error)
  } finally {
    auditLoading.value = false
  }
}

// 上架
const handlePublish = async (row: any) => {
  await ElMessageBox.confirm('确定要上架该课程吗？', '提示', { type: 'info' })
  try {
    await publishCourse(row.id)
    ElMessage.success('上架成功')
    getList()
  } catch (error) {
    console.error('上架失败:', error)
  }
}

// 下架
const handleUnpublish = async (row: any) => {
  await ElMessageBox.confirm('确定要下架该课程吗？', '提示', { type: 'warning' })
  try {
    await unpublishCourse(row.id)
    ElMessage.success('下架成功')
    getList()
  } catch (error) {
    console.error('下架失败:', error)
  }
}

// 获取分类树
const getCategoryTreeData = async () => {
  try {
    const res = await getCategoryTree()
    const treeData = res.data || []
    // 设置分类树选项（添加顶级节点选项）
    categoryTreeOptions.value = [
      { id: 0, parentId: null, categoryName: '顶级分类', categoryCode: '', level: 0, sortOrder: 0, icon: null, status: 1, children: treeData }
    ] as Category[]
  } catch (error) {
    console.error('获取分类树失败:', error)
  }
}

onMounted(() => {
  getList()
  getCategoryTreeData()
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
</style>
