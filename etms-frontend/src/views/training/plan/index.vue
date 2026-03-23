<template>
  <div class="training-plan">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="计划名称">
          <el-input v-model="searchForm.planName" placeholder="请输入计划名称" clearable />
        </el-form-item>
        <el-form-item label="计划类型">
          <el-select v-model="searchForm.planType" placeholder="请选择计划类型" clearable>
            <el-option label="必修" :value="1" />
            <el-option label="选修" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已结束" :value="3" />
            <el-option label="已归档" :value="4" />
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
          <span>培训计划列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="planName" label="计划名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="planCode" label="计划编码" width="120" />
        <el-table-column prop="planType" label="计划类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.planType === 1 ? 'danger' : 'success'">
              {{ row.planType === 1 ? '必修' : '选修' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="关联课程" width="150" show-overflow-tooltip />
        <el-table-column label="培训日期" width="200">
          <template #default="{ row }">
            {{ row.startDate }} 至 {{ row.endDate }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handlePublish(row)"
            >发布</el-button>
            <el-button
              v-if="row.status === 3"
              type="warning"
              link
              @click="handleArchive(row)"
            >归档</el-button>
            <el-button
              v-if="row.status === 0"
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
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="form.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划编码" prop="planCode">
              <el-input v-model="form.planCode" placeholder="请输入计划编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划类型" prop="planType">
              <el-select v-model="form.planType" placeholder="请选择计划类型" style="width: 100%">
                <el-option label="必修" :value="1" />
                <el-option label="选修" :value="2" />
              </el-select>
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
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="培训日期" prop="dateRange">
              <el-date-picker
                v-model="form.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目标类型" prop="targetType">
              <el-select v-model="form.targetType" placeholder="请选择目标类型" style="width: 100%">
                <el-option label="部门" :value="1" />
                <el-option label="岗位" :value="2" />
                <el-option label="个人" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否需要考试" prop="needExam">
              <el-radio-group v-model="form.needExam">
                <el-radio :value="1">是</el-radio>
                <el-radio :value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.needExam === 1">
          <el-col :span="12">
            <el-form-item label="及格分数" prop="passScore">
              <el-input-number
                v-model="form.passScore"
                :min="0"
                :max="100"
                placeholder="请输入及格分数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大补考次数" prop="maxRetake">
              <el-input-number
                v-model="form.maxRetake"
                :min="0"
                :max="10"
                placeholder="请输入最大补考次数"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最低学习时长" prop="minStudyTime">
              <el-input-number
                v-model="form.minStudyTime"
                :min="0"
                placeholder="分钟"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最低完成进度" prop="minProgress">
              <el-input-number
                v-model="form.minProgress"
                :min="0"
                :max="100"
                placeholder="%"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="计划描述" prop="planDesc">
          <el-input
            v-model="form.planDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入计划描述"
          />
        </el-form-item>
        <el-form-item label="培训目标" prop="planObjective">
          <el-input
            v-model="form.planObjective"
            type="textarea"
            :rows="2"
            placeholder="请输入培训目标"
          />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getPlanList,
  getPlanDetail,
  createPlan,
  updatePlan,
  deletePlan,
  publishPlan,
  archivePlan
} from '@/api/training'
import { getCourseListAll } from '@/api/course'

// 搜索表单
const searchForm = reactive({
  planName: '',
  planType: null as number | null,
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
const dialogTitle = computed(() => isEdit.value ? '编辑培训计划' : '新增培训计划')
const formRef = ref<FormInstance>()

// 课程列表
const courseList = ref<any[]>([])

// 表单数据
const form = reactive({
  id: null as number | null,
  planName: '',
  planCode: '',
  planType: 1 as number,
  courseId: null as number | null,
  dateRange: [] as string[],
  targetType: 1 as number,
  needExam: 0 as number,
  passScore: 60 as number,
  maxRetake: 3 as number,
  minStudyTime: 0 as number,
  minProgress: 100 as number,
  planDesc: '',
  planObjective: ''
})

// 表单验证规则
const rules: FormRules = {
  planName: [
    { required: true, message: '请输入计划名称', trigger: 'blur' }
  ],
  planCode: [
    { required: true, message: '请输入计划编码', trigger: 'blur' }
  ],
  planType: [
    { required: true, message: '请选择计划类型', trigger: 'change' }
  ],
  dateRange: [
    { required: true, message: '请选择培训日期', trigger: 'change' }
  ],
  targetType: [
    { required: true, message: '请选择目标类型', trigger: 'change' }
  ]
}

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: ''
  }
  return types[status] || 'info'
}

// 获取状态名称
const getStatusName = (status: number) => {
  const names: Record<number, string> = {
    0: '草稿',
    1: '已发布',
    2: '进行中',
    3: '已结束',
    4: '已归档'
  }
  return names[status] || '未知'
}

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getPlanList({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取培训计划列表失败')
  } finally {
    loading.value = false
  }
}

// 获取课程列表
const getCourseList = async () => {
  try {
    const res = await getCourseListAll()
    courseList.value = res.data || []
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取课程列表失败')
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
    planName: '',
    planType: null,
    status: null
  })
  handleSearch()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    planName: '',
    planCode: '',
    planType: 1,
    courseId: null,
    dateRange: [],
    targetType: 1,
    needExam: 0,
    passScore: 60,
    maxRetake: 3,
    minStudyTime: 0,
    minProgress: 100,
    planDesc: '',
    planObjective: ''
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
    const res = await getPlanDetail(row.id)
    const data = res.data
    Object.assign(form, {
      id: data.id,
      planName: data.planName,
      planCode: data.planCode,
      planType: data.planType,
      courseId: data.courseId,
      dateRange: data.startDate && data.endDate ? [data.startDate, data.endDate] : [],
      targetType: data.targetType || 1,
      needExam: data.needExam || 0,
      passScore: data.passScore || 60,
      maxRetake: data.maxRetake || 3,
      minStudyTime: data.minStudyTime || 0,
      minProgress: data.minProgress || 100,
      planDesc: data.planDesc || '',
      planObjective: data.planObjective || ''
    })
    dialogVisible.value = true
  } catch (error: any) {
    console.error(error)
    ElMessage.error(error.message || '获取培训计划详情失败')
  }
}

// 删除
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该培训计划吗？', '提示', { type: 'warning' })
    await deletePlan(row.id)
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
  try {
    await ElMessageBox.confirm('确定要发布该培训计划吗？发布后将无法修改。', '提示', { type: 'warning' })
    await publishPlan(row.id)
    ElMessage.success('发布成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '发布失败')
    }
  }
}

// 归档
const handleArchive = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要归档该培训计划吗？', '提示', { type: 'warning' })
    await archivePlan(row.id)
    ElMessage.success('归档成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '归档失败')
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
      planName: form.planName,
      planCode: form.planCode,
      planType: form.planType,
      courseId: form.courseId,
      startDate: form.dateRange?.[0] || null,
      endDate: form.dateRange?.[1] || null,
      targetType: form.targetType,
      needExam: form.needExam,
      passScore: form.passScore,
      maxRetake: form.maxRetake,
      minStudyTime: form.minStudyTime,
      minProgress: form.minProgress,
      planDesc: form.planDesc,
      planObjective: form.planObjective
    }

    if (isEdit.value) {
      await updatePlan(form.id!, data)
      ElMessage.success('更新成功')
    } else {
      await createPlan({ ...data, status: 0 })
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
  getCourseList()
})
</script>

<style lang="scss" scoped>
.training-plan {
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
