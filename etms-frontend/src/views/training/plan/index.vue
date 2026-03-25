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
              v-if="row.status === 1 || row.status === 2"
              type="danger"
              link
              @click="handleEnd(row)"
            >结束</el-button>
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
      @close="resetForm"
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
        <!-- 目标选择：部门 -->
        <el-row :gutter="20" v-if="form.targetType === 1">
          <el-col :span="24">
            <el-form-item label="目标部门" prop="targetSelection">
              <el-tree-select
                v-model="form.targetDeptIds"
                :data="deptList"
                multiple
                :render-after-expand="false"
                check-strictly
                placeholder="请选择目标部门"
                style="width: 100%"
                node-key="id"
                :props="{ label: 'deptName', children: 'children' }"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 目标选择：岗位 -->
        <el-row :gutter="20" v-if="form.targetType === 2">
          <el-col :span="24">
            <el-form-item label="目标岗位" prop="targetSelection">
              <el-select
                v-model="form.targetPositionIds"
                multiple
                filterable
                placeholder="请选择目标岗位"
                style="width: 100%"
              >
                <el-option
                  v-for="pos in positionList"
                  :key="pos.id"
                  :label="pos.positionName"
                  :value="pos.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 目标选择：个人 -->
        <el-row :gutter="20" v-if="form.targetType === 3">
          <el-col :span="24">
            <el-form-item label="目标人员" prop="targetSelection">
              <el-select
                v-model="form.targetUserIds"
                multiple
                filterable
                placeholder="请选择目标人员"
                style="width: 100%"
              >
                <el-option
                  v-for="user in userList"
                  :key="user.id"
                  :label="`${user.realName}(${user.username})`"
                  :value="user.id"
                />
              </el-select>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
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
  archivePlan,
  endPlan
} from '@/api/training'
import { getCourseListAll } from '@/api/course'
import { getDeptTree } from '@/api/dept'
import { getPositionListAll } from '@/api/position'
import { getUserList } from '@/api/user'

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

// 目标选项列表
const deptList = ref<any[]>([])
const positionList = ref<any[]>([])
const userList = ref<any[]>([])

// 表单数据
const form = reactive({
  id: null as number | null,
  planName: '',
  planCode: '',
  planType: 1 as number,
  courseId: null as number | null,
  dateRange: [] as string[],
  targetType: 1 as number,
  targetDeptIds: [] as number[],
  targetPositionIds: [] as number[],
  targetUserIds: [] as number[],
  needExam: 0 as number,
  passScore: 60 as number,
  maxRetake: 3 as number,
  minStudyTime: 0 as number,
  minProgress: 100 as number,
  planDesc: '',
  planObjective: ''
})

// 日期范围验证器 - 结束日期必须大于开始日期
const validateDateRange = (_rule: any, value: string[], callback: (error?: Error) => void) => {
  if (!value || value.length === 0) {
    callback(new Error('请选择培训日期'))
    return
  }
  if (value[0] && value[1]) {
    const startDate = new Date(value[0])
    const endDate = new Date(value[1])
    if (endDate <= startDate) {
      callback(new Error('结束日期必须大于开始日期'))
      return
    }
  }
  callback()
}

