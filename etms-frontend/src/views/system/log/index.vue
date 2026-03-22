<template>
  <div class="log-management">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="操作人员">
          <el-input v-model="searchForm.operator" placeholder="请输入操作人员" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" clearable>
            <el-option label="新增" value="新增" />
            <el-option label="修改" value="修改" />
            <el-option label="删除" value="删除" />
            <el-option label="查询" value="查询" />
            <el-option label="导出" value="导出" />
            <el-option label="登录" value="登录" />
            <el-option label="退出" value="退出" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
          />
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
          <span>操作日志列表</span>
          <div class="header-buttons">
            <el-button type="danger" @click="handleClear">
              <el-icon><Delete /></el-icon>清空日志
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>导出
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="logId" label="日志编号" width="100" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)">
              {{ row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人员" width="120" />
        <el-table-column prop="ip" label="操作IP" width="140" />
        <el-table-column prop="status" label="操作状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column prop="description" label="操作描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志编号">{{ currentLog.logId }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operationType }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ currentLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input
            v-model="currentLog.requestParams"
            type="textarea"
            :rows="3"
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <el-input
            v-model="currentLog.responseResult"
            type="textarea"
            :rows="3"
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.status === 0">
          <el-input
            v-model="currentLog.errorMsg"
            type="textarea"
            :rows="2"
            readonly
          />
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 日志数据类型
interface LogItem {
  logId: number
  module: string
  operationType: string
  operator: string
  ip: string
  status: number
  operationTime: string
  description: string
  method?: string
  requestParams?: string
  responseResult?: string
  errorMsg?: string
}

// 日期快捷选项
const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const today = new Date()
      return [today, today]
    }
  },
  {
    text: '昨天',
    value: () => {
      const yesterday = new Date()
      yesterday.setTime(yesterday.getTime() - 3600 * 1000 * 24)
      return [yesterday, yesterday]
    }
  },
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  }
]

// 搜索表单
const searchForm = reactive({
  operator: '',
  operationType: '',
  status: null as number | null,
  timeRange: [] as string[]
})

// 表格数据
const tableData = ref<LogItem[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })

// 详情对话框
const detailDialogVisible = ref(false)
const currentLog = ref<LogItem | null>(null)

