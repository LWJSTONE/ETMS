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
        <el-table-column prop="id" label="日志编号" width="100" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)">
              {{ row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="操作人员" width="120" />
        <el-table-column prop="ipAddress" label="操作IP" width="140" />
        <el-table-column prop="status" label="操作状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
        <el-table-column prop="operationDesc" label="操作描述" min-width="200" show-overflow-tooltip />
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
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志编号">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operationType }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="IP归属地">{{ currentLog.ipLocation }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="执行耗时">{{ currentLog.costTime ? currentLog.costTime + 'ms' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ currentLog.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <el-input
            v-model="currentLog.requestParams"
            type="textarea"
            :rows="3"
            readonly
          />
        </el-descriptions-item>
        <el-descriptions-item label="浏览器" :span="1">{{ currentLog.browser }}</el-descriptions-item>
        <el-descriptions-item label="操作系统" :span="1">{{ currentLog.os }}</el-descriptions-item>
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
import { getLogList, getLogDetail, clearLogs, exportLogs, type LogItem } from '@/api/log'

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

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      operator: searchForm.operator || undefined,
      operationType: searchForm.operationType || undefined,
      status: searchForm.status,
      startTime: searchForm.timeRange?.[0] || undefined,
      endTime: searchForm.timeRange?.[1] || undefined
    }
    const res = await getLogList(params)
    if (res.data) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (error) {
    console.error('获取日志列表失败:', error)
    ElMessage.error('获取日志列表失败')
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
    operator: '',
    operationType: '',
    status: null,
    timeRange: []
  })
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.current = 1
  getList()
}

// 当前页改变
const handleCurrentChange = () => {
  getList()
}

// 查看详情
const handleViewDetail = async (row: LogItem) => {
  try {
    const res = await getLogDetail(row.id)
    if (res.data) {
      currentLog.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取日志详情失败:', error)
    ElMessage.error('获取日志详情失败')
  }
}

// 清空日志
const handleClear = async () => {
  // 检查是否选择了时间范围
  if (!searchForm.timeRange || searchForm.timeRange.length < 2) {
    ElMessage.warning('请先选择要清空的日志时间范围')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要清空 ${searchForm.timeRange[0]} 至 ${searchForm.timeRange[1]} 的操作日志吗？此操作不可恢复！`,
      '警告',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    await clearLogs({
      startTime: searchForm.timeRange[0],
      endTime: searchForm.timeRange[1]
    })
    ElMessage.success('日志已清空')
    getList()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('清空日志失败:', error)
      ElMessage.error('清空日志失败')
    }
  }
}

// 导出日志
const handleExport = async () => {
  try {
    const params = {
      operator: searchForm.operator || undefined,
      operationType: searchForm.operationType || undefined,
      status: searchForm.status,
      startTime: searchForm.timeRange?.[0] || undefined,
      endTime: searchForm.timeRange?.[1] || undefined
    }
    const blob = await exportLogs(params)
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('日志导出成功')
  } catch (error) {
    console.error('导出日志失败:', error)
    ElMessage.error('导出日志失败')
  }
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
