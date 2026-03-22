<template>
  <div class="config-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="参数名称">
          <el-input 
            v-model="searchForm.configName" 
            placeholder="请输入参数名称" 
            clearable 
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="参数键名">
          <el-input 
            v-model="searchForm.configKey" 
            placeholder="请输入参数键名" 
            clearable 
            style="width: 200px"
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
    
    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>参数列表</span>
          <div class="header-buttons">
            <el-button type="warning" @click="handleRefreshCache">
              <el-icon><RefreshRight /></el-icon>刷新缓存
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="configName" label="参数名称" width="180" show-overflow-tooltip />
        <el-table-column prop="configKey" label="参数键名" width="200" show-overflow-tooltip />
        <el-table-column prop="configValue" label="参数键值" width="150" show-overflow-tooltip />
        <el-table-column prop="configType" label="系统内置" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.configType === 'Y' ? 'primary' : 'info'">
              {{ row.configType === 'Y' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
              :disabled="row.configType === 'Y'"
            >
              删除
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
    
    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item label="参数名称" prop="configName">
          <el-input 
            v-model="form.configName" 
            placeholder="请输入参数名称" 
            maxlength="100"
          />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input 
            v-model="form.configKey" 
            placeholder="请输入参数键名" 
            maxlength="100"
            :disabled="isEdit && form.configType === 'Y'"
          />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input 
            v-model="form.configValue" 
            placeholder="请输入参数键值" 
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="系统内置" prop="configType">
          <el-radio-group v-model="form.configType" :disabled="isEdit">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

// 定义配置项类型
interface ConfigItem {
  id: number
  configName: string
  configKey: string
  configValue: string
  configType: 'Y' | 'N'
  remark: string
  status: number
  createTime: string
}

// 模拟数据
const mockData: ConfigItem[] = [
  {
    id: 1,
    configName: '系统名称',
    configKey: 'sys.system.name',
    configValue: '企业培训管理系统',
    configType: 'Y',
    remark: '系统显示名称，用于浏览器标题和登录页面',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    configName: '系统版本',
    configKey: 'sys.system.version',
    configValue: 'v1.0.0',
    configType: 'Y',
    remark: '系统版本号',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 3,
    configName: '文件上传路径',
    configKey: 'sys.file.uploadPath',
    configValue: '/upload/files',
    configType: 'Y',
    remark: '文件上传存储路径',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 4,
    configName: '文件大小限制',
    configKey: 'sys.file.maxSize',
    configValue: '10',
    configType: 'Y',
    remark: '文件上传大小限制，单位MB',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 5,
    configName: '验证码开关',
    configKey: 'sys.captcha.enabled',
    configValue: 'true',
    configType: 'Y',
    remark: '是否开启验证码功能',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 6,
    configName: '登录失败次数',
    configKey: 'sys.login.failCount',
    configValue: '5',
    configType: 'Y',
    remark: '登录失败次数限制，超过后锁定账户',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 7,
    configName: '密码最小长度',
    configKey: 'sys.password.minLength',
    configValue: '6',
    configType: 'Y',
    remark: '密码最小长度要求',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 8,
    configName: '会话超时时间',
    configKey: 'sys.session.timeout',
    configValue: '30',
    configType: 'Y',
    remark: '会话超时时间，单位分钟',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 9,
    configName: '考试默认时长',
    configKey: 'exam.default.duration',
    configValue: '60',
    configType: 'N',
    remark: '考试默认时长，单位分钟',
    status: 1,
    createTime: '2024-01-15 14:30:00'
  },
  {
    id: 10,
    configName: '考试及格分数',
    configKey: 'exam.default.passScore',
    configValue: '60',
    configType: 'N',
    remark: '考试默认及格分数',
    status: 1,
    createTime: '2024-01-15 14:30:00'
  },
  {
    id: 11,
    configName: '签到开始时间',
    configKey: 'attendance.startTime',
    configValue: '08:30',
    configType: 'N',
    remark: '签到开始时间',
    status: 1,
    createTime: '2024-01-20 09:00:00'
  },
  {
    id: 12,
    configName: '签到结束时间',
    configKey: 'attendance.endTime',
    configValue: '09:00',
    configType: 'N',
    remark: '签到结束时间，超过视为迟到',
    status: 1,
    createTime: '2024-01-20 09:00:00'
  },
  {
    id: 13,
    configName: '补签天数限制',
    configKey: 'attendance.reissueDays',
    configValue: '3',
    configType: 'N',
    remark: '允许补签的天数限制',
    status: 1,
    createTime: '2024-01-20 09:00:00'
  },
  {
    id: 14,
    configName: '系统公告',
    configKey: 'sys.notice.content',
    configValue: '欢迎使用企业培训管理系统',
    configType: 'N',
    remark: '系统首页公告内容',
    status: 0,
    createTime: '2024-02-01 08:00:00'
  }
]

// 存储数据副本用于模拟操作
let configData = [...mockData]
let nextId = mockData.length + 1

// 搜索表单
const searchForm = reactive({
  configName: '',
  configKey: ''
})

// 表格数据
const tableData = ref<ConfigItem[]>([])
const loading = ref(false)
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 新增/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑参数' : '新增参数')
const formRef = ref<FormInstance>()
const form = reactive({
  id: null as number | null,
  configName: '',
  configKey: '',
  configValue: '',
  configType: 'N' as 'Y' | 'N',
  remark: '',
  status: 1
})

// 表单验证规则
const rules: FormRules = {
  configName: [
    { required: true, message: '请输入参数名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  configKey: [
    { required: true, message: '请输入参数键名', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9._-]*$/, message: '以字母开头，只能包含字母、数字、点、下划线和横线', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '请输入参数键值', trigger: 'blur' }
  ],
  configType: [
    { required: true, message: '请选择是否系统内置', trigger: 'change' }
  ]
}

// 获取当前时间字符串
const getCurrentTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取列表
const getList = () => {
  loading.value = true
  
  // 模拟异步请求
  setTimeout(() => {
    let filteredData = [...configData]
    
    // 按参数名称搜索
    if (searchForm.configName) {
      filteredData = filteredData.filter(item => 
        item.configName.toLowerCase().includes(searchForm.configName.toLowerCase())
      )
    }
    
    // 按参数键名搜索
    if (searchForm.configKey) {
      filteredData = filteredData.filter(item => 
        item.configKey.toLowerCase().includes(searchForm.configKey.toLowerCase())
      )
    }
    
    // 计算分页
    pagination.total = filteredData.length
    const startIndex = (pagination.current - 1) * pagination.size
    const endIndex = startIndex + pagination.size
    tableData.value = filteredData.slice(startIndex, endIndex)
    
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
    configName: '',
    configKey: ''
  })
  handleSearch()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'N',
    remark: '',
    status: 1
  })
  dialogVisible.value = true
  // 清除表单验证
  formRef.value?.clearValidate()
}