// 模拟日志数据
const mockLogData: LogItem[] = [
  {
    logId: 1001,
    module: '用户管理',
    operationType: '新增',
    operator: '管理员',
    ip: '192.168.1.100',
    status: 1,
    operationTime: '2024-01-15 10:30:25',
    description: '新增用户：张三',
    method: 'POST /api/user/create',
    requestParams: '{"username":"zhangsan","realName":"张三","gender":1,"phone":"13800138001"}',
    responseResult: '{"code":200,"message":"操作成功","data":{"id":101}}'
  },
  {
    logId: 1002,
    module: '角色管理',
    operationType: '修改',
    operator: '管理员',
    ip: '192.168.1.100',
    status: 1,
    operationTime: '2024-01-15 10:35:18',
    description: '修改角色：培训管理员',
    method: 'PUT /api/role/update',
    requestParams: '{"id":2,"roleName":"培训管理员","roleDesc":"负责培训管理"}',
    responseResult: '{"code":200,"message":"操作成功"}'
  },
  {
    logId: 1003,
    module: '课程管理',
    operationType: '删除',
    operator: '培训管理员',
    ip: '192.168.1.101',
    status: 1,
    operationTime: '2024-01-15 11:20:45',
    description: '删除课程：测试课程',
    method: 'DELETE /api/course/delete/105',
    requestParams: '{"id":105}',
    responseResult: '{"code":200,"message":"操作成功"}'
  },
  {
    logId: 1004,
    module: '登录管理',
    operationType: '登录',
    operator: '张三',
    ip: '192.168.1.102',
    status: 1,
    operationTime: '2024-01-15 09:00:12',
    description: '用户登录成功',
    method: 'POST /api/auth/login',
    requestParams: '{"username":"zhangsan"}',
    responseResult: '{"code":200,"message":"登录成功"}'
  },
  {
    logId: 1005,
    module: '登录管理',
    operationType: '登录',
    operator: '李四',
    ip: '192.168.1.103',
    status: 0,
    operationTime: '2024-01-15 09:05:33',
    description: '用户登录失败',
    method: 'POST /api/auth/login',
    requestParams: '{"username":"lisi"}',
    responseResult: '{"code":401,"message":"密码错误"}',
    errorMsg: '密码错误，登录失败'
  },
  {
    logId: 1006,
    module: '考试管理',
    operationType: '导出',
    operator: '管理员',
    ip: '192.168.1.100',
    status: 1,
    operationTime: '2024-01-15 14:22:56',
    description: '导出考试成绩数据',
    method: 'GET /api/exam/result/export',
    requestParams: '{"examId":10}',
    responseResult: '{"code":200,"message":"导出成功"}'
  },
  {
    logId: 1007,
    module: '部门管理',
    operationType: '新增',
    operator: '管理员',
    ip: '192.168.1.100',
    status: 1,
    operationTime: '2024-01-14 16:45:30',
    description: '新增部门：研发部',
    method: 'POST /api/dept/create',
    requestParams: '{"deptName":"研发部","parentId":1,"sortOrder":2}',
    responseResult: '{"code":200,"message":"操作成功","data":{"id":15}}'
  },
  {
    logId: 1008,
    module: '培训计划',
    operationType: '修改',
    operator: '培训管理员',
    ip: '192.168.1.101',
    status: 1,
    operationTime: '2024-01-14 15:30:22',
    description: '修改培训计划：2024年入职培训',
    method: 'PUT /api/training/plan/update',
    requestParams: '{"id":20,"planName":"2024年入职培训","status":2}',
    responseResult: '{"code":200,"message":"操作成功"}'
  },
  {
    logId: 1009,
    module: '签到管理',
    operationType: '查询',
    operator: '张三',
    ip: '192.168.1.102',
    status: 1,
    operationTime: '2024-01-15 08:55:10',
    description: '查询签到记录',
    method: 'GET /api/attendance/record/list',
    requestParams: '{"current":1,"size":10}',
    responseResult: '{"code":200,"message":"操作成功","data":{"total":50,"records":[...]}}'
  },
  {
    logId: 1010,
    module: '登录管理',
    operationType: '退出',
    operator: '管理员',
    ip: '192.168.1.100',
    status: 1,
    operationTime: '2024-01-15 18:00:00',
    description: '用户退出登录',
    method: 'POST /api/auth/logout',
    requestParams: '{}',
    responseResult: '{"code":200,"message":"退出成功"}'
  },
  {
    logId: 1011,
    module: '题库管理',
    operationType: '新增',
    operator: '培训管理员',
    ip: '192.168.1.101',
    status: 1,
    operationTime: '2024-01-13 10:15:40',
    description: '新增试题：Vue基础知识',
    method: 'POST /api/question/create',
    requestParams: '{"type":1,"content":"Vue3使用什么API？","answer":"Composition API"}',
    responseResult: '{"code":200,"message":"操作成功","data":{"id":500}}'
  },
  {
    logId: 1012,
    module: '用户管理',
    operationType: '删除',
    operator: '管理员',
    ip: '192.168.1.100',
    status: 0,
    operationTime: '2024-01-12 14:20:15',
    description: '删除用户失败：用户存在关联数据',
    method: 'DELETE /api/user/delete/99',
    requestParams: '{"id":99}',
    responseResult: '{"code":500,"message":"删除失败"}',
    errorMsg: '该用户存在培训记录，无法删除'
  },
  {
    logId: 1013,
    module: '试卷管理',
    operationType: '修改',
    operator: '培训管理员',
    ip: '192.168.1.101',
    status: 1,
    operationTime: '2024-01-11 09:45:28',
    description: '修改试卷：前端基础测试',
    method: 'PUT /api/paper/update',
    requestParams: '{"id":30,"paperName":"前端基础测试","totalScore":100}',
    responseResult: '{"code":200,"message":"操作成功"}'
  },
  {
    logId: 1014,
    module: '字典管理',
    operationType: '新增',
    operator: '管理员',
    ip: '192.168.1.100',
    status: 1,
    operationTime: '2024-01-10 11:30:55',
    description: '新增字典项：课程类型',
    method: 'POST /api/dict/create',
    requestParams: '{"dictType":"course_type","dictLabel":"在线课程"}',
    responseResult: '{"code":200,"message":"操作成功","data":{"id":200}}'
  },
  {
    logId: 1015,
    module: '系统配置',
    operationType: '修改',
    operator: '管理员',
    ip: '192.168.1.100',
    status: 1,
    operationTime: '2024-01-09 16:10:42',
    description: '修改系统配置：考试时长限制',
    method: 'PUT /api/config/update',
    requestParams: '{"configKey":"exam_duration","configValue":"120"}',
    responseResult: '{"code":200,"message":"操作成功"}'
  }
]

