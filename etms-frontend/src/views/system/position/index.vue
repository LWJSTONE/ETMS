<template>
  <div class="position-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="岗位名称">
          <el-input v-model="searchForm.positionName" placeholder="请输入岗位名称" clearable />
        </el-form-item>
        <el-form-item label="岗位编码">
          <el-input v-model="searchForm.positionCode" placeholder="请输入岗位编码" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
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
          <span>岗位列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="positionName" label="岗位名称" width="150" />
        <el-table-column prop="positionCode" label="岗位编码" width="150" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="岗位名称" prop="positionName">
          <el-input v-model="form.positionName" placeholder="请输入岗位名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="岗位编码" prop="positionCode">
          <el-input v-model="form.positionCode" placeholder="请输入岗位编码" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="200"
            show-word-limit
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

// 岗位数据类型
interface PositionItem {
  id: number
  positionName: string
  positionCode: string
  sortOrder: number
  status: number
  remark: string
  createTime: string
}

// 搜索表单
const searchForm = reactive({
  positionName: '',
  positionCode: '',
  status: null as number | null
})

// 表格数据
const tableData = ref<PositionItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑岗位' : '新增岗位')
const formRef = ref<FormInstance>()

// 表单数据
const form = reactive({
  id: null as number | null,
  positionName: '',
  positionCode: '',
  sortOrder: 0,
  status: 1,
  remark: ''
})