// 编辑
const handleEdit = (row: ConfigItem) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    configName: row.configName,
    configKey: row.configKey,
    configValue: row.configValue,
    configType: row.configType,
    remark: row.remark,
    status: row.status
  })
  dialogVisible.value = true
  // 清除表单验证
  formRef.value?.clearValidate()
}

// 删除
const handleDelete = async (row: ConfigItem) => {
  if (row.configType === 'Y') {
    ElMessage.warning('系统内置参数不能删除')
    return
  }
  
  await ElMessageBox.confirm(
    `确定要删除参数「${row.configName}」吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  
  // 模拟删除操作
  configData = configData.filter(item => item.id !== row.id)
  ElMessage.success('删除成功')
  getList()
}

// 刷新缓存
const handleRefreshCache = async () => {
  await ElMessageBox.confirm(
    '确定要刷新系统参数缓存吗？',
    '刷新缓存',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  
  loading.value = true
  // 模拟刷新缓存
  setTimeout(() => {
    loading.value = false
    ElMessage.success('缓存刷新成功')
  }, 1000)
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
      const index = configData.findIndex(item => item.id === form.id)
      if (index !== -1) {
        configData[index] = {
          ...configData[index],
          configName: form.configName,
          configKey: form.configKey,
          configValue: form.configValue,
          configType: form.configType,
          remark: form.remark,
          status: form.status
        }
      }
      ElMessage.success('更新成功')
    } else {
      // 新增 - 检查键名是否重复
      const exists = configData.some(item => item.configKey === form.configKey)
      if (exists) {
        ElMessage.error('参数键名已存在')
        submitLoading.value = false
        return
      }
      
      // 添加新记录
      const newConfig: ConfigItem = {
        id: nextId++,
        configName: form.configName,
        configKey: form.configKey,
        configValue: form.configValue,
        configType: form.configType as 'Y' | 'N',
        remark: form.remark,
        status: form.status,
        createTime: getCurrentTime()
      }
      configData.unshift(newConfig)
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
.config-management {
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
  }
  
  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>