// 目标选择验证器
const validateTargetSelection = (_rule: any, value: any, callback: (error?: Error) => void) => {
  if (form.targetType === 1 && (!form.targetDeptIds || form.targetDeptIds.length === 0)) {
    callback(new Error('请选择目标部门'))
    return
  }
  if (form.targetType === 2 && (!form.targetPositionIds || form.targetPositionIds.length === 0)) {
    callback(new Error('请选择目标岗位'))
    return
  }
  if (form.targetType === 3 && (!form.targetUserIds || form.targetUserIds.length === 0)) {
    callback(new Error('请选择目标人员'))
    return
  }
  callback()
}

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
  courseId: [
    { required: true, message: '请选择关联课程', trigger: 'change' }
  ],
  dateRange: [
    { required: true, validator: validateDateRange, trigger: 'change' }
  ],
  targetType: [
    { required: true, message: '请选择目标类型', trigger: 'change' }
  ],
  targetSelection: [
    { required: true, validator: validateTargetSelection, trigger: 'change' }
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
    // 只传递有效的搜索参数
    const params: Record<string, any> = {
      current: pagination.current,
      size: pagination.size
    }
    if (searchForm.planName) params.planName = searchForm.planName
    if (searchForm.planType !== null) params.planType = searchForm.planType
    if (searchForm.status !== null) params.status = searchForm.status
    
    const res = await getPlanList(params)
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
    targetDeptIds: [],
    targetPositionIds: [],
    targetUserIds: [],
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
    // 解析目标ID
    let targetDeptIds: number[] = []
    let targetPositionIds: number[] = []
    let targetUserIds: number[] = []
    
    try {
      if (data.targetDeptIds) targetDeptIds = JSON.parse(data.targetDeptIds)
      if (data.targetPositionIds) targetPositionIds = JSON.parse(data.targetPositionIds)
      if (data.targetUserIds) targetUserIds = JSON.parse(data.targetUserIds)
    } catch (e) {
      console.warn('解析目标ID失败', e)
    }
    
    Object.assign(form, {
      id: data.id,
      planName: data.planName,
      planCode: data.planCode,
      planType: data.planType,
      courseId: data.courseId,
      dateRange: data.startDate && data.endDate ? [data.startDate, data.endDate] : [],
      targetType: data.targetType || 1,
      targetDeptIds,
      targetPositionIds,
      targetUserIds,
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
  // 验证必填字段是否完整
  const missingFields: string[] = []
  
  if (!row.planName || row.planName.trim() === '') {
    missingFields.push('计划名称')
  }
  if (!row.planCode || row.planCode.trim() === '') {
    missingFields.push('计划编码')
  }
  if (!row.planType) {
    missingFields.push('计划类型')
  }
  if (!row.courseId) {
    missingFields.push('关联课程')
  }
  if (!row.startDate || !row.endDate) {
    missingFields.push('培训日期')
  }
  if (!row.targetType) {
    missingFields.push('目标类型')
  } else {
    // 验证目标选择
    if (row.targetType === 1) {
      let targetDeptIds: number[] = []
      try {
        targetDeptIds = row.targetDeptIds ? JSON.parse(row.targetDeptIds) : []
        if (!Array.isArray(targetDeptIds)) {
          targetDeptIds = []
        }
      } catch (e) {
        console.warn('解析目标部门ID失败:', e)
        targetDeptIds = []
      }
      if (targetDeptIds.length === 0) {
        missingFields.push('目标部门')
      }
    } else if (row.targetType === 2) {
      let targetPositionIds: number[] = []
      try {
        targetPositionIds = row.targetPositionIds ? JSON.parse(row.targetPositionIds) : []
        if (!Array.isArray(targetPositionIds)) {
          targetPositionIds = []
        }
      } catch (e) {
        console.warn('解析目标岗位ID失败:', e)
        targetPositionIds = []
      }
      if (targetPositionIds.length === 0) {
        missingFields.push('目标岗位')
      }
    } else if (row.targetType === 3) {
      let targetUserIds: number[] = []
      try {
        targetUserIds = row.targetUserIds ? JSON.parse(row.targetUserIds) : []
        if (!Array.isArray(targetUserIds)) {
          targetUserIds = []
        }
      } catch (e) {
        console.warn('解析目标人员ID失败:', e)
        targetUserIds = []
      }
      if (targetUserIds.length === 0) {
        missingFields.push('目标人员')
      }
    }
  }
  
  if (missingFields.length > 0) {
    ElMessage.warning(`以下必填字段未完善：${missingFields.join('、')}，请先编辑完善后再发布`)
    return
  }
  
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

// 结束
const handleEnd = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要结束该培训计划吗？结束后将无法继续学习。', '提示', { type: 'warning' })
    await endPlan(row.id)
    ElMessage.success('结束成功')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(error.message || '结束失败')
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
  try {
    const valid = await formRef.value?.validate()
    if (!valid) {
      ElMessage.warning('请检查表单填写是否正确')
      return
    }
  } catch {
    ElMessage.warning('表单验证失败，请检查输入')
    return
  }

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
      targetDeptIds: form.targetDeptIds.length > 0 ? JSON.stringify(form.targetDeptIds) : null,
      targetPositionIds: form.targetPositionIds.length > 0 ? JSON.stringify(form.targetPositionIds) : null,
      targetUserIds: form.targetUserIds.length > 0 ? JSON.stringify(form.targetUserIds) : null,
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

// 获取部门列表
const getDeptList = async () => {
  try {
    const res = await getDeptTree()
    deptList.value = res.data || []
  } catch (error: any) {
    console.error(error)
  }
}

// 获取岗位列表
const getPositionListData = async () => {
  try {
    const res = await getPositionListAll()
    positionList.value = res.data || []
  } catch (error: any) {
    console.error(error)
  }
}

// 获取用户列表
const getUserListAll = async () => {
  try {
    const res = await getUserList({ current: 1, size: 1000, status: 1 })
    userList.value = res.data?.records || []
  } catch (error: any) {
    console.error(error)
  }
}

// 监听目标类型变化，清空其他类型的目标选择
watch(() => form.targetType, (newType, oldType) => {
  // 仅在类型实际变化时清空（排除初始化和编辑加载的情况）
  if (oldType !== undefined && newType !== oldType && dialogVisible.value) {
    // 清空所有目标选择
    form.targetDeptIds = []
    form.targetPositionIds = []
    form.targetUserIds = []
    // 清除目标选择的验证错误
    formRef.value?.clearValidate('targetSelection')
  }
})

onMounted(() => {
  getList()
  getCourseList()
  getDeptList()
  getPositionListData()
  getUserListAll()
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