// 表单验证规则
const rules: FormRules = {
  positionName: [
    { required: true, message: '请输入岗位名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  positionCode: [
    { required: true, message: '请输入岗位编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '编码只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 模拟岗位数据
const mockPositionData: PositionItem[] = [
  {
    id: 1,
    positionName: '总经理',
    positionCode: 'CEO',
    sortOrder: 1,
    status: 1,
    remark: '公司最高管理者',
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    positionName: '部门经理',
    positionCode: 'DEPT_MANAGER',
    sortOrder: 2,
    status: 1,
    remark: '部门负责人',
    createTime: '2024-01-01 10:05:00'
  },
  {
    id: 3,
    positionName: '培训管理员',
    positionCode: 'TRAINING_ADMIN',
    sortOrder: 3,
    status: 1,
    remark: '负责培训计划制定与执行',
    createTime: '2024-01-01 10:10:00'
  },
  {
    id: 4,
    positionName: '培训讲师',
    positionCode: 'TRAINER',
    sortOrder: 4,
    status: 1,
    remark: '负责课程授课',
    createTime: '2024-01-01 10:15:00'
  },
  {
    id: 5,
    positionName: '普通员工',
    positionCode: 'EMPLOYEE',
    sortOrder: 5,
    status: 1,
    remark: '普通员工岗位',
    createTime: '2024-01-01 10:20:00'
  },
  {
    id: 6,
    positionName: '人事专员',
    positionCode: 'HR_STAFF',
    sortOrder: 6,
    status: 1,
    remark: '负责人员招聘与人事管理',
    createTime: '2024-01-02 09:00:00'
  },
  {
    id: 7,
    positionName: '系统管理员',
    positionCode: 'SYSTEM_ADMIN',
    sortOrder: 7,
    status: 1,
    remark: '负责系统维护与管理',
    createTime: '2024-01-02 09:30:00'
  },
  {
    id: 8,
    positionName: '技术总监',
    positionCode: 'TECH_DIRECTOR',
    sortOrder: 8,
    status: 1,
    remark: '技术部门负责人',
    createTime: '2024-01-03 14:00:00'
  },
  {
    id: 9,
    positionName: '高级工程师',
    positionCode: 'SENIOR_ENGINEER',
    sortOrder: 9,
    status: 1,
    remark: '高级技术开发人员',
    createTime: '2024-01-03 14:30:00'
  },
  {
    id: 10,
    positionName: '初级工程师',
    positionCode: 'JUNIOR_ENGINEER',
    sortOrder: 10,
    status: 1,
    remark: '初级技术开发人员',
    createTime: '2024-01-03 15:00:00'
  },
  {
    id: 11,
    positionName: '销售经理',
    positionCode: 'SALES_MANAGER',
    sortOrder: 11,
    status: 1,
    remark: '销售部门负责人',
    createTime: '2024-01-04 10:00:00'
  },
  {
    id: 12,
    positionName: '销售专员',
    positionCode: 'SALES_STAFF',
    sortOrder: 12,
    status: 1,
    remark: '销售人员',
    createTime: '2024-01-04 10:30:00'
  },
  {
    id: 13,
    positionName: '财务经理',
    positionCode: 'FINANCE_MANAGER',
    sortOrder: 13,
    status: 1,
    remark: '财务部门负责人',
    createTime: '2024-01-05 09:00:00'
  },
  {
    id: 14,
    positionName: '会计',
    positionCode: 'ACCOUNTANT',
    sortOrder: 14,
    status: 1,
    remark: '财务核算人员',
    createTime: '2024-01-05 09:30:00'
  },
  {
    id: 15,
    positionName: '实习生',
    positionCode: 'INTERN',
    sortOrder: 99,
    status: 0,
    remark: '实习岗位（已停用）',
    createTime: '2024-01-06 11:00:00'
  }
]

// 获取列表
const getList = () => {
  loading.value = true
  
  // 模拟异步请求
  setTimeout(() => {
    let filteredData = [...mockPositionData]
    
    // 按岗位名称筛选
    if (searchForm.positionName) {
      filteredData = filteredData.filter(item => 
        item.positionName.includes(searchForm.positionName)
      )
    }
    
    // 按岗位编码筛选
    if (searchForm.positionCode) {
      filteredData = filteredData.filter(item => 
        item.positionCode.toLowerCase().includes(searchForm.positionCode.toLowerCase())
      )
    }
    
    // 按状态筛选
    if (searchForm.status !== null) {
      filteredData = filteredData.filter(item => 
        item.status === searchForm.status
      )
    }
    
    // 按排序字段排序
    filteredData.sort((a, b) => a.sortOrder - b.sortOrder)
    
    // 分页
    pagination.total = filteredData.length
    const start = (pagination.current - 1) * pagination.size
    const end = start + pagination.size
    tableData.value = filteredData.slice(start, end)
    
    loading.value = false
  }, 300)
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  getList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    positionName: '',
    positionCode: '',
    status: null
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: PositionItem) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    positionName: row.positionName,
    positionCode: row.positionCode,
    sortOrder: row.sortOrder,
    status: row.status,
    remark: row.remark
  })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: PositionItem) => {
  await ElMessageBox.confirm(
    `确定要删除岗位「${row.positionName}」吗？`,
    '提示',
    { type: 'warning' }
  )
  
  // 模拟删除操作
  const index = mockPositionData.findIndex(item => item.id === row.id)
  if (index !== -1) {
    mockPositionData.splice(index, 1)
    ElMessage.success('删除成功')
    getList()
  }
}

// 状态切换
const handleStatusChange = (row: PositionItem) => {
  const item = mockPositionData.find(item => item.id === row.id)
  if (item) {
    item.status = row.status
    ElMessage.success(`岗位「${row.positionName}」已${row.status === 1 ? '启用' : '禁用'}`)
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    positionName: '',
    positionCode: '',
    sortOrder: 0,
    status: 1,
    remark: ''
  })
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  submitLoading.value = true
  
  // 模拟异步请求
  setTimeout(() => {
    if (isEdit.value) {
      // 编辑
      const item = mockPositionData.find(item => item.id === form.id)
      if (item) {
        item.positionName = form.positionName
        item.positionCode = form.positionCode
        item.sortOrder = form.sortOrder
        item.status = form.status
        item.remark = form.remark
        ElMessage.success('修改成功')
      }
    } else {
      // 新增
      const newId = Math.max(...mockPositionData.map(item => item.id)) + 1
      const newItem: PositionItem = {
        id: newId,
        positionName: form.positionName,
        positionCode: form.positionCode,
        sortOrder: form.sortOrder,
        status: form.status,
        remark: form.remark,
        createTime: new Date().toISOString().replace('T', ' ').slice(0, 19)
      }
      mockPositionData.push(newItem)
      ElMessage.success('新增成功')
    }
    
    submitLoading.value = false
    dialogVisible.value = false
    getList()
  }, 500)
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.position-management {
  .search-card {
    margin-bottom: 20px;
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