// 获取操作类型标签颜色
const getOperationTypeTag = (type: string): string => {
  const typeMap: Record<string, string> = {
    '新增': 'success',
    '修改': 'warning',
    '删除': 'danger',
    '查询': 'info',
    '导出': 'primary',
    '登录': 'primary',
    '退出': 'info'
  }
  return typeMap[type] || 'info'
}

// 获取列表（模拟数据过滤）
const getList = () => {
  loading.value = true
  
  // 模拟异步请求
  setTimeout(() => {
    let filteredData = [...mockLogData]
    
    // 按操作人员筛选
    if (searchForm.operator) {
      filteredData = filteredData.filter(item => 
        item.operator.includes(searchForm.operator)
      )
    }
    
    // 按操作类型筛选
    if (searchForm.operationType) {
      filteredData = filteredData.filter(item => 
        item.operationType === searchForm.operationType
      )
    }
    
    // 按操作状态筛选
    if (searchForm.status !== null) {
      filteredData = filteredData.filter(item => 
        item.status === searchForm.status
      )
    }
    
    // 按时间范围筛选
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      const startDate = new Date(searchForm.timeRange[0])
      const endDate = new Date(searchForm.timeRange[1])
      endDate.setHours(23, 59, 59, 999)
      
      filteredData = filteredData.filter(item => {
        const itemDate = new Date(item.operationTime)
        return itemDate >= startDate && itemDate <= endDate
      })
    }
    
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
    operator: '',
    operationType: '',
    status: null,
    timeRange: []
  })
  handleSearch()
}

// 查看详情
const handleViewDetail = (row: LogItem) => {
  currentLog.value = { ...row }
  detailDialogVisible.value = true
}

// 清空日志
const handleClear = async () => {
  await ElMessageBox.confirm(
    '确定要清空所有操作日志吗？此操作不可恢复！',
    '警告',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  )
  
  // 模拟清空操作
  tableData.value = []
  pagination.total = 0
  ElMessage.success('日志已清空')
}

// 导出日志
const handleExport = () => {
  // 模拟导出功能
  ElMessage.success('日志导出成功')
  
  // 创建CSV内容
  const headers = ['日志编号', '操作模块', '操作类型', '操作人员', '操作IP', '操作状态', '操作时间', '操作描述']
  const csvContent = [
    headers.join(','),
    ...tableData.value.map(item => [
      item.logId,
      item.module,
      item.operationType,
      item.operator,
      item.ip,
      item.status === 1 ? '成功' : '失败',
      item.operationTime,
      `"${item.description}"`
    ].join(','))
  ].join('\n')
  
  // 创建下载链接
  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `操作日志_${new Date().toISOString().slice(0, 10)}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.log-management {
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
